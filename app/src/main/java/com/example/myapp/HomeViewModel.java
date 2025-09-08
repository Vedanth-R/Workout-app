package com.example.myapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapp.model.Food;
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
        SharedPreferences prefs = ctx.getSharedPreferences("nutrition_prefs", Context.MODE_PRIVATE);
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
        // NutritionFragment.PREFS_NAME / KEY_LAST_7_DAYS should be public static
        SharedPreferences prefs = ctx.getSharedPreferences(NutritionFragment.PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(NutritionFragment.KEY_LAST_7_DAYS, "{}"); // Map<String, Integer>
        Map<String, Integer> dailyCalories =
                new Gson().fromJson(json, new TypeToken<Map<String, Integer>>() {}.getType());
        if (dailyCalories == null) dailyCalories = new java.util.HashMap<>();

        // Ensure today exists in the map (default 0)
        String todayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (!dailyCalories.containsKey(todayKey)) {
            dailyCalories.put(todayKey, 0);
            prefs.edit().putString(NutritionFragment.KEY_LAST_7_DAYS, new Gson().toJson(dailyCalories)).apply();
        }

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
}
