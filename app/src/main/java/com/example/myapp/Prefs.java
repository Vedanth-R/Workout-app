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

    private static final String KEY_LAST_WEEK_WITH_WORKOUT_YMD = "last_week_with_workout_ymd";
    private static final String KEY_WEEKLY_STREAK = "weekly_streak";

    private static final String KEY_ACTIVE_MONTH = "active_month"; // "yyyyMM"
    private static final String KEY_ACTIVE_DAYS_SET = "active_days_set"; // Set<String> of "yyyyMMdd"


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
    /*private static String currentWeekStartYmd() {
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
    }*/

    // ======================= Weekly Streak & Active Days =======================
    /*
     * Weekly Streak:
     * - We store:
     *   - lastWeekWithWorkoutYmd: "yyyyMMdd" for the week’s Sunday (or Monday if you switch)
     *   - weeklyStreak: current streak length (int)
     * - On each workout save:
     *   - If it’s the same week as last → no change (already counted)
     *   - If it’s exactly the next week → streak++
     *   - If there’s a gap (>= 1 empty week) → streak = 1
     *
     * Active Days This Month:
     * - We store:
     *   - activeMonth: "yyyyMM" (e.g., 202508)
     *   - activeDaysSet: Set<String> of day keys "yyyyMMdd" for that month
     * - On each workout save:
     *   - If month changed → reset set to empty + set new month id
     *   - Add today’s yyyyMMdd to set (a set prevents duplicates)
     */

// ----------------------- Public getters -----------------------

    public static int getWeeklyStreak(Context ctx) {
        return sp(ctx).getInt(KEY_WEEKLY_STREAK, 0);
    }

    public static int getActiveDaysThisMonth(Context ctx) {
        SharedPreferences s = sp(ctx);
        String currentMonth = currentMonthId();
        String storedMonth = s.getString(KEY_ACTIVE_MONTH, "");
        if (!currentMonth.equals(storedMonth)) return 0;
        return safeSizeOfStringSet(s.getStringSet(KEY_ACTIVE_DAYS_SET, null));
    }

// ----------------------- Mutations on workout save -----------------------

    /** Call this ONCE when a workout is finally saved/confirmed. */
    public static void updateWeeklyStreakOnWorkout(Context ctx) {
        SharedPreferences s = sp(ctx);

        String currentWeekStart = currentWeekStartYmd();
        String lastWeekStart = s.getString(KEY_LAST_WEEK_WITH_WORKOUT_YMD, "");
        int streak = s.getInt(KEY_WEEKLY_STREAK, 0);

        if (currentWeekStart.equals(lastWeekStart)) {
            // Already counted a workout for this week → nothing to do
            return;
        }

        if (lastWeekStart.isEmpty()) {
            // First ever week
            streak = 1;
        } else {
            // If current week is exactly the next week after last → continue streak
            String expectedNextWeek = addDaysYmd(lastWeekStart, 7);
            if (expectedNextWeek.equals(currentWeekStart)) {
                streak += 1;
            } else {
                // Gap of >= 1 empty week → reset streak
                streak = 1;
            }
        }

        s.edit()
                .putString(KEY_LAST_WEEK_WITH_WORKOUT_YMD, currentWeekStart)
                .putInt(KEY_WEEKLY_STREAK, streak)
                .apply();
    }

    /** Call this ONCE when a workout is finally saved/confirmed. */
    public static void markActiveDayThisMonth(Context ctx) {
        SharedPreferences s = sp(ctx);

        String today = todayYmd();
        String currentMonth = today.substring(0, 6); // yyyyMM
        String storedMonth = s.getString(KEY_ACTIVE_MONTH, "");

        // Copy the set to avoid editing the live reference returned by getStringSet()
        java.util.Set<String> days = new java.util.HashSet<>();
        java.util.Set<String> existing = s.getStringSet(KEY_ACTIVE_DAYS_SET, null);
        if (existing != null) days.addAll(existing);

        if (!currentMonth.equals(storedMonth)) {
            // New month → reset the set
            days.clear();
        }

        days.add(today);

        s.edit()
                .putString(KEY_ACTIVE_MONTH, currentMonth)
                .putStringSet(KEY_ACTIVE_DAYS_SET, days)
                .apply();
    }

// ----------------------- Date helpers -----------------------

    /** Sunday-start week. To use Monday-start weeks, change SUNDAY to MONDAY. */
    private static String currentWeekStartYmd() {
        java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getDefault(), java.util.Locale.US);
        cal.setFirstDayOfWeek(java.util.Calendar.SUNDAY);  // switch to MONDAY if you prefer ISO-style weeks
        zeroTime(cal);
        int dow = cal.get(java.util.Calendar.DAY_OF_WEEK); // 1..7 (Sun..Sat)
        int delta = (dow - cal.getFirstDayOfWeek() + 7) % 7;
        cal.add(java.util.Calendar.DAY_OF_MONTH, -delta);
        return new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(cal.getTime());
    }

    private static String todayYmd() {
        java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getDefault(), java.util.Locale.US);
        zeroTime(cal);
        return new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(cal.getTime());
    }

    private static String currentMonthId() {
        java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getDefault(), java.util.Locale.US);
        zeroTime(cal);
        return new java.text.SimpleDateFormat("yyyyMM", java.util.Locale.US).format(cal.getTime());
    }

    private static void zeroTime(java.util.Calendar cal) {
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
    }

    /** Adds 'days' to a yyyyMMdd string and returns a yyyyMMdd string. */
    private static String addDaysYmd(String ymd, int days) {
        try {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US);
            java.util.Date d = fmt.parse(ymd);
            java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getDefault(), java.util.Locale.US);
            cal.setTime(d);
            zeroTime(cal);
            cal.add(java.util.Calendar.DAY_OF_MONTH, days);
            return fmt.format(cal.getTime());
        } catch (Exception e) {
            // If something goes wrong, fall back to current week behavior
            return currentWeekStartYmd();
        }
    }

    private static int safeSizeOfStringSet(java.util.Set<String> set) {
        return (set == null) ? 0 : set.size();
    }

}