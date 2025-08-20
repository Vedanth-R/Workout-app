package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvDate;
    private LineChart chartProgress;
    private Spinner spinnerMetric;
    private TextView tvWorkoutCount, tvCaloriesBurned, tvStreakCount, tvViewAllPR;
    private RecyclerView rvPersonalRecords;
    private MaterialButton btnQuickWorkout, btnLogMeal;
    private FloatingActionButton fabSettings, fabChat;

    public HomeFragment() {
        // Required empty constructor
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

        // Bind views
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvDate = view.findViewById(R.id.tvDate);
        chartProgress = view.findViewById(R.id.chartProgress);
        spinnerMetric = view.findViewById(R.id.spinnerMetric);
        tvWorkoutCount = view.findViewById(R.id.tvWorkoutCount);
        tvCaloriesBurned = view.findViewById(R.id.tvCaloriesBurned);
        tvStreakCount = view.findViewById(R.id.tvStreakCount);
        tvViewAllPR = view.findViewById(R.id.tvViewAllPR);
        rvPersonalRecords = view.findViewById(R.id.rvPersonalRecords);
        btnQuickWorkout = view.findViewById(R.id.btnQuickWorkout);
        btnLogMeal = view.findViewById(R.id.btnLogMeal);

        // Optional: if you moved FABs into the fragment (else keep in MainActivity)
        fabSettings = getActivity().findViewById(R.id.fabSettings);

        // Dummy data
        tvGreeting.setText("Good Morning, User");
        tvDate.setText("Today, August 19");
        tvWorkoutCount.setText("12");
        tvCaloriesBurned.setText("1450");
        tvStreakCount.setText("7");

        setupChartWithDummyData();

        // FAB click listeners
        if (fabSettings != null) {
            fabSettings.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            });
        }


        // Quick Actions
        btnQuickWorkout.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), WorkoutsActivity.class));
        });

        btnLogMeal.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), NutritionActivity.class));
        });

        // Personal Records "View All"
        tvViewAllPR.setOnClickListener(v -> {
            // TODO: open PR activity
        });

        // Spinner listener stub
        spinnerMetric.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                // TODO: update chart based on selection
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
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
}