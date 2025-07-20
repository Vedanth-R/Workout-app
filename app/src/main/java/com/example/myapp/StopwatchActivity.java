package com.example.myapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.model.Workout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class StopwatchActivity extends AppCompatActivity {
    private TextView stopwatchTimer;
    ArrayList<String> listNames;
    private Button startButton, stopButton, resetButton, logWorkoutButton;
    private boolean loggedToday = false;
    private RecyclerView recyclerView;
    private WorkoutAdapter workoutAdapter;
    private static final String PREFS_NAME = "workout_prefs";
    private SharedPreferences sharedPreferences;

    private ArrayList<Workout> workoutList;

    private Handler handler = new Handler();
    private long startTime = 0L, timeInMilliseconds = 0L, timeSwapBuff = 0L, updateTime = 0L;

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = System.currentTimeMillis() - startTime;
            updateTime = timeSwapBuff + timeInMilliseconds;
            int secs = (int) (updateTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updateTime % 1000);
            stopwatchTimer.setText("" + mins + ":" + String.format("%02d", secs) + ":" + String.format("%03d", milliseconds));
            handler.postDelayed(this, 0);
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);


        Spinner listsSpinner = findViewById(R.id.listSpinner);
        stopwatchTimer = findViewById(R.id.stopwatch_timer);
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        resetButton = findViewById(R.id.reset_button);
        logWorkoutButton = findViewById(R.id.logWorkoutButton);
        ImageButton backButton=findViewById(R.id.backButton);
        recyclerView = findViewById(R.id.recycler_view);


        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        workoutList = new ArrayList<>();

        workoutAdapter = new WorkoutAdapter(workoutList, this, null);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(workoutAdapter);

        listNames = new ArrayList<>(loadListNames());
        ArrayAdapter<String> listNamesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listNames);
        listNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listsSpinner.setAdapter(listNamesAdapter);

        listsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.vibrantAccent));
                String selectedListName = (String) parent.getItemAtPosition(position);
                retrieveWorkouts(selectedListName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_stopwatch);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            public boolean onNavigationItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.navigation_stopwatch) {
                    return true;
                }
                if (item.getItemId() == R.id.navigation_workouts) {
                    Intent workoutsIntent = new Intent(StopwatchActivity.this, WorkoutsActivity.class);
                    startActivity(workoutsIntent);
                    return true;
                }
                if (item.getItemId() == R.id.navigation_settings) {
                    Intent settingsIntent = new Intent(StopwatchActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    return true;
                }
                if (item.getItemId() == R.id.navigation_home) {
                    Intent settingsIntent = new Intent(StopwatchActivity.this, MainActivity.class);
                    startActivity(settingsIntent);
                    return true;
                }
                if (item.getItemId() == R.id.navigation_nutrition) {
                    Intent settingsIntent = new Intent(StopwatchActivity.this, Stretching.class);
                    startActivity(settingsIntent);
                    return true;
                }
                return false;
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                startTime = System.currentTimeMillis();
                handler.postDelayed(updateTimerThread, 0);
                startButton.setVisibility(View.GONE);
                stopButton.setVisibility(View.VISIBLE);
                resetButton.setVisibility(View.GONE);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                timeSwapBuff += timeInMilliseconds;
                handler.removeCallbacks(updateTimerThread);
                startButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.GONE);
                resetButton.setVisibility(View.VISIBLE);
            }
        });

        logWorkoutButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                loggedToday = !loggedToday;

                if (loggedToday) {
                    logWorkoutButton.setText("Logged Today");
                } else {
                    logWorkoutButton.setText("Log Workout");
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(StopwatchActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime = 0L;
                timeSwapBuff = 0L;
                stopwatchTimer.setText("0:00:000");
                resetButton.setVisibility(View.GONE);
                startButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.GONE);
            }
        });

    }
    private void retrieveWorkouts(String listName) {
        workoutList.clear();
        Set<String> workoutSet = sharedPreferences.getStringSet(listName, new HashSet<>());

        if (workoutSet != null) {
            workoutList.clear();
            for (String workoutString : workoutSet) {
                String[] parts = workoutString.split(";");
                String exercise = parts[0];
                String reps = parts.length > 1 ? parts[1] : "";
                String weight = parts.length > 2 ? parts[2] : "";

                // Add the workout to the list
                workoutList.add(new Workout(exercise, reps, weight));
            }

            workoutAdapter.notifyDataSetChanged();
        }
    }
    private Set<String> loadListNames() {
        return sharedPreferences.getStringSet("list_names", new HashSet<>());
    }
}
