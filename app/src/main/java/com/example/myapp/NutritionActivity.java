package com.example.myapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.model.ShoppingListAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class NutritionActivity extends AppCompatActivity {

    private LinearLayout daySelectorLayout;
    private TextView warningText;
    private EditText breakfastEditText, lunchEditText, dinnerEditText;
    private Button logBreakfastBtn, logLunchBtn, logDinnerBtn;

    private EditText shoppingItemEditText;
    private Button addShoppingItemBtn;
    private RecyclerView shoppingListRecyclerView;

    private SharedPreferences sharedPreferences;
    private String selectedDateKey;

    private ShoppingListAdapter shoppingListAdapter;
    private ArrayList<String> shoppingListItems;

    private static final String PREFS_NAME = "nutrition_prefs";
    private static final int MAX_SHOPPING_ITEMS = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        daySelectorLayout = findViewById(R.id.daySelectorLayout);
        warningText = findViewById(R.id.warningText);

        breakfastEditText = findViewById(R.id.breakfastEditText);
        lunchEditText = findViewById(R.id.lunchEditText);
        dinnerEditText = findViewById(R.id.dinnerEditText);

        logBreakfastBtn = findViewById(R.id.logBreakfastBtn);
        logLunchBtn = findViewById(R.id.logLunchBtn);
        logDinnerBtn = findViewById(R.id.logDinnerBtn);

        shoppingItemEditText = findViewById(R.id.shoppingItemEditText);
        addShoppingItemBtn = findViewById(R.id.addShoppingItemBtn);
        shoppingListRecyclerView = findViewById(R.id.shoppingListRecyclerView);

        shoppingListItems = new ArrayList<>();


        // Initialize day selector with 7 days starting today
        setupDaySelector();

        // Default to today
        selectedDateKey = getDateKey(Calendar.getInstance());

        loadMealsForSelectedDate();
        loadShoppingList();

        // Log meal buttons
        logBreakfastBtn.setOnClickListener(v -> logMeal("breakfast", breakfastEditText.getText().toString()));
        logLunchBtn.setOnClickListener(v -> logMeal("lunch", lunchEditText.getText().toString()));
        logDinnerBtn.setOnClickListener(v -> logMeal("dinner", dinnerEditText.getText().toString()));

        // Add shopping list item button
        addShoppingItemBtn.setOnClickListener(v -> addShoppingItem());

        // Bottom nav setup (like your stopwatch example)
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_nutrition); // replace "stretches" with your nav id for nutrition if you want

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_nutrition) { // replace with nutrition id if you add one
                    return true;
                }
                if (id == R.id.navigation_home) {
                    startActivity(new Intent(NutritionActivity.this, MainActivity.class));
                    return true;
                }
                if (id == R.id.navigation_workouts) {
                    startActivity(new Intent(NutritionActivity.this, WorkoutsActivity.class));
                    return true;
                }
                if (id == R.id.navigation_stopwatch) {
                    startActivity(new Intent(NutritionActivity.this, StopwatchActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void setupDaySelector() {
        daySelectorLayout.removeAllViews();
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            final String dateKey = getDateKey(calendar);
            String dayLabel = new SimpleDateFormat("EEE\ndd", Locale.getDefault()).format(calendar.getTime());

            Button dayButton = new Button(this);
            dayButton.setText(dayLabel);
            dayButton.setTag(dateKey);
            dayButton.setAllCaps(false);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            dayButton.setLayoutParams(params);

            if (dateKey.equals(selectedDateKey)) {
                dayButton.setBackgroundColor(getResources().getColor(R.color.vibrantAccent));
                dayButton.setTextColor(getResources().getColor(android.R.color.white));
            }

            dayButton.setOnClickListener(v -> {
                selectedDateKey = (String) v.getTag();
                updateDaySelectorUI();
                loadMealsForSelectedDate();
            });

            daySelectorLayout.addView(dayButton);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void updateDaySelectorUI() {
        int childCount = daySelectorLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = daySelectorLayout.getChildAt(i);
            if (child instanceof Button) {
                String tag = (String) child.getTag();
                if (tag.equals(selectedDateKey)) {
                    child.setBackgroundColor(getResources().getColor(R.color.vibrantAccent));
                    ((Button) child).setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    child.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    ((Button) child).setTextColor(getResources().getColor(android.R.color.black));
                }
            }
        }
    }

    private String getDateKey(Calendar calendar) {
        return new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.getTime());
    }

    private void loadMealsForSelectedDate() {
        breakfastEditText.setText(sharedPreferences.getString(selectedDateKey + "_breakfast", ""));
        lunchEditText.setText(sharedPreferences.getString(selectedDateKey + "_lunch", ""));
        dinnerEditText.setText(sharedPreferences.getString(selectedDateKey + "_dinner", ""));
    }

    private void logMeal(String mealType, String mealText) {
        if (mealText.isEmpty()) {
            Toast.makeText(this, "Please enter your " + mealType + " before logging.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Save meal description to SharedPreferences
        sharedPreferences.edit().putString(selectedDateKey + "_" + mealType, mealText).apply();

        // For now, just show a toast as placeholder for API call
        Toast.makeText(this, mealType.substring(0,1).toUpperCase() + mealType.substring(1) + " logged!", Toast.LENGTH_SHORT).show();

        // TODO: Integrate Nutritionix API call here to log calories, protein, carbs, fat
        // If user changes text and logs again, replace original data but still count as API call usage
    }

    private void loadShoppingList() {
        Set<String> itemsSet = sharedPreferences.getStringSet("shopping_list", new HashSet<>());
        shoppingListItems.clear();
        shoppingListItems.addAll(itemsSet);
        shoppingListAdapter.notifyDataSetChanged();
    }

    private void addShoppingItem() {
        String newItem = shoppingItemEditText.getText().toString().trim();
        if (newItem.isEmpty()) {
            Toast.makeText(this, "Please enter an item to add.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (shoppingListItems.size() >= MAX_SHOPPING_ITEMS) {
            Toast.makeText(this, "Shopping list limit reached (50 items).", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newItem.length() > 50) {
            Toast.makeText(this, "Item is too long (max 50 characters).", Toast.LENGTH_SHORT).show();
            return;
        }

        shoppingListItems.add(newItem);
        shoppingItemEditText.setText("");
        shoppingListAdapter.notifyDataSetChanged();

        // Save shopping list to SharedPreferences
        Set<String> itemsSet = new HashSet<>(shoppingListItems);
        sharedPreferences.edit().putStringSet("shopping_list", itemsSet).apply();
    }
}