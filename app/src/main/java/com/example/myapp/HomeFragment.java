package com.example.myapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.model.Food;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvDate;
    private LineChart chartProgress;
    private Spinner spinnerMetric;
    private TextView tvWorkoutCount, tvCaloriesToday, tvStreakCount, tvViewAllPR;
    private RecyclerView rvPersonalRecords;
    private MaterialButton btnQuickWorkout, btnLogMeal;
    private FloatingActionButton fabSettings, fabChat;
    
    private HomeViewModel homeViewModel;

    public HomeFragment() {
        // Required empty constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCaloriesToday();
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
        tvViewAllPR = view.findViewById(R.id.tvViewAllPR);
        rvPersonalRecords = view.findViewById(R.id.rvPersonalRecords);
        btnQuickWorkout = view.findViewById(R.id.btnQuickWorkout);
        btnLogMeal = view.findViewById(R.id.btnLogMeal);

        // Optional: if you moved FABs into the fragment (else keep in MainActivity)
        fabSettings = getActivity().findViewById(R.id.fabSettings);

        // Observe ViewModel data
        observeViewModelData();

        // Setup chart
        setupChartWithRealData();

        loadCaloriesToday();

        // FAB click listeners
        if (fabSettings != null) {
            fabSettings.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            });
        }

        // Quick Actions
        btnQuickWorkout.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            NavOptions options = new NavOptions.Builder()
                    .setLaunchSingleTop(true) // Avoid multiple copies
                    .setPopUpTo(R.id.navigation_home, false) // Optional: keeps home on the back stack
                    .build();
            navController.navigate(R.id.navigation_workouts, null, options);
        });

        btnLogMeal.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            NavOptions options = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(R.id.navigation_home, false)
                    .build();
            navController.navigate(R.id.navigation_nutrition, null, options);
        });

        // Personal Records "View All"
        tvViewAllPR.setOnClickListener(v -> {
            // TODO: open PR activity
        });

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
        homeViewModel.getWorkoutCount().observe(getViewLifecycleOwner(), count -> {
            if (tvWorkoutCount != null) {
                tvWorkoutCount.setText(String.valueOf(count));
            }
        });
        
        // Observe calories burned changes
        homeViewModel.getCaloriesBurned().observe(getViewLifecycleOwner(), calories -> {
            if (tvCaloriesToday != null) {
                tvCaloriesToday.setText(String.valueOf(calories));
            }
        });
        
        // Observe streak count changes
        homeViewModel.getStreakCount().observe(getViewLifecycleOwner(), streak -> {
            if (tvStreakCount != null) {
                tvStreakCount.setText(String.valueOf(streak));
            }
        });
    }

    private void setupChartWithRealData() {
        List<Integer> last7Days = loadLast7DaysCalories();

        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < last7Days.size(); i++) {
            entries.add(new Entry(i, last7Days.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Calories Last 7 Days");
        dataSet.setColor(0xFF6200EE);
        dataSet.setValueTextColor(0xFF000000);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(0xFF6200EE);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);

        LineData lineData = new LineData(dataSet);
        chartProgress.setData(lineData);

        XAxis xAxis = chartProgress.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = chartProgress.getAxisLeft();
        leftAxis.setDrawGridLines(false);

        chartProgress.getAxisRight().setEnabled(false);
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

        YAxis leftAxis = chartProgress.getAxisLeft();
        leftAxis.setDrawGridLines(false);

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
}