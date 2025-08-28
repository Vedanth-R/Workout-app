package com.example.myapp.achievements;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.*;

public class AchievementsRepository {

    // In a real app, fetch from DB. Keeping it simple & replaceable.
    private final MutableLiveData<Map<String, Long>> metricsLive = new MutableLiveData<>();
    private final MutableLiveData<List<PersonalRecord>> prsLive = new MutableLiveData<>();

    public AchievementsRepository() {
        // Seed with placeholder; replace with real sources.
        Map<String, Long> seed = new HashMap<>();
        seed.put(MetricKeys.WORKOUTS_COMPLETED, 17L);
        seed.put(MetricKeys.TOTAL_WEIGHT_LIFTED, 13250L);
        seed.put(MetricKeys.TOTAL_REPS, 1800L);
        seed.put(MetricKeys.ACTIVE_DAYS, 22L);
        seed.put(MetricKeys.LONGEST_STREAK, 6L);
        metricsLive.setValue(seed);

        prsLive.setValue(Arrays.asList(
                new PersonalRecord("Bench Press", "maxWeight", 92.5f, new Date()),
                new PersonalRecord("Deadlift", "maxWeight", 140.0f, new Date()),
                new PersonalRecord("Pull Ups", "maxReps", 18f, new Date()),
                new PersonalRecord("Squat", "maxVolume", 1025f, new Date())
        ));
    }

    public LiveData<Map<String, Long>> metrics() { return metricsLive; }
    public LiveData<List<PersonalRecord>> personalRecords() { return prsLive; }

    // Call this from wherever your stats update
    public void updateMetrics(Map<String, Long> newMetrics) { metricsLive.setValue(newMetrics); }
    public void updatePRs(List<PersonalRecord> newPRs) { prsLive.setValue(newPRs); }
}
