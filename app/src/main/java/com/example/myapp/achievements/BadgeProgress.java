package com.example.myapp.achievements;

public class BadgeProgress {
    public final BadgeDefinition badge;
    public final long current;      // current metric value
    public final long target;       // next threshold
    public final int levelIndex;    // -1 if none reached yet; otherwise index of highest threshold reached
    public final boolean unlocked;  // true if current >= last threshold (badge “maxed”)
    public final boolean hasProgressTowardNext; // true if a next threshold exists

    public BadgeProgress(BadgeDefinition badge, long current, long target, int levelIndex, boolean unlocked, boolean hasProgressTowardNext) {
        this.badge = badge;
        this.current = current;
        this.target = target;
        this.levelIndex = levelIndex;
        this.unlocked = unlocked;
        this.hasProgressTowardNext = hasProgressTowardNext;
    }

    public float progressFraction() {
        if (!hasProgressTowardNext || target <= 0) return 1f;
        return Math.min(1f, (float) current / (float) target);
    }
}
