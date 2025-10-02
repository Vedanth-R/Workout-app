// AchievementsFragment.java
package com.PeakForm.workoutapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.PeakForm.workoutapp.data.TrophyRepository;
import com.PeakForm.workoutapp.model.Trophy;
import com.PeakForm.workoutapp.trophies.TrophiesAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic Achievements/Trophies page.
 * - Inflates fragment_achievements.xml
 * - Wires header, overview numbers, trophies RecyclerView (shows 3 items)
 * - Sets up "View All" click (placeholder Toast)
 *
 * Advanced features are left as TODOs so we can iterate later.
 */
public class AchievementsFragment extends Fragment {

    // Header
    private TextView tvHeaderTitle;
    private TextView tvHeaderSubtitle;

    // Overview
    private TextView tvStreakValue, tvTrophiesUnlockedValue, tvTotalWorkoutsValue, tvActiveDaysValue;

    // Trophies
    private RecyclerView rvTrophies;
    private TextView btnViewAllTrophies;
    private TrophiesAdapter trophiesAdapter;

    public AchievementsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_achievements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        bindViews(root);
        setupHeader();
        setupOverview();
        setupTrophiesList();
        setupClickListeners();

        // TODO: Add locked/unlocked visuals, progress, animations, and filtering (chips/tabs).
    }

    private void bindViews(@NonNull View root) {
        // Header
        tvHeaderTitle = root.findViewById(R.id.tvHeaderTitle);
        tvHeaderSubtitle = root.findViewById(R.id.tvHeaderSubtitle);

        // Overview
        tvStreakValue = root.findViewById(R.id.tvStreakValue);
        tvTrophiesUnlockedValue = root.findViewById(R.id.tvTrophiesUnlockedValue);
        tvTotalWorkoutsValue = root.findViewById(R.id.tvTotalWorkoutsValue);
        tvActiveDaysValue = root.findViewById(R.id.tvActiveDaysValue);

        // Trophies
        rvTrophies = root.findViewById(R.id.rvTrophies);
        btnViewAllTrophies = root.findViewById(R.id.btnViewAllTrophies);
    }

    private void setupHeader() {
        // Minimal static content for now (can be dynamic later)
        if (tvHeaderTitle != null) tvHeaderTitle.setText(R.string.trophies_fragment_title);
        if (tvHeaderSubtitle != null) tvHeaderSubtitle.setText(R.string.trophies_fragment_subtitle);
    }

    private void setupOverview() {

        // Get data from Prefs
        int total = Prefs.getTotalWorkouts(requireContext());
        int streak = Prefs.getWeeklyStreak(requireContext());
        int active = Prefs.getActiveDaysThisMonth(requireContext());

        if (tvStreakValue != null) tvStreakValue.setText(String.valueOf(streak));           // Weekly Streak (days)
        if (tvTotalWorkoutsValue != null) tvTotalWorkoutsValue.setText(String.valueOf(total));; // Total Workouts
        if (tvActiveDaysValue != null) tvActiveDaysValue.setText(String.valueOf(active));  // Active Days (This Month)


        if (tvTrophiesUnlockedValue != null) {
            int unlocked = TrophyRepository.getInstance(requireContext()).getUnlockedCount();
            tvTrophiesUnlockedValue.setText(String.valueOf(unlocked));
        }
    }

    private void setupTrophiesList() {
        rvTrophies.setLayoutManager(
        	new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        List<Trophy> all = TrophyRepository.getInstance(requireContext()).getAll();
        trophiesAdapter = new TrophiesAdapter(new ArrayList<>(all));
        rvTrophies.setAdapter(trophiesAdapter);
    }


    private void setupClickListeners() {
        btnViewAllTrophies.setOnClickListener(v -> {
            // Placeholder behavior. We’ll implement navigation later.
            Toast.makeText(requireContext(), "View All Trophies (coming soon)", Toast.LENGTH_SHORT).show();

            // TODO: Navigate to a full trophies screen or expand list via sheet/dialog.
        });
    }

    private final TrophyRepository.TrophyUnlockListener unlockListener = unlockedId -> {
        if (!isAdded()) return; // fragment not visible

        requireActivity().runOnUiThread(() -> {
            // 1) refresh data from repo
            List<Trophy> fresh = TrophyRepository.getInstance(requireContext()).getAll();
            trophiesAdapter.setItems(fresh);

            if (tvTrophiesUnlockedValue != null) {
                int unlocked = TrophyRepository.getInstance(requireContext()).getUnlockedCount();
                tvTrophiesUnlockedValue.setText(String.valueOf(unlocked));
            }


            // 2) find index and scroll, then animate after scroll settles
            int idx = trophiesAdapter.indexOf(unlockedId);
            if (idx >= 0) {
                trophiesAdapter.setRecentlyUnlocked(unlockedId);
                rvTrophies.smoothScrollToPosition(idx);

                // small delay so the row is laid out before we rebind/animate
                rvTrophies.postDelayed(() -> {
                    trophiesAdapter.notifyItemChanged(idx);
                }, 150);
            }
        });
    };

    @Override public void onStart() {
        super.onStart();
        TrophyRepository.getInstance(requireContext()).addListener(unlockListener);
    }

    @Override public void onStop() {
        TrophyRepository.getInstance(requireContext()).removeListener(unlockListener);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupOverview(); // refresh stats when returning
    }

}
