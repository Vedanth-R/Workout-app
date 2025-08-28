package com.example.myapp.achievements;

import java.util.*;

public class AchievementEngine {

    public List<BadgeProgress> evaluate(List<BadgeDefinition> defs, Map<String, Long> metrics) {
        List<BadgeProgress> results = new ArrayList<>();
        for (BadgeDefinition def : defs) {
            long current = metrics.getOrDefault(def.metricKey, 0L);
            int levelIndex = -1;
            for (int i = 0; i < def.thresholds.size(); i++) {
                if (current >= def.thresholds.get(i)) levelIndex = i;
                else break;
            }
            boolean maxed = (levelIndex == def.thresholds.size() - 1);
            long target = 0L;
            boolean hasNext = false;
            if (!maxed) {
                int next = Math.max(levelIndex + 1, 0);
                target = def.thresholds.get(next);
                hasNext = true;
            }
            results.add(new BadgeProgress(def, current, target, levelIndex, maxed, hasNext));
        }
        return results;
    }

    public List<BadgeProgress> inProgress(List<BadgeProgress> all) {
        List<BadgeProgress> list = new ArrayList<>();
        for (BadgeProgress p : all) {
            if (!p.unlocked && p.hasProgressTowardNext) list.add(p);
        }
        // Sort by % to next threshold descending (closest to completion first)
        list.sort((a,b) -> Float.compare(b.progressFraction(), a.progressFraction()));
        return list;
    }

    public List<BadgeProgress> unlocked(List<BadgeProgress> all) {
        List<BadgeProgress> list = new ArrayList<>();
        for (BadgeProgress p : all) if (p.levelIndex >= 0) list.add(p);
        // Sort by level high→low, then current value desc
        list.sort((a,b) -> {
            int cmp = Integer.compare(b.levelIndex, a.levelIndex);
            if (cmp != 0) return cmp;
            return Long.compare(b.current, a.current);
        });
        return list;
    }
}
