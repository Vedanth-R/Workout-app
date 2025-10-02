package com.PeakForm.workoutapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.PeakForm.workoutapp.model.Food;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** UI state holder for Home. */
public class HomeViewModel extends AndroidViewModel {

    /** Immutable snapshot of everything the Home screen needs. */
    public static final class HomeUiState {
        public final String greeting;
        public final String date;
        public final int workoutsThisWeek;
        public final int dailyStreak;
        public final int caloriesToday;
        public final List<Integer> last7DaysCalories;

        public HomeUiState(String greeting, String date, int workoutsThisWeek, int dailyStreak,
                           int caloriesToday, List<Integer> last7DaysCalories) {
            this.greeting = greeting;
            this.date = date;
            this.workoutsThisWeek = workoutsThisWeek;
            this.dailyStreak = dailyStreak;
            this.caloriesToday = caloriesToday;
            this.last7DaysCalories = last7DaysCalories;
        }
    }

    private static final String NUTRITION_PREFS = "nutrition_prefs";
    private static final String KEY_LAST_UPDATED_YMD = "nutrition_lastUpdatedYmd";


    private final MutableLiveData<HomeUiState> state = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application app) {
        super(app);
        // Seed immediately so first frame has real values (no flicker)
        state.setValue(buildState(app.getApplicationContext()));
    }

    public LiveData<HomeUiState> getState() {
        return state;
    }

    /** Call when returning from Timer or whenever you want fresh numbers. */
    public void refresh() {
        state.setValue(buildState(getApplication().getApplicationContext()));
    }

    // --------------------- Builders ---------------------

    private HomeUiState buildState(Context ctx) {

        NutritionRollover.ensureDayRollover(ctx);

        String greeting = greetingForNow();
        String date = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(new Date());
        int weekly = Prefs.getWorkoutsThisWeek(ctx);
        int daily = Prefs.getDailyStreak(ctx);
        int caloriesToday = computeCaloriesToday(ctx);
        List<Integer> last7 = loadLast7DaysCalories(ctx);
        return new HomeUiState(greeting, date, weekly, daily, caloriesToday, last7);
    }

    private String greetingForNow() {
        int hour = Integer.parseInt(new SimpleDateFormat("H", Locale.US).format(new Date()));
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }

    // --------------------- Nutrition helpers (moved from Fragment) ---------------------

    private int computeCaloriesToday(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(NUTRITION_PREFS, Context.MODE_PRIVATE);
        return getMealCalories("breakfast", prefs)
                + getMealCalories("lunch", prefs)
                + getMealCalories("dinner", prefs)
                + getMealCalories("snacks", prefs);
    }

    private int getMealCalories(String mealType, SharedPreferences prefs) {
        String json = prefs.getString(mealType + "_foods", null);
        if (json == null) return 0;

        Gson gson = new Gson();
        Type type = new TypeToken<List<Food>>() {}.getType();
        List<Food> foods = gson.fromJson(json, type);

        int total = 0;
        for (Food f : foods) total += f.getCalories();
        return total;
    }

    private List<Integer> loadLast7DaysCalories(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(NutritionFragment.PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(NutritionFragment.KEY_LAST_7_DAYS, "{}"); // Map<String, Integer>
        Map<String, Integer> dailyCalories =
                new Gson().fromJson(json, new TypeToken<Map<String, Integer>>() {}.getType());
        if (dailyCalories == null) dailyCalories = new java.util.HashMap<>();

        // Format today
        String todayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 🔑 Always recalc today's calories live
        int caloriesToday = computeCaloriesToday(ctx);
        dailyCalories.put(todayKey, caloriesToday);

        // Save back so NutritionFragment stays in sync
        prefs.edit()
                .putString(NutritionFragment.KEY_LAST_7_DAYS, new Gson().toJson(dailyCalories))
                .apply();

        // Build last 7 days list
        List<Integer> last7Days = new ArrayList<>(7);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6); // 6 days ago
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            String key = sdf.format(cal.getTime());
            Integer value = dailyCalories.get(key);
            last7Days.add(value != null ? value : 0);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        return last7Days;
    }

    @SuppressWarnings("ConstantConditions")
    private void ensureNutritionDayRollover(Context ctx) {
        // Date keys
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        SharedPreferences meals = ctx.getSharedPreferences(NUTRITION_PREFS, Context.MODE_PRIVATE);
        String last = meals.getString(KEY_LAST_UPDATED_YMD, null);

        // First ever run: mark today and bail
        if (last == null) {
            meals.edit().putString(KEY_LAST_UPDATED_YMD, today).apply();
            return;
        }

        // Same day → nothing to do
        if (today.equals(last)) return;

        // Different day → commit "yesterday" (actually 'last') and reset today
        int yTotal = getMealCalories("breakfast", meals)
                + getMealCalories("lunch", meals)
                + getMealCalories("dinner", meals)
                + getMealCalories("snacks", meals);

        // Update the 7-day map under the correct date (last day), and zero today
        SharedPreferences mapPrefs = ctx.getSharedPreferences(NutritionFragment.PREFS_NAME, Context.MODE_PRIVATE);
        String json = mapPrefs.getString(NutritionFragment.KEY_LAST_7_DAYS, "{}");
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.Map<String, Integer>>() {}.getType();
        java.util.Map<String, Integer> daily = new com.google.gson.Gson().fromJson(json, type);
        if (daily == null) daily = new java.util.HashMap<>();

        // Write the actual last day's total and ensure today starts at 0
        daily.put(last, yTotal);
        daily.put(today, 0);

        mapPrefs.edit()
                .putString(NutritionFragment.KEY_LAST_7_DAYS, new com.google.gson.Gson().toJson(daily))
                .apply();

        // Clear meal lists so today starts clean
        meals.edit()
                .putString("breakfast_foods", "[]")
                .putString("lunch_foods", "[]")
                .putString("dinner_foods", "[]")
                .putString("snacks_foods", "[]")
                .putString(KEY_LAST_UPDATED_YMD, today)
                .apply();
    }

}
