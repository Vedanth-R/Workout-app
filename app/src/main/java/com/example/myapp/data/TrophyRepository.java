package com.example.myapp.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapp.model.Trophy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TrophyRepository {

    private static final String SP_NAME = "trophies_prefs";

    // Trophy IDs
    public static final String ID_EXPLORER     = "explorer";
    public static final String ID_FIRST_STEPS  = "first_steps";
    public static final String ID_PLANNER      = "planner";
    public static final String ID_TIMER_ROOKIE = "timer_rookie";

    // Keys
    private static final String K_DONE_PREFIX   = "trophy_done_";
    private static final String K_CUR_PREFIX    = "trophy_cur_";

    // Explorer-specific
    private static final String K_TABS_VISITED  = "tabs_visited"; // StringSet
    // First Steps-specific
    private static final String K_DAYS_OPENED   = "days_opened";  // StringSet of yyyy-MM-dd
    private static final int FIRST_STEPS_TOTAL  = 3;

    // Explorer required tabs (must match your BottomNav destinations)
    private static final Set<String> REQUIRED_TABS = new HashSet<>();
    static {
        REQUIRED_TABS.add("workouts");
        REQUIRED_TABS.add("nutrition");
        REQUIRED_TABS.add("home");
        REQUIRED_TABS.add("timer");
        REQUIRED_TABS.add("achievements");
    }

    private static TrophyRepository INSTANCE;

    private final SharedPreferences sp;
    private final SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private TrophyRepository(Context ctx) {
        sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TrophyRepository getInstance(Context ctx) {
        if (INSTANCE == null) INSTANCE = new TrophyRepository(ctx.getApplicationContext());
        return INSTANCE;
    }

    // ---------- Public API ----------

    /** Returns all trophies with live progress from prefs. */
    public List<Trophy> getAll() {
        List<Trophy> list = new ArrayList<>();
        list.add(buildExplorer());
        list.add(buildFirstSteps());
        list.add(buildPlanner());
        list.add(buildTimerRookie());
        return list;
    }

    /** Convenience: count unlocked (useful for an Overview cell). */
    public int getUnlockedCount() {
        int c = 0;
        for (Trophy t : getAll()) if (t.isUnlocked()) c++;
        return c;
    }

    // ---- Event hooks (call these from screens) ----

    /** Call on each BottomNav select with one of: workouts|nutrition|home|timer|achievements */
    public void onTabVisited(String tabKey) {
        if (!REQUIRED_TABS.contains(tabKey)) return;
        Set<String> visited = new HashSet<>(sp.getStringSet(K_TABS_VISITED, new HashSet<>()));
        if (visited.add(tabKey)) {
            sp.edit().putStringSet(K_TABS_VISITED, visited).apply();
        }
        // Auto-complete if all visited
        if (visited.containsAll(REQUIRED_TABS)) {
            unlock(ID_EXPLORER);
        }
    }

    /** Call in onResume of MainActivity (or Application onStart) to record today. */
    public void onAppOpenedToday() {
        Set<String> days = new HashSet<>(sp.getStringSet(K_DAYS_OPENED, new HashSet<>()));
        String today = dayFmt.format(new Date());
        if (days.add(today)) {
            sp.edit().putStringSet(K_DAYS_OPENED, days).apply();
        }
        // Progress update for First Steps
        int cur = Math.min(days.size(), FIRST_STEPS_TOTAL);
        sp.edit().putInt(K_CUR_PREFIX + ID_FIRST_STEPS, cur).apply();
        if (cur >= FIRST_STEPS_TOTAL) unlock(ID_FIRST_STEPS);
    }

    /** Call when a routine is successfully created/saved. */
    public void onRoutineCreated() {
        unlock(ID_PLANNER);
    }

    /** Call when the user starts a timer for the first time. */
    public void onTimerStarted() {
        unlock(ID_TIMER_ROOKIE);
    }

    // ---------- Builders ----------

    private Trophy buildExplorer() {
        Set<String> visited = sp.getStringSet(K_TABS_VISITED, new HashSet<>());
        int cur = visited.size();
        int total = REQUIRED_TABS.size();

        Trophy t = new Trophy(
                ID_EXPLORER,
                "Explorer",
                "Visit all 5 tabs at least once",
                Trophy.Type.INCREMENTAL
        );
        t.setTotal(total);
        t.setCurrent(cur);
        t.setUnlocked(isDone(ID_EXPLORER) || cur >= total);
        // If user already qualifies, mark done
        if (t.isUnlocked() && !isDone(ID_EXPLORER)) unlock(ID_EXPLORER);
        return t;
    }

    private Trophy buildFirstSteps() {
        Set<String> days = sp.getStringSet(K_DAYS_OPENED, new HashSet<>());
        int cur = Math.min(days.size(), FIRST_STEPS_TOTAL);

        Trophy t = new Trophy(
                ID_FIRST_STEPS,
                "First Steps",
                "Open the app on 3 different days",
                Trophy.Type.INCREMENTAL
        );
        t.setTotal(FIRST_STEPS_TOTAL);
        t.setCurrent(cur);
        t.setUnlocked(isDone(ID_FIRST_STEPS) || cur >= FIRST_STEPS_TOTAL);
        if (t.isUnlocked() && !isDone(ID_FIRST_STEPS)) unlock(ID_FIRST_STEPS);
        return t;
    }

    private Trophy buildPlanner() {
        Trophy t = new Trophy(
                ID_PLANNER,
                "Planner",
                "Create your first routine",
                Trophy.Type.BINARY
        );
        t.setUnlocked(isDone(ID_PLANNER));
        return t;
    }

    private Trophy buildTimerRookie() {
        Trophy t = new Trophy(
                ID_TIMER_ROOKIE,
                "Timer Rookie",
                "Start your first timer",
                Trophy.Type.BINARY
        );
        t.setUnlocked(isDone(ID_TIMER_ROOKIE));
        return t;
    }

    // ---------- Persistence helpers ----------

    private boolean isDone(String id) {
        return sp.getBoolean(K_DONE_PREFIX + id, false);
    }

    private void unlock(String id) {
        if (!isDone(id)) {
            sp.edit()
                    .putBoolean(K_DONE_PREFIX + id, true)
                    .apply();
        }
    }
}
