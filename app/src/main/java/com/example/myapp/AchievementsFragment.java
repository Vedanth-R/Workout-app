package com.example.myapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AchievementsFragment extends Fragment {

    private RecyclerView rvAchievements;
    private TextView tvTotalAchievements;
    private Integer numAchievements = 7;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements, container, false);

        rvAchievements = view.findViewById(R.id.rvAchievements);
        tvTotalAchievements = view.findViewById(R.id.tvTotalAchievements);

        setupAchievements();

        String achievements = getString(R.string.nav_achievements) + ": " + numAchievements;
        tvTotalAchievements.setText(achievements);


        return view;
    }

    private void setupAchievements() {
        // Dummy achievements data
        List<Achievement> achievements = new ArrayList<>();
        achievements.add(new Achievement("First Workout", "Complete your first workout", "🏋️", true));
        achievements.add(new Achievement("Week Warrior", "Work out 7 days in a row", "🔥", true));
        achievements.add(new Achievement("Goal Setter", "Set your first fitness goal", "🎯", true));
        achievements.add(new Achievement("Nutrition Tracker", "Log meals for 3 days", "🥗", false));
        achievements.add(new Achievement("Consistency King", "Work out for 30 days", "👑", false));

        tvTotalAchievements.setText("Achievements: " + achievements.size());

        // TODO: Create and set up achievements adapter
        // For now, just show the count
    }

    private static class Achievement {
        String title;
        String description;
        String icon;
        boolean unlocked;

        Achievement(String title, String description, String icon, boolean unlocked) {
            this.title = title;
            this.description = description;
            this.icon = icon;
            this.unlocked = unlocked;
        }
    }
}
