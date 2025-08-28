package com.example.myapp.achievements;

import java.util.List;

public class BadgeDefinition {
    public final String id;               // e.g., "workouts_milestone"
    public final String title;            // "Workouts Milestones"
    public final String description;      // shown once; level-specific copy optional
    public final int iconResId;           // R.drawable.ic_badge_workouts
    public final String metricKey;        // MetricKeys.WORKOUTS_COMPLETED
    public final List<Long> thresholds;   // e.g., [1, 10, 25, 50, 100]

    public BadgeDefinition(String id, String title, String description, int iconResId, String metricKey, List<Long> thresholds) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.metricKey = metricKey;
        this.thresholds = thresholds;
    }
}
