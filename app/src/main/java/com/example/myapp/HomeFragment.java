package com.example.myapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.data.TrophyRepository;
import com.example.myapp.model.Trophy;
import com.example.myapp.trophies.TrophiesAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvDate;
    private LineChart chartProgress;
    private TextView tvWorkoutCount, tvCaloriesToday, tvStreakCount/*, tvViewAllPR*/;
    private MaterialButton btnQuickWorkout, btnLogMeal;
    
    private HomeViewModel homeViewModel;

    private RecyclerView rvAchievementsPreview;
    private TrophiesAdapter trophiesPreviewAdapter;

    public HomeFragment() {
        // Required empty constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        homeViewModel.refresh();  // pull fresh numbers after returning from Timer

        if (trophiesPreviewAdapter != null) {
            List<Trophy> fresh = TrophyRepository.getInstance(requireContext()).getAll();
            trophiesPreviewAdapter.setItems(fresh);
        }
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate fragment layout
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModel (AndroidViewModel so we can read prefs safely)
        homeViewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
        ).get(HomeViewModel.class);

        homeViewModel.refresh();

        // Bind views
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvDate = view.findViewById(R.id.tvDate);
        chartProgress = view.findViewById(R.id.chartProgress);
        tvWorkoutCount = view.findViewById(R.id.tvWorkoutCount);
        tvCaloriesToday = view.findViewById(R.id.tvCaloriesToday);
        tvStreakCount = view.findViewById(R.id.tvStreakCount);
        btnQuickWorkout = view.findViewById(R.id.btnQuickWorkout);
        btnLogMeal = view.findViewById(R.id.btnLogMeal);

        rvAchievementsPreview = view.findViewById(R.id.rvAchievementsPreview);
        setupHomeTrophiesList();

        // Seed immediately to avoid blank/flicker
        HomeViewModel.HomeUiState initial = homeViewModel.getState().getValue();
        if (initial != null) render(initial);

        // Observe for updates
        homeViewModel.getState().observe(getViewLifecycleOwner(), this::render);

        // Quick Actions
        btnQuickWorkout.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.navigation_workouts);
        });
        btnLogMeal.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.navigation_nutrition);
        });
    }



    private void setupHomeTrophiesList() {
        rvAchievementsPreview.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        List<Trophy> all = TrophyRepository.getInstance(requireContext()).getAll();
        trophiesPreviewAdapter = new TrophiesAdapter(new ArrayList<>(all));
        rvAchievementsPreview.setAdapter(trophiesPreviewAdapter);
    }

    // Listen for trophy unlocks and refresh/animate the Home preview list.
    private final TrophyRepository.TrophyUnlockListener homeUnlockListener = unlockedId -> {
        if (!isAdded()) return; // Fragment not attached/visible

        requireActivity().runOnUiThread(() -> {
            // 1) Pull fresh data
            List<Trophy> fresh = TrophyRepository.getInstance(requireContext()).getAll();
            trophiesPreviewAdapter.setItems(fresh);

            // 2) Find the unlocked item
            int idx = trophiesPreviewAdapter.indexOf(unlockedId);
            if (idx >= 0) {
                // 3) Mark it as recent so adapter shows Snackbar + pulse
                trophiesPreviewAdapter.setRecentlyUnlocked(unlockedId);

                // 4) Smooth scroll into view
                rvAchievementsPreview.smoothScrollToPosition(idx);

                // 5) Rebind after layout so the animation/snackbar runs on the visible cell
                rvAchievementsPreview.postDelayed(() ->
                        trophiesPreviewAdapter.notifyItemChanged(idx), 150);
            }
        });
    };

    private void render(HomeViewModel.HomeUiState s) {
        if (tvGreeting != null) tvGreeting.setText(s.greeting);
        if (tvDate != null) tvDate.setText(s.date);
        if (tvWorkoutCount != null) tvWorkoutCount.setText(String.valueOf(s.workoutsThisWeek));
        if (tvStreakCount != null) tvStreakCount.setText(String.valueOf(s.dailyStreak));
        if (tvCaloriesToday != null) tvCaloriesToday.setText(String.valueOf(s.caloriesToday));
        renderChart(s.last7DaysCalories);
    }

    private void renderChart(List<Integer> last7Days) {
        if (chartProgress == null || last7Days == null || last7Days.size() != 7) return;

        ArrayList<com.github.mikephil.charting.data.Entry> entries = new ArrayList<>();
        for (int i = 0; i < last7Days.size(); i++) {
            entries.add(new com.github.mikephil.charting.data.Entry(i, last7Days.get(i)));
        }

        int labelColor = Color.WHITE;
        int lineColor = Color.WHITE;
        int circleColor = Color.WHITE;

        com.github.mikephil.charting.data.LineDataSet dataSet =
                new com.github.mikephil.charting.data.LineDataSet(entries, "");
        dataSet.setColor(lineColor);
        dataSet.setValueTextColor(labelColor);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(circleColor);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setValueTextSize(12f);

        com.github.mikephil.charting.data.LineData lineData = new com.github.mikephil.charting.data.LineData(dataSet);
        chartProgress.setData(lineData);

        // Labels for last 7 days
        String[] days = new String[7];
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            days[i] = sdf.format(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        XAxis xAxis = chartProgress.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(labelColor);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(days));

        YAxis leftAxis = chartProgress.getAxisLeft();
        leftAxis.setTextColor(labelColor);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);

        chartProgress.getAxisRight().setEnabled(false);
        chartProgress.getLegend().setEnabled(false);
        chartProgress.getDescription().setEnabled(false);

        com.github.mikephil.charting.components.MarkerView marker =
                new com.github.mikephil.charting.components.MarkerView(getContext(), R.layout.marker_view) {
                    @Override public void refreshContent(Entry e, Highlight highlight) {
                        TextView tv = findViewById(R.id.tvMarker);
                        tv.setText((int) e.getY() + " cal");
                        super.refreshContent(e, highlight);
                    }
                    @Override public MPPointF getOffset() {
                        return new MPPointF(-(getWidth() / 2f), -getHeight());
                    }
                };

        chartProgress.setMarker(marker);
        chartProgress.invalidate();
    }


    @Override
    public void onStart() {
        super.onStart();
        TrophyRepository.getInstance(requireContext()).addListener(homeUnlockListener);
    }

    @Override
    public void onStop() {
        TrophyRepository.getInstance(requireContext()).removeListener(homeUnlockListener);
        super.onStop();
    }


}