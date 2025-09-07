package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.model.Food;
import com.example.myapp.model.ShoppingListAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NutritionFragment extends Fragment {

    private TextView tvCaloriesConsumed, tvCaloriesGoal;
    private LinearProgressIndicator progressCalories;

    private TextView tvBreakfastCalories, tvLunchCalories, tvDinnerCalories, tvSnacksCalories;
    private MaterialButton btnAddBreakfast, btnAddLunch, btnAddDinner, btnAddSnacks;

    private MaterialButton btnAddItem;
    private RecyclerView rvShoppingList;
    private ShoppingListAdapter shoppingListAdapter;
    private ArrayList<String> shoppingListItems;

    private SharedPreferences sharedPreferences;
    static final String PREFS_NAME = "nutrition_prefs";
    private static final int MAX_SHOPPING_ITEMS = 50;
    private static final String PREF_LAST_RESET = "last_reset_date";
    static final String KEY_LAST_7_DAYS = "last_7_days_calories";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nutrition, container, false);

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, getActivity().MODE_PRIVATE);


        // Calories section
        tvCaloriesConsumed = view.findViewById(R.id.tvCaloriesConsumed);
        tvCaloriesGoal = view.findViewById(R.id.tvCaloriesGoal);
        progressCalories = view.findViewById(R.id.progressCalories);

        // Meals section
        tvBreakfastCalories = view.findViewById(R.id.tvBreakfastCalories);
        tvLunchCalories = view.findViewById(R.id.tvLunchCalories);
        tvDinnerCalories = view.findViewById(R.id.tvDinnerCalories);
        tvSnacksCalories = view.findViewById(R.id.tvSnacksCalories);

        btnAddBreakfast = view.findViewById(R.id.btnAddBreakfast);
        btnAddLunch = view.findViewById(R.id.btnAddLunch);
        btnAddDinner = view.findViewById(R.id.btnAddDinner);
        btnAddSnacks = view.findViewById(R.id.btnAddSnacks);

        // Shopping list section
        btnAddItem = view.findViewById(R.id.btnAddItem);
        rvShoppingList = view.findViewById(R.id.rvShoppingList);
        shoppingListItems = new ArrayList<>();
        shoppingListItems = new ArrayList<>();
        shoppingListAdapter = new ShoppingListAdapter(shoppingListItems, updatedList -> {
            saveShoppingList(); // just save when the list changes
        });

        rvShoppingList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvShoppingList.setAdapter(shoppingListAdapter);

// Load data
        loadCalories();
        loadShoppingList();
        updateMealCalories();
        resetMealsIfNewDay();

        // "+" buttons for adding/searching foods
        btnAddBreakfast.setOnClickListener(v -> openFoodBottomSheet("breakfast"));
        btnAddLunch.setOnClickListener(v -> openFoodBottomSheet("lunch"));
        btnAddDinner.setOnClickListener(v -> openFoodBottomSheet("dinner"));
        btnAddSnacks.setOnClickListener(v -> openFoodBottomSheet("snacks"));

        // Meal card clicks to view logged foods
        view.findViewById(R.id.cardBreakfast).setOnClickListener(v -> openMealFoods("breakfast", "🌅"));
        view.findViewById(R.id.cardLunch).setOnClickListener(v -> openMealFoods("lunch", "☀️"));
        view.findViewById(R.id.cardDinner).setOnClickListener(v -> openMealFoods("dinner", "🌙"));
        view.findViewById(R.id.cardSnacks).setOnClickListener(v -> openMealFoods("snacks", "🍿"));

        // Shopping list
        btnAddItem.setOnClickListener(v -> addShoppingItem());

        return view;
    }

    private void update7DayCalories(int todayCalories) {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_LAST_7_DAYS, "{}"); // map format

        Map<String, Integer> dailyCalories = new Gson().fromJson(json, new TypeToken<Map<String, Integer>>(){}.getType());
        if (dailyCalories == null) dailyCalories = new HashMap<>();

        // Key = yyyy-MM-dd for today
        String todayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        dailyCalories.put(todayKey, todayCalories);

        // Save back
        prefs.edit().putString(KEY_LAST_7_DAYS, new Gson().toJson(dailyCalories)).apply();
    }

    // Opens FoodBottomSheet in SEARCH mode for "+" buttons
    private void openFoodBottomSheet(String mealType) {
        FoodBottomSheet bottomSheet = FoodBottomSheet.newInstance(mealType, FoodBottomSheet.Mode.SEARCH);
        bottomSheet.setListener(() -> {
            updateMealCalories();
            loadCalories();
            update7DayCalories(getTotalCalories());
        });
        bottomSheet.show(getParentFragmentManager(), "FoodBottomSheet_" + mealType);
    }

    // Opens FoodBottomSheet in LOGGED mode for card clicks
    private void openMealFoods(String mealType, String emoji) {
        String json = sharedPreferences.getString(mealType + "_foods", null);
        List<Food> loggedFoods = new ArrayList<>();
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Food>>() {}.getType();
            loggedFoods = gson.fromJson(json, type);
        }

        FoodBottomSheet bottomSheet = FoodBottomSheet.newInstance(mealType, FoodBottomSheet.Mode.LOGGED);
        bottomSheet.setListener(() -> {
            updateMealCalories();
            loadCalories();
            update7DayCalories(getTotalCalories());
        });
        bottomSheet.show(getParentFragmentManager(), "FoodBottomSheet_" + mealType);
    }

    private void loadCalories() {
        int total = getTotalCalories();
        int goal = sharedPreferences.getInt("calories_goal", 2000);

        tvCaloriesConsumed.setText(String.valueOf(total));
        tvCaloriesGoal.setText(String.valueOf(goal));

        int progress = (goal == 0) ? 0 : (int) ((total / (float) goal) * 100);
        progressCalories.setProgress(progress);
    }

    private int getTotalCalories() {
        return getMealCalories("breakfast") + getMealCalories("lunch") +
                getMealCalories("dinner") + getMealCalories("snacks");
    }

    private void updateMealCalories() {
        tvBreakfastCalories.setText(getMealCalories("breakfast") + " cal");
        tvLunchCalories.setText(getMealCalories("lunch") + " cal");
        tvDinnerCalories.setText(getMealCalories("dinner") + " cal");
        tvSnacksCalories.setText(getMealCalories("snacks") + " cal");
    }

    private int getMealCalories(String mealType) {
        String json = sharedPreferences.getString(mealType + "_foods", null);
        if (json == null) return 0;

        Gson gson = new Gson();
        Type type = new TypeToken<List<Food>>() {}.getType();
        List<Food> foods = gson.fromJson(json, type);

        int total = 0;
        for (Food f : foods) total += f.getCalories();
        return total;
    }

    private void loadShoppingList() {
        Set<String> itemsSet = sharedPreferences.getStringSet("shopping_list", new HashSet<>());
        shoppingListItems.clear();
        shoppingListItems.addAll(itemsSet);
        shoppingListAdapter.notifyDataSetChanged();
    }

    private void addShoppingItem() {
        if (shoppingListItems.size() >= MAX_SHOPPING_ITEMS) {
            Toast.makeText(getContext(), "Shopping list limit reached (50 items).", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText etNewItem = new EditText(getContext());
        etNewItem.setHint("Enter item");

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Add Shopping Item")
                .setView(etNewItem)
                .setPositiveButton("Add", (dialog, which) -> {
                    String newItem = etNewItem.getText().toString().trim();
                    if (!newItem.isEmpty()) {
                        shoppingListItems.add(newItem);
                        shoppingListAdapter.notifyDataSetChanged();
                        saveShoppingList();
                    } else {
                        Toast.makeText(getContext(), "Item cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Save current shopping list to SharedPreferences
    private void saveShoppingList() {
        Set<String> itemsSet = new HashSet<>(shoppingListItems);
        sharedPreferences.edit().putStringSet("shopping_list", itemsSet).apply();
    }

    private void resetMealsIfNewDay() {
        SharedPreferences prefs = sharedPreferences;
        String lastReset = prefs.getString(PREF_LAST_RESET, "");

        // Get today's date in yyyy-MM-dd format
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (!today.equals(lastReset)) {
            // New day → first save today’s calories into rolling 7-day list
            int todayCalories = getTotalCalories();
            update7DayCalories(todayCalories);

            // Then reset meals safely
            clearAllMealLogs();

            // Update last reset date AFTER clearing
            prefs.edit().putString(PREF_LAST_RESET, today).apply();
        }
    }

    private void clearAllMealLogs() {
        String[] meals = {"breakfast", "lunch", "dinner", "snacks"};
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (String meal : meals) {
            editor.remove(meal + "_foods");
        }
        editor.apply();

        // Update UI safely
        tvBreakfastCalories.setText("0 cal");
        tvLunchCalories.setText("0 cal");
        tvDinnerCalories.setText("0 cal");
        tvSnacksCalories.setText("0 cal");
        tvCaloriesConsumed.setText("0");
        progressCalories.setProgress(0);

        // If FoodBottomSheet is open, notify it to reload empty lists
        FragmentManager fm = getParentFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        for (Fragment f : fragments) {
            if (f instanceof FoodBottomSheet) {
                ((FoodBottomSheet) f).loadLoggedFoods(); // safely reload
            }
        }
    }
}