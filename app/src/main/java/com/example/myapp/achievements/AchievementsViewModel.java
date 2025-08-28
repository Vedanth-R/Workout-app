package com.example.myapp.achievements;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.*;

public class AchievementsViewModel extends ViewModel {
    private final AchievementsRepository repo = new AchievementsRepository();
    private final AchievementEngine engine = new AchievementEngine();

    private final MediatorLiveData<List<BadgeProgress>> allProgress = new MediatorLiveData<>();
    public final LiveData<List<BadgeProgress>> inProgressLimited;
    public final LiveData<List<BadgeProgress>> unlockedCapped;
    public final LiveData<List<PersonalRecord>> personalRecords = repo.personalRecords();

    public AchievementsViewModel() {
        allProgress.addSource(repo.metrics(), metrics -> {
            List<BadgeProgress> evaluated = engine.evaluate(AchievementsConfig.allBadges(), metrics);
            allProgress.setValue(evaluated);
        });

        inProgressLimited = Transformations.map(allProgress, list -> {
            if (list == null) return Collections.emptyList();
            List<BadgeProgress> ip = engine.inProgress(list);
            int limit = Math.min(ip.size(), AchievementsConfig.IN_PROGRESS_LIMIT);
            return ip.subList(0, limit);
        });

        unlockedCapped = Transformations.map(allProgress, list -> {
            if (list == null) return Collections.emptyList();
            List<BadgeProgress> un = engine.unlocked(list);
            int cap = Math.min(un.size(), AchievementsConfig.UNLOCKED_VISIBLE_CAP);
            return un.subList(0, cap);
        });
    }
}
