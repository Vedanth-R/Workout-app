// AchievementsFragment.java
package com.example.myapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private TextView tvStreakValue, tvBenchPRValue, tvTotalWorkoutsValue, tvActiveDaysValue;
    private LinearLayout llOverview;

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

        // TODO: Hook into a ViewModel/Repository to load real user data & observe changes.
        // TODO: Implement "View All" navigation to full trophies screen.
        // TODO: Replace placeholder adapter layout with custom item_trophy layout (XML).
        // TODO: Add locked/unlocked visuals, progress, animations, and filtering (chips/tabs).
    }

    private void bindViews(@NonNull View root) {
        // Header
        tvHeaderTitle = root.findViewById(R.id.tvHeaderTitle);
        tvHeaderSubtitle = root.findViewById(R.id.tvHeaderSubtitle);

        // Overview
        llOverview = root.findViewById(R.id.llOverview);
        tvStreakValue = root.findViewById(R.id.tvStreakValue);
        tvBenchPRValue = root.findViewById(R.id.tvBenchPRValue);
        tvTotalWorkoutsValue = root.findViewById(R.id.tvTotalWorkoutsValue);
        tvActiveDaysValue = root.findViewById(R.id.tvActiveDaysValue);

        // Trophies
        rvTrophies = root.findViewById(R.id.rvTrophies);
        btnViewAllTrophies = root.findViewById(R.id.btnViewAllTrophies);
    }

    private void setupHeader() {
        // Minimal static content for now (can be dynamic later)
        if (tvHeaderTitle != null) tvHeaderTitle.setText("Trophies");
        if (tvHeaderSubtitle != null) tvHeaderSubtitle.setText("Keep progressing to unlock more");
    }

    private void setupOverview() {
        // Placeholder numbers for now; replace with real data later.
        // These IDs match the XML you added.

        // Get data from Prefs
        int total = Prefs.getTotalWorkouts(requireContext());
        int streak = Prefs.getWeeklyStreak(requireContext());
//        int bench = Prefs.getBenchPR(requireContext());
        int active = Prefs.getActiveDaysThisMonth(requireContext());

        if (tvStreakValue != null) tvStreakValue.setText(String.valueOf(streak));           // Weekly Streak (days)
        if (tvBenchPRValue != null) tvBenchPRValue.setText("0");       // Bench PR (lb)
        if (tvTotalWorkoutsValue != null) tvTotalWorkoutsValue.setText(String.valueOf(total));; // Total Workouts
        if (tvActiveDaysValue != null) tvActiveDaysValue.setText(String.valueOf(active));  // Active Days (This Month)

        // TODO: Optionally style cells differently (e.g., badges/emoji), or add click-throughs.
        // TODO: Pull these values from persistence (Room) or backend via ViewModel.
    }

    private void setupTrophiesList() {
        // Horizontal list, show exactly 3 items here
        rvTrophies.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        List<Trophy> sample = getSampleTrophiesLimited(3);
        trophiesAdapter = new TrophiesAdapter(sample);
        rvTrophies.setAdapter(trophiesAdapter);
    }

    private void setupClickListeners() {
        btnViewAllTrophies.setOnClickListener(v -> {
            // Placeholder behavior. We’ll implement navigation later.
            Toast.makeText(requireContext(), "View All Trophies (coming soon)", Toast.LENGTH_SHORT).show();

            // TODO: Navigate to a full trophies screen or expand list via sheet/dialog.
        });
    }

    // ----- Sample data for now -----
    private List<Trophy> getSampleTrophiesLimited(int limit) {
        List<Trophy> all = new ArrayList<>();
        all.add(new Trophy("First Workout", "Complete your first workout", true));
        all.add(new Trophy("10-Day Streak", "Work out 10 days in a row", false));
        all.add(new Trophy("Bench Milestone", "Hit 200 lb bench press", true));
        all.add(new Trophy("Consistency", "Log workouts 5 days this week", false));
        all.add(new Trophy("Early Bird", "Start a workout before 7am", false));

        if (limit <= 0 || limit >= all.size()) return all.subList(0, Math.min(3, all.size()));
        return all.subList(0, Math.min(limit, all.size()));
    }

    // ----- Simple model -----
    static class Trophy {
        final String title;
        final String description;
        final boolean unlocked;
        int current = 5;
        int total = 10;

        Trophy(String title, String description, boolean unlocked) {
            this.title = title;
            this.description = description;
            this.unlocked = unlocked;
        }
    }

    // ----- Minimal Adapter (uses a simple built-in layout as a placeholder) -----
    // Replace with a proper custom row layout (item_trophy.xml) later.
    private static class TrophiesAdapter extends RecyclerView.Adapter<TrophiesAdapter.Holder> {

        private final List<Trophy> items;

        TrophiesAdapter(List<Trophy> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Placeholder layout for now (two lines). Replace with item_trophy later.
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_trophy, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            Trophy t = items.get(position);


            h.title.setText(t.title);
            h.desc.setText(t.description);

            // Status / lock visuals
            if (t.unlocked) {
                h.status.setText("UNLOCKED");
                h.lock.setVisibility(View.GONE);
                h.progressContainer.setVisibility(View.GONE);
                // TODO: set colored icon for unlocked
                // h.icon.setImageResource(R.drawable.ic_badge_unlocked_variant);
                // Optionally tint: h.icon.setImageTintList(null);
            } else {
                h.status.setText("LOCKED");
                h.lock.setVisibility(View.VISIBLE);

                // If the trophy is incremental, show progress
                if (t.total > 0) {
                    h.progressContainer.setVisibility(View.VISIBLE);
                    int pct = (int) (100f * t.current / Math.max(1, t.total));
                    h.progress.setProgress(pct);
                    h.progressText.setText(t.current + " / " + t.total);
                } else {
                    h.progressContainer.setVisibility(View.GONE);
                }

                // TODO: set grey icon/tint for locked
                // h.icon.setImageResource(R.drawable.ic_badge_locked_variant);
                // ImageViewCompat.setImageTintList(h.icon, ColorStateList.valueOf(...));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            ImageView icon, lock;
            TextView title, desc, status, progressText;
            ProgressBar progress;
            View progressContainer;

            Holder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.ivTrophyIcon);
                lock = itemView.findViewById(R.id.ivLockOverlay);
                title = itemView.findViewById(R.id.tvTrophyTitle);
                desc = itemView.findViewById(R.id.tvTrophyDesc);
                status = itemView.findViewById(R.id.tvTrophyStatus);
                progress = itemView.findViewById(R.id.pbTrophyProgress);
                progressText = itemView.findViewById(R.id.tvTrophyProgressText);
                progressContainer = itemView.findViewById(R.id.layoutProgress);
            }
        }
    }
}
