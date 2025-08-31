package com.example.myapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.data.TrophyRepository;
import com.example.myapp.model.Food;
import com.example.myapp.model.Trophy;
import com.example.myapp.trophies.TrophiesAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvDate;
    private LineChart chartProgress;
    private Spinner spinnerMetric;
    private TextView tvWorkoutCount, tvCaloriesToday, tvStreakCount/*, tvViewAllPR*/;
//    private RecyclerView rvPersonalRecords;
    private MaterialButton btnQuickWorkout, btnLogMeal;
    private FloatingActionButton fabSettings, fabChat;
    
    private HomeViewModel homeViewModel;

    private RecyclerView rvAchievementsPreview;
    private TrophiesAdapter trophiesPreviewAdapter;

    public HomeFragment() {
        // Required empty constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCaloriesToday();
        loadWeeklyWorkouts();

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

        // Initialize ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

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


        // Optional: if you moved FABs into the fragment (else keep in MainActivity)
//        fabSettings = getActivity().findViewById(R.id.fabSettings);

        // Observe ViewModel data
        observeViewModelData();

        // Setup chart
		setupChartWithRealData();
		loadWeeklyWorkouts();
        loadCaloriesToday();

        // FAB click listeners
        if (fabSettings != null) {
            fabSettings.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            });
        }

        // Quick Actions
        btnQuickWorkout.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.navigation_workouts);
        });

        btnLogMeal.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.navigation_nutrition);
        });


        // Personal Records "View All"
//        tvViewAllPR.setOnClickListener(v -> {
//            // TODO: open PR activity
//        });

        // Spinner listener stub
    }

    private List<Integer> loadLast7DaysCalories() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("nutrition_prefs", Context.MODE_PRIVATE);
        String json = prefs.getString("last_7_days_calories", null);
        List<Integer> last7 = new ArrayList<>();

        if (json != null) {
            try {
                JSONArray arr = new JSONArray(json);
                for (int i = 0; i < arr.length(); i++) last7.add(arr.getInt(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Make sure list always has 7 entries (fill with 0 if not enough)
        while (last7.size() < 7) last7.add(0);
        return last7;
    }
    
    private void observeViewModelData() {
        // Observe greeting changes
        homeViewModel.getGreeting().observe(getViewLifecycleOwner(), greeting -> {
            if (tvGreeting != null) {
                tvGreeting.setText(greeting);
            }
        });
        
        // Observe date changes
        homeViewModel.getDate().observe(getViewLifecycleOwner(), date -> {
            if (tvDate != null) {
                tvDate.setText(date);
            }
        });
        
        // Observe workout count changes
        /*homeViewModel.getWorkoutCount().observe(getViewLifecycleOwner(), count -> {
            if (tvWorkoutCount != null) {
//                tvWorkoutCount.setText(String.valueOf(count));
                int weekly = Prefs.getWorkoutsThisWeek(requireContext());
                tvWorkoutCount.setText(String.valueOf(weekly));
            }
        });
*/

        
        // Observe streak count changes
        homeViewModel.getStreakCount().observe(getViewLifecycleOwner(), streak -> {
            if (tvStreakCount != null) {
                tvStreakCount.setText(String.valueOf(streak));
            }
        });
    }

    private void loadWeeklyWorkouts() {
        int weekly = Prefs.getWorkoutsThisWeek(requireContext());
        if (tvWorkoutCount != null) {
            tvWorkoutCount.setText(String.valueOf(weekly));
        }
    }

    private void setupChartWithRealData() {
        List<Integer> last7Days = loadLast7DaysCalories();

        // Reverse entries so newest day is on the right
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < last7Days.size(); i++) {
            entries.add(new Entry(i, last7Days.get(last7Days.size() - 1 - i)));
        }

        int labelColor = Color.BLACK; // default for light mode
        int lineColor = Color.BLACK;
        int circleColor = Color.BLACK;

        int nightModeFlags =
                getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            labelColor = Color.WHITE;
            lineColor = Color.WHITE;
            circleColor = Color.WHITE;
        }

        // Line dataset
        LineDataSet dataSet = new LineDataSet(entries, ""); // empty label removes legend
        dataSet.setColor(lineColor);
        dataSet.setValueTextColor(labelColor);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(circleColor);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        chartProgress.setData(lineData);

        // --- Create labels for last 7 days ---
        String[] days = new String[7];
        Calendar cal = Calendar.getInstance();
        for (int i = 6; i >= 0; i--) {
            days[i] = new SimpleDateFormat("EEE", Locale.getDefault()).format(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        // Configure X-axis
        XAxis xAxis = chartProgress.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(labelColor);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));

        // Configure Y-axis
        YAxis leftAxis = chartProgress.getAxisLeft();
        leftAxis.setTextColor(labelColor);
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = chartProgress.getAxisRight();
        rightAxis.setEnabled(false);

        // Remove legend
        chartProgress.getLegend().setEnabled(false);

        // Disable description
        chartProgress.getDescription().setEnabled(false);

        chartProgress.invalidate();
    }

    private void setupChartWithDummyData() {
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 80));
        entries.add(new Entry(1, 100));
        entries.add(new Entry(2, 95));
        entries.add(new Entry(3, 120));
        entries.add(new Entry(4, 110));
        entries.add(new Entry(5, 130));
        entries.add(new Entry(6, 125));

        LineDataSet dataSet = new LineDataSet(entries, "Weekly Progress");
        dataSet.setColor(0xFF6200EE); // Primary color
        dataSet.setValueTextColor(0xFF000000);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(0xFF6200EE);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);

        LineData lineData = new LineData(dataSet);
        chartProgress.setData(lineData);

        // X & Y axis settings
        XAxis xAxis = chartProgress.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);


        chartProgress.getAxisRight().setEnabled(false);
        chartProgress.getDescription().setEnabled(false);
        chartProgress.invalidate();
    }

    private void loadCaloriesToday() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("nutrition_prefs", Context.MODE_PRIVATE);

        // Compute calories today from the meals stored in SharedPreferences
        int todayCalories = getMealCalories("breakfast", prefs)
                + getMealCalories("lunch", prefs)
                + getMealCalories("dinner", prefs)
                + getMealCalories("snacks", prefs);

        if (tvCaloriesToday != null) {
            tvCaloriesToday.setText(String.valueOf(todayCalories));
        }
    }

    private int getMealCalories(String mealType, SharedPreferences prefs) {
        String json = prefs.getString(mealType + "_foods", null);
        if (json == null) return 0;

        Gson gson = new Gson();
        Type type = new TypeToken<List<Food>>() {}.getType();
        List<Food> foods = gson.fromJson(json, type);

        int total = 0;
        for (Food f : foods) total += f.getCalories();
        return total;
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