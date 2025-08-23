package com.example.myapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.model.ShoppingListAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.HashSet;
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
    private static final String PREFS_NAME = "nutrition_prefs";
    private static final int MAX_SHOPPING_ITEMS = 50;

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
        shoppingListAdapter = new ShoppingListAdapter(shoppingListItems);
        rvShoppingList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvShoppingList.setAdapter(shoppingListAdapter);

        // Load data
        loadCalories();
        loadMeals();
        loadShoppingList();

        // Button listeners
        btnAddBreakfast.setOnClickListener(v -> addMeal("breakfast"));
        btnAddLunch.setOnClickListener(v -> addMeal("lunch"));
        btnAddDinner.setOnClickListener(v -> addMeal("dinner"));
        btnAddSnacks.setOnClickListener(v -> addMeal("snacks"));

        btnAddItem.setOnClickListener(v -> addShoppingItem());

        return view;
    }

    private void loadCalories() {
        int consumed = sharedPreferences.getInt("calories_consumed", 0);
        int goal = sharedPreferences.getInt("calories_goal", 2000);

        tvCaloriesConsumed.setText(String.valueOf(consumed));
        tvCaloriesGoal.setText(String.valueOf(goal));

        int progress = (goal == 0) ? 0 : (int) ((consumed / (float) goal) * 100);
        progressCalories.setProgress(progress);
    }

    private void loadMeals() {
        tvBreakfastCalories.setText(sharedPreferences.getString("breakfast_calories", "0 calories"));
        tvLunchCalories.setText(sharedPreferences.getString("lunch_calories", "0 calories"));
        tvDinnerCalories.setText(sharedPreferences.getString("dinner_calories", "0 calories"));
        tvSnacksCalories.setText(sharedPreferences.getString("snacks_calories", "0 calories"));
    }

    private void addMeal(String mealType) {
        // Placeholder: just show a toast. Replace with Nutritionix API integration.
        Toast.makeText(getContext(), mealType.substring(0, 1).toUpperCase() + mealType.substring(1) + " logged!", Toast.LENGTH_SHORT).show();

        // Example: increment calories (for demo)
        int currentCalories = sharedPreferences.getInt("calories_consumed", 0);
        int newCalories = currentCalories + 100; // assume 100 calories per add
        sharedPreferences.edit().putInt("calories_consumed", newCalories).apply();
        loadCalories();
    }

    private void loadShoppingList() {
        Set<String> itemsSet = sharedPreferences.getStringSet("shopping_list", new HashSet<>());
        shoppingListItems.clear();
        shoppingListItems.addAll(itemsSet);
        shoppingListAdapter.notifyDataSetChanged();
    }

    private void addShoppingItem() {
        // You can implement a dialog or input here. For now, just add placeholder.
        if (shoppingListItems.size() >= MAX_SHOPPING_ITEMS) {
            Toast.makeText(getContext(), "Shopping list limit reached (50 items).", Toast.LENGTH_SHORT).show();
            return;
        }

        String newItem = "New Item " + (shoppingListItems.size() + 1); // placeholder
        shoppingListItems.add(newItem);
        shoppingListAdapter.notifyDataSetChanged();

        // Save
        Set<String> itemsSet = new HashSet<>(shoppingListItems);
        sharedPreferences.edit().putStringSet("shopping_list", itemsSet).apply();
    }
}