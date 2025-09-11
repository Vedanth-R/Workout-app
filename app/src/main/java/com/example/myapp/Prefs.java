package com.example.myapp;


import android.content.Context;
import android.content.SharedPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class Prefs {


    // Keys
    static final String KEY_TOTAL_WORKOUTS = "total_workouts";
    private static final String KEY_WORKOUTS_THIS_WEEK = "workouts_this_week";
    private static final String KEY_WEEK_START_YMD = "week_start_ymd"; // "yyyyMMdd" for the start of this week (Sunday)

    private static final String KEY_LAST_WEEK_WITH_WORKOUT_YMD = "last_week_with_workout_ymd";
    private static final String KEY_WEEKLY_STREAK = "weekly_streak";

    private static final String KEY_ACTIVE_MONTH = "active_month"; // "yyyyMM"
    private static final String KEY_ACTIVE_DAYS_SET = "active_days_set"; // Set<String> of "yyyyMMdd"

    public static final String KEY_STREAK_COUNT = "dailyStreak";
    private static final String KEY_LAST_LOGGED_YMD = "lastLoggedYmd";

    public static final String KEY_DAILY_STREAK = "dailyStreak";
    public static final String KEY_LAST_DAY_WITH_WORKOUT_YMD = "lastDayWithWorkoutYmd";



    private Prefs() {}

    private static SharedPreferences sp(Context ctx) {
        return ctx.getSharedPreferences("train_prefs", Context.MODE_PRIVATE);
    }

    /* -------------------- Total Workouts -------------------- */

    /*public static int getTotalWorkouts(Context ctx) {
        return sp(ctx).getInt(KEY_TOTAL_WORKOUTS, 0);
    }*/

    public static void incrementTotalWorkouts(Context ctx) {
        SharedPreferences s = sp(ctx);
        int current = s.getInt(KEY_TOTAL_WORKOUTS, 0);
        s.edit().putInt(KEY_TOTAL_WORKOUTS, current + 1).apply();
    }

    /* -------------------- Workouts This Week -------------------- */

    public static int getWorkoutsThisWeek(Context ctx) {
        SharedPreferences s = sp(ctx);
        String currentWeek = currentWeekStartYmd();
        String storedWeek = s.getString(KEY_WEEK_START_YMD, "");
        if (!currentWeek.equals(storedWeek)) return 0; // week rolled over
        return s.getInt(KEY_WORKOUTS_THIS_WEEK, 0);
    }

    public static void incrementWorkoutsThisWeek(Context ctx) {
        SharedPreferences s = sp(ctx);
        String currentWeek = currentWeekStartYmd(); // you already have this
        String storedWeek = s.getString(KEY_WEEK_START_YMD, "");
        int count = s.getInt(KEY_WORKOUTS_THIS_WEEK, 0);

        if (!currentWeek.equals(storedWeek)) {
            storedWeek = currentWeek;
            count = 0;
        }
        count += 1;

        s.edit()
                .putString(KEY_WEEK_START_YMD, storedWeek)
                .putInt(KEY_WORKOUTS_THIS_WEEK, count)
                .apply();
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

//    public static int getWeeklyStreak(Context ctx) {
//        return sp(ctx).getInt(KEY_WEEKLY_STREAK, 0);
//    }

    public static int getActiveDaysThisMonth(Context ctx) {
        SharedPreferences s = sp(ctx);
        String currentMonth = currentMonthId();
        String storedMonth = s.getString(KEY_ACTIVE_MONTH, "");
        if (!currentMonth.equals(storedMonth)) return 0;
        return safeSizeOfStringSet(s.getStringSet(KEY_ACTIVE_DAYS_SET, null));
    }

// ----------------------- Mutations on workout save -----------------------

    /** Weekly streak only. No daily logic here. */
    public static void updateWeeklyStreakOnWorkout(Context ctx) {
        SharedPreferences s = sp(ctx);

        String currentWeekStart = currentWeekStartYmd();
        String lastWeekStart = s.getString(KEY_LAST_WEEK_WITH_WORKOUT_YMD, "");
        int weeklyStreak = s.getInt(KEY_WEEKLY_STREAK, 0);

        if (currentWeekStart.equals(lastWeekStart)) {
            // Already counted a workout for this week → nothing to do
            return;
        }

        if (lastWeekStart.isEmpty()) {
            weeklyStreak = 1;
        } else {
            // If current week is exactly the next week after last → continue streak
            String expectedNextWeek = addDaysYmd(lastWeekStart, 7);
            if (expectedNextWeek.equals(currentWeekStart)) {
                weeklyStreak += 1;
            } else {
                // Gap of >= 1 empty week → reset streak
                weeklyStreak = 1;
            }
        }

        s.edit()
                .putString(KEY_LAST_WEEK_WITH_WORKOUT_YMD, currentWeekStart)
                .putInt(KEY_WEEKLY_STREAK, weeklyStreak)
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

    // DAILY HOMEPAGE STREAK //
    private static String ymd(Date d) {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(d);
    }

    private static Date parseYmd(String ymd) {
        try {
            return new SimpleDateFormat("yyyyMMdd", Locale.US).parse(ymd);
        } catch (ParseException e) {
            return null;
        }
    }

    private static boolean isYesterday(String lastYmd, String todayYmd) {
        if (lastYmd == null) return false;
        Date last = parseYmd(lastYmd);
        Date today = parseYmd(todayYmd);
        if (last == null || today == null) return false;

        Calendar c = Calendar.getInstance();
        c.setTime(last);
        c.add(Calendar.DAY_OF_YEAR, 1);
        String dayAfterLast = ymd(c.getTime());
        return dayAfterLast.equals(todayYmd);
    }


    /** Read without modifying. */
    public static int getCurrentStreak(Context ctx) {
        return sp(ctx).getInt(KEY_STREAK_COUNT, 0);
    }


    // REWORK

    public static void onWorkoutConfirmed(Context ctx) {
        SharedPreferences s = sp(ctx);

        // Per-session counters
        int total = s.getInt(KEY_TOTAL_WORKOUTS, 0) + 1;
        s.edit().putInt(KEY_TOTAL_WORKOUTS, total).apply();

        incrementWorkoutsThisWeek(ctx);          // per-session weekly counter

        // Weekly streak (does not touch daily)
        updateWeeklyStreakOnWorkout(ctx);

        // Daily streak, idempotent per calendar day
        String today = ymd(new Date());
        String lastDay = s.getString(KEY_LAST_DAY_WITH_WORKOUT_YMD, "");
        if (!today.equals(lastDay)) {
            int daily = s.getInt(KEY_DAILY_STREAK, 0);
            String expectedYesterday = addDaysYmd(today, -1);
            daily = expectedYesterday.equals(lastDay) ? Math.max(1, daily + 1) : 1;
            android.util.Log.d("Prefs", "Daily streak updated to " + daily + " on " + today);
            s.edit()
                    .putString(KEY_LAST_DAY_WITH_WORKOUT_YMD, today)
                    .putInt(KEY_DAILY_STREAK, daily)
                    .apply();

            // Active day marking belongs with daily logic
            markActiveDayThisMonth(ctx);
        }
    }



    // Convenience readers for UI:
    public static int getDailyStreak(Context ctx) {
        return sp(ctx).getInt(KEY_DAILY_STREAK, 0);
    }
    public static int getWeeklyStreak(Context ctx) {
        return sp(ctx).getInt(KEY_WEEKLY_STREAK, 0);
    }
    public static int getTotalWorkouts(Context ctx) {
        return sp(ctx).getInt(KEY_TOTAL_WORKOUTS, 0);
    }

}