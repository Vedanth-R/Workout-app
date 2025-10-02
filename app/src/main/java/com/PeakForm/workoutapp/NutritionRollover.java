package com.PeakForm.workoutapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.PeakForm.workoutapp.model.Food;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public final class NutritionRollover {
    private NutritionRollover() {}

    private static final String PREFS_NAME = NutritionFragment.PREFS_NAME;
    private static final String PREF_LAST_RESET = "last_reset_date";
    private static final String KEY_LAST_7_DAYS = NutritionFragment.KEY_LAST_7_DAYS;

    /** Call this before rendering nutrition or home stats. Safe to call multiple times. */
    public static void ensureDayRollover(Context ctx) {
        SharedPreferences meals = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastReset = meals.getString(PREF_LAST_RESET, null);

        // First run or fresh install: seed today and stamp reset date.
        if (lastReset == null) {
            seedTodayZero(ctx, today);
            meals.edit().putString(PREF_LAST_RESET, today).apply();
            return;
        }

        // Same day → nothing to do
        if (today.equals(lastReset)) return;

        // Compute yesterday's total BEFORE clearing
        int yTotal = getMealCalories("breakfast", meals)
                + getMealCalories("lunch", meals)
                + getMealCalories("dinner", meals)
                + getMealCalories("snacks", meals);

        // Merge into 7-day map without clobbering a real value with 0
        SharedPreferences mapPrefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = mapPrefs.getString(KEY_LAST_7_DAYS, "{}");
        Type type = new TypeToken<Map<String, Integer>>() {}.getType();
        Map<String, Integer> daily = new Gson().fromJson(json, type);
        if (daily == null) daily = new HashMap<>();

        // Non-destructive merge:
        int prev = daily.getOrDefault(lastReset, 0);
        daily.put(lastReset, Math.max(prev, yTotal));   // never overwrite a higher/real value

        // Ensure today key exists and starts at 0 (don't overwrite if something already wrote nonzero)
        daily.put(today, daily.getOrDefault(today, 0));

        mapPrefs.edit().putString(KEY_LAST_7_DAYS, new Gson().toJson(daily)).apply();

        // Clear meals so today starts clean, then stamp last reset
        clearMealLogs(meals);
        meals.edit().putString(PREF_LAST_RESET, today).apply();
    }

    private static void seedTodayZero(Context ctx, String today) {
        SharedPreferences mapPrefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = mapPrefs.getString(KEY_LAST_7_DAYS, "{}");
        Type type = new TypeToken<Map<String, Integer>>() {}.getType();
        Map<String, Integer> daily = new Gson().fromJson(json, type);
        if (daily == null) daily = new HashMap<>();
        if (!daily.containsKey(today)) {
            daily.put(today, 0);
            mapPrefs.edit().putString(KEY_LAST_7_DAYS, new Gson().toJson(daily)).apply();
        }
    }

    private static int getMealCalories(String mealType, SharedPreferences meals) {
        String json = meals.getString(mealType + "_foods", null);
        if (json == null) return 0;
        List<Food> foods = new Gson().fromJson(json, new TypeToken<List<Food>>() {}.getType());
        int total = 0;
        for (Food f : foods) total += f.getCalories();
        return total;
    }

    private static void clearMealLogs(SharedPreferences meals) {
        String[] keys = {"breakfast_foods", "lunch_foods", "dinner_foods", "snacks_foods"};
        SharedPreferences.Editor e = meals.edit();
        for (String k : keys) e.remove(k);
        e.apply();
    }
}
