package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // New UI elements
    private TextView greetingText;
    private TextView workoutCount;
    private TextView activeDaysCount;
    private TextView caloriesCount;
    private Spinner chartTypeSpinner;
    private LineChart progressChart;

    // Existing elements
    private FloatingActionButton fabChat;
    private FloatingActionButton fabSettings;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize new UI elements
        greetingText = findViewById(R.id.greetingText);
        workoutCount = findViewById(R.id.workoutCount);
        activeDaysCount = findViewById(R.id.activeDaysCount);
        caloriesCount = findViewById(R.id.caloriesCount);
        chartTypeSpinner = findViewById(R.id.chartTypeSpinner);
        progressChart = findViewById(R.id.progressChart);

        // Set dummy data
        greetingText.setText("Good Morning, User");
        workoutCount.setText("12");
        activeDaysCount.setText("5");
        caloriesCount.setText("1450");

        setupChartWithDummyData();

        // Floating Action Buttons
        fabChat = findViewById(R.id.fabChat);
        fabSettings = findViewById(R.id.fabSettings);

        // AI Chat button - keep existing functionality
        fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, chatbot.class);
                startActivity(intent);
            }
        });

        // Settings FAB opens SettingsActivity
        fabSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        FloatingActionButton fabSettings = findViewById(R.id.fabSettings);
        fabSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Bottom navigation setup - keep existing functionality
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    return true;
                } else if (id == R.id.navigation_workouts) {
                    startActivity(new Intent(MainActivity.this, WorkoutsActivity.class));
                    return true;
                } else if (id == R.id.navigation_settings) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    return true;
                } else if (id == R.id.navigation_stopwatch) {
                    startActivity(new Intent(MainActivity.this, StopwatchActivity.class));
                    return true;
                } else if (id == R.id.navigation_stretches) {
                    startActivity(new Intent(MainActivity.this, Stretching.class));
                    return true;
                }
                return false;
            }
        });

        // Optional: Spinner selection listener stub (no actual chart update yet)
        chartTypeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                // TODO: Update chart based on spinner selection
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupChartWithDummyData() {
        ArrayList<Entry> entries = new ArrayList<>();

        // Dummy data: (x, y)
        entries.add(new Entry(0, 80));
        entries.add(new Entry(1, 100));
        entries.add(new Entry(2, 95));
        entries.add(new Entry(3, 120));
        entries.add(new Entry(4, 110));
        entries.add(new Entry(5, 130));
        entries.add(new Entry(6, 125));

        LineDataSet dataSet = new LineDataSet(entries, "Weekly Progress");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);

        LineData lineData = new LineData(dataSet);
        progressChart.setData(lineData);

        // Optional: Customize chart appearance
        XAxis xAxis = progressChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = progressChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);

        progressChart.getAxisRight().setEnabled(false); // Disable right axis

        progressChart.getDescription().setEnabled(false); // Remove description
        progressChart.invalidate(); // Refresh chart
    }
}
