package com.example.myapp.achievements;

import com.example.myapp.R;
import java.util.Arrays;
import java.util.List;

public final class AchievementsConfig {
    private AchievementsConfig() {}

    public static List<BadgeDefinition> allBadges() {
        return Arrays.asList(
                new BadgeDefinition(
                        "workouts_milestone",
                        "Workouts",
                        "Milestones for total workouts completed",
                        R.drawable.ic_badge_workouts,
                        MetricKeys.WORKOUTS_COMPLETED,
                        Arrays.asList(1L, 10L, 25L, 50L, 100L, 250L, 500L)
                ),
                new BadgeDefinition(
                        "weight_milestone",
                        "Weight Lifted",
                        "Total weight lifted across all workouts (kg)",
                        R.drawable.ic_badge_weight,
                        MetricKeys.TOTAL_WEIGHT_LIFTED,
                        Arrays.asList(1000L, 5000L, 10000L, 25000L, 50000L, 100000L)
                ),
                new BadgeDefinition(
                        "reps_milestone",
                        "Total Reps",
                        "Milestones for cumulative reps",
                        R.drawable.ic_badge_reps,
                        MetricKeys.TOTAL_REPS,
                        Arrays.asList(100L, 500L, 1000L, 2500L, 5000L, 10000L)
                ),
                new BadgeDefinition(
                        "streak_milestone",
                        "Streak",
                        "Longest active-day streak milestones",
                        R.drawable.ic_badge_streak,
                        MetricKeys.LONGEST_STREAK,
                        Arrays.asList(3L, 7L, 14L, 30L, 60L, 100L)
                )
        );
    }

    // Only surface up to this many "in progress" badges (to reduce overload).
    public static final int IN_PROGRESS_LIMIT = 8;

    // Optional: cap visible unlocked badges (e.g., show recent/top N, add “See all”)
    public static final int UNLOCKED_VISIBLE_CAP = 12;
}
