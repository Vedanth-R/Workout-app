package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public final class Prefs {
    private static final String FILE = "app_prefs";

    // Keys
    private static final String KEY_TOTAL_WORKOUTS = "total_workouts";
    private static final String KEY_WORKOUTS_THIS_WEEK = "workouts_this_week";
    private static final String KEY_WEEK_START_YMD = "week_start_ymd"; // "yyyyMMdd" for the start of this week (Sunday)

    private Prefs() {}

    private static SharedPreferences sp(Context ctx) {
        return ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /* -------------------- Total Workouts -------------------- */

    public static int getTotalWorkouts(Context ctx) {
        return sp(ctx).getInt(KEY_TOTAL_WORKOUTS, 0);
    }

    public static void incrementTotalWorkouts(Context ctx) {
        SharedPreferences s = sp(ctx);
        int current = s.getInt(KEY_TOTAL_WORKOUTS, 0);
        s.edit().putInt(KEY_TOTAL_WORKOUTS, current + 1).apply();
    }

    /* -------------------- Workouts This Week -------------------- */

    public static int getWorkoutsThisWeek(Context ctx) {
        SharedPreferences s = sp(ctx);
        String storedWeekStart = s.getString(KEY_WEEK_START_YMD, "");
        String currentWeekStart = currentWeekStartYmd();

        // If the stored week is stale (or empty), treat current count as 0 for this week.
        if (!currentWeekStart.equals(storedWeekStart)) {
            return 0;
        }
        return s.getInt(KEY_WORKOUTS_THIS_WEEK, 0);
    }

    public static void incrementWorkoutsThisWeek(Context ctx) {
        SharedPreferences s = sp(ctx);
        String currentWeekStart = currentWeekStartYmd();
        String storedWeekStart = s.getString(KEY_WEEK_START_YMD, "");

        if (!currentWeekStart.equals(storedWeekStart)) {
            // New week just started → reset to 1 and update the anchor
            s.edit()
                    .putString(KEY_WEEK_START_YMD, currentWeekStart)
                    .putInt(KEY_WORKOUTS_THIS_WEEK, 1)
                    .apply();
        } else {
            int current = s.getInt(KEY_WORKOUTS_THIS_WEEK, 0);
            s.edit().putInt(KEY_WORKOUTS_THIS_WEEK, current + 1).apply();
        }
    }

    /* -------------------- Week boundary (Sunday start) -------------------- */

    /**
     * Returns "yyyyMMdd" for the SUNDAY that begins the current week
     * in the device's local timezone. This is simple, reliable, and fast.
     */
    private static String currentWeekStartYmd() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault(), Locale.US);
        cal.setFirstDayOfWeek(Calendar.SUNDAY);      // US-style week
        // zero-out time to midnight
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int dow = cal.get(Calendar.DAY_OF_WEEK);     // 1..7 (Sun..Sat)
        int delta = (dow - cal.getFirstDayOfWeek() + 7) % 7;
        cal.add(Calendar.DAY_OF_MONTH, -delta);      // back up to Sunday

        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(cal.getTime());
    }
}
