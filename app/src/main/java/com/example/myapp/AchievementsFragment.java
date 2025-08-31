// AchievementsFragment.java
package com.example.myapp;

import android.content.res.ColorStateList;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// NEW
import com.example.myapp.data.TrophyRepository;
import com.example.myapp.model.Trophy;
import com.example.myapp.R;

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
        tvTrophiesUnlockedValue = root.findViewById(R.id.tvTrophiesUnlockedValue);
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
        int active = Prefs.getActiveDaysThisMonth(requireContext());

        if (tvStreakValue != null) tvStreakValue.setText(String.valueOf(streak));           // Weekly Streak (days)
        if (tvTotalWorkoutsValue != null) tvTotalWorkoutsValue.setText(String.valueOf(total));; // Total Workouts
        if (tvActiveDaysValue != null) tvActiveDaysValue.setText(String.valueOf(active));  // Active Days (This Month)


        if (tvTrophiesUnlockedValue != null) {
            int unlocked = TrophyRepository.getInstance(requireContext()).getUnlockedCount();
            tvTrophiesUnlockedValue.setText(String.valueOf(unlocked));
        }


        // TODO: Optionally style cells differently (e.g., badges/emoji), or add click-throughs.
        // TODO: Pull these values from persistence (Room) or backend via ViewModel.
    }

    private void setupTrophiesList() {
        rvTrophies.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        // NEW: fetch all trophies from repository
        List<Trophy> all = TrophyRepository.getInstance(requireContext()).getAll();

        trophiesAdapter = new TrophiesAdapter(all); // CHANGED: pass model.Trophy
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
    /*private List<Trophy> getSampleTrophiesLimited(int limit) {
        List<Trophy> all = new ArrayList<>();
        all.add(new Trophy("First Workout", "Complete your first workout", true));
        all.add(new Trophy("10-Day Streak", "Work out 10 days in a row", false));
        all.add(new Trophy("Bench Milestone", "Hit 200 lb bench press", true));
        all.add(new Trophy("Consistency", "Log workouts 5 days this week", false));
        all.add(new Trophy("Early Bird", "Start a workout before 7am", false));

        if (limit <= 0 || limit >= all.size()) return all.subList(0, Math.min(3, all.size()));
        return all.subList(0, Math.min(limit, all.size()));
    }*/

    /*// ----- Simple model -----
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
    }*/


    // ----- Trophy Repository -----
    /*private List<Trophy> getAllTrophies() {
        List<Trophy> all = new ArrayList<>();

        // Simple (binary) trophies
        all.add(new Trophy("First Workout", "Complete your first workout", Prefs.getBool(requireContext(), "trophy_first_workout", false)));
        all.add(new Trophy("Explorer", "Visit all 5 tabs at least once", Prefs.getBool(requireContext(), "trophy_explorer", false)));
        all.add(new Trophy("Planner", "Create your first routine", Prefs.getBool(requireContext(), "trophy_planner", false)));

        // Incremental trophies (set current/total)
        Trophy habit = new Trophy("Habit Builder", "Open the app on 10 distinct days", Prefs.getBool(requireContext(), "trophy_habit_builder_done", false));
        habit.total = 10;
        habit.current = Prefs.getInt(requireContext(), "trophy_habit_builder_current", 0);
        all.add(habit);

        Trophy timer5 = new Trophy("Timer Time", "Run the timer 5 times", Prefs.getBool(requireContext(), "trophy_timer5_done", false));
        timer5.total = 5;
        timer5.current = Prefs.getInt(requireContext(), "trophy_timer5_current", 0);
        all.add(timer5);

        // …add more here…

        return all;
    }*/



    // ----- Minimal Adapter (uses a simple built-in layout as a placeholder) -----
    private static class TrophiesAdapter extends RecyclerView.Adapter<TrophiesAdapter.Holder> {

        private final List<Trophy> items;

        TrophiesAdapter(List<Trophy> items) {
            this.items = items;
        }

        @NonNull @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_trophy, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            Trophy t = items.get(position);
            h.title.setText(t.getTitle());
            h.desc.setText(t.getDescription());

            if (t.isUnlocked()) {
                h.status.setText("UNLOCKED");

                // icon
                h.icon.setImageResource(R.drawable.ic_unlock);
//                h.icon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.hintColor)));

                h.lock.setVisibility(View.GONE);
                h.progressContainer.setVisibility(View.GONE);
            } else {
                h.status.setText("LOCKED");

                // icon
                h.icon.setImageResource(R.drawable.ic_lock);
//                h.icon.setImageTintList(AppCompatResources.getColorStateList(
//                        h.itemView.getContext(), R.color.hintColor
//                ));

                h.lock.setVisibility(View.VISIBLE);

                if (t.isIncremental()) {
                    h.progressContainer.setVisibility(View.VISIBLE);
                    int pct = (int) (100f * t.getCurrent() / Math.max(1, t.getTotal()));
                    h.progress.setProgress(pct);
                    h.progressText.setText(t.getCurrent() + " / " + t.getTotal());
                } else {
                    h.progressContainer.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

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
