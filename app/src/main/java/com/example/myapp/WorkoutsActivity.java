package com.example.myapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapp.model.Workout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorkoutsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "workout_prefs";
    private static final String KEY_LIST_NAMES = "list_names";
    private SharedPreferences sharedPreferences;

    private ArrayList<String> listNames;
    private Map<String, ArrayList<Workout>> workoutLists;
    private ArrayAdapter<String> listNamesAdapter;
    private ArrayAdapter<Workout> workoutAdapter;

    private Spinner listsSpinner;
    private ListView workoutListView;
    private Button shareButton;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workouts);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        listNames = new ArrayList<>(loadListNames());
        workoutLists = new HashMap<>();
        for (String listName : listNames) {
            workoutLists.put(listName, new ArrayList<>(loadWorkoutsForList(listName)));
        }

        ImageButton backButton = findViewById(R.id.backButton);
        EditText listNameEditText = findViewById(R.id.listNameEditText);
        Button createListButton = findViewById(R.id.createListButton);
        listsSpinner = findViewById(R.id.listsSpinner);
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        Button addButton = findViewById(R.id.addButton);
        workoutListView = findViewById(R.id.workoutListView);
        Button deleteListButton = findViewById(R.id.deleteListButton);

        String[] listofexercise = getResources().getStringArray(R.array.exercises);
        ArrayAdapter<String> exerciseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listofexercise);
        autoCompleteTextView.setAdapter(exerciseAdapter);

        listNamesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listNames);
        listNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listsSpinner.setAdapter(listNamesAdapter);

        if (!listNames.isEmpty()) {
            updateWorkoutListAdapter(listNames.get(0));
        }
        shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareWorkoutNames();
            }
        });


        createListButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String listName = listNameEditText.getText().toString().trim();
                if (!listName.isEmpty() && !workoutLists.containsKey(listName)) {
                    listNames.add(listName);
                    workoutLists.put(listName, new ArrayList<>());
                    listNamesAdapter.notifyDataSetChanged();
                    saveListNames();
                    listNameEditText.setText("");
                }
            }
        });



        addButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String selectedExercise = autoCompleteTextView.getText().toString().trim();
                String currentListName = (String) listsSpinner.getSelectedItem();
                if (!selectedExercise.isEmpty() && currentListName != null) {
                    workoutLists.get(currentListName).add(new Workout(selectedExercise, "", ""));
                    updateWorkoutListAdapter(currentListName);
                    autoCompleteTextView.setText("");
                    saveWorkoutsForList(currentListName);
                }
            }
        });

        listsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.vibrantAccent));
                String selectedListName = (String) parent.getItemAtPosition(position);
                updateWorkoutListAdapter(selectedListName);
            }


            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(WorkoutsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        deleteListButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String currentListName = (String) listsSpinner.getSelectedItem();
                if (currentListName != null) {
                    listNames.remove(currentListName);
                    workoutLists.remove(currentListName);
                    listNamesAdapter.notifyDataSetChanged();
                    saveListNames();
                    clearWorkoutsForList(currentListName);

                    if (!listNames.isEmpty()) {
                        updateWorkoutListAdapter(listNames.get(0));
                    } else {
                        workoutAdapter.clear();
                        workoutAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_workouts);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            public boolean onNavigationItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.navigation_workouts) {
                    return true;
                }
                if (item.getItemId() == R.id.navigation_home) {
                    Intent workoutsIntent = new Intent(WorkoutsActivity.this, MainActivity.class);
                    startActivity(workoutsIntent);
                    return true;
                }
                if (item.getItemId() == R.id.navigation_settings) {
                    Intent settingsIntent = new Intent(WorkoutsActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    return true;
                }
                if (item.getItemId() == R.id.navigation_stopwatch) {
                    Intent settingsIntent = new Intent(WorkoutsActivity.this, StopwatchActivity.class);
                    startActivity(settingsIntent);
                    return true;
                }
                if (item.getItemId() == R.id.navigation_stretches) {
                    Intent settingsIntent = new Intent(WorkoutsActivity.this, Stretching.class);
                    startActivity(settingsIntent);
                    return true;
                }

                return false;
            }
        });
    }

    private void shareWorkoutNames() {
        StringBuilder workoutNames = new StringBuilder("My Workouts:\n");

        // Get the selected workout list
        String currentListName = (String) listsSpinner.getSelectedItem();
        if (currentListName != null) {
            for (Workout workout : workoutLists.get(currentListName)) {
                workoutNames.append(workout.getExercise()).append("\n");
            }
        }

        // Create an Intent to share the workout names
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, workoutNames.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Workout Routine"));
    }


    private void updateWorkoutListAdapter(String listName) {
        workoutAdapter = new ArrayAdapter<Workout>(this, R.layout.list_item_workout, R.id.exerciseTextView, workoutLists.get(listName)) {

            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_workout, parent, false);
                }

                Workout workout = getItem(position);


                TextView exerciseTextView = convertView.findViewById(R.id.exerciseTextView);
                EditText repsEditText = convertView.findViewById(R.id.repsEditText);
                EditText weightEditText = convertView.findViewById(R.id.weightEditText);
                ImageButton deleteButton = convertView.findViewById(R.id.deleteButton);

                exerciseTextView.setText(workout.getExercise());
                repsEditText.setText(workout.getReps());
                weightEditText.setText(workout.getWeight());

                repsEditText.addTextChangedListener(new SimpleTextWatcher() {

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        workout.setReps(s.toString());
                        saveWorkoutsForList((String) listsSpinner.getSelectedItem());
                    }
                });

                weightEditText.addTextChangedListener(new SimpleTextWatcher() {

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        workout.setWeight(s.toString());
                        saveWorkoutsForList((String) listsSpinner.getSelectedItem());
                    }
                });

                exerciseTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(WorkoutsActivity.this, workout.getExercise(), Toast.LENGTH_SHORT).show();
                    }
                });

                deleteButton.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        String currentListName = (String) listsSpinner.getSelectedItem();
                        if (currentListName != null) {
                            workoutLists.get(currentListName).remove(workout);
                            updateWorkoutListAdapter(currentListName);
                            saveWorkoutsForList(currentListName);
                        }
                    }
                });

                return convertView;
            }
        };
        workoutListView.setAdapter(workoutAdapter);
    }

    private void saveListNames() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> set = new HashSet<>(listNames);
        editor.putStringSet(KEY_LIST_NAMES, set);
        editor.apply();
    }

    private Set<String> loadListNames() {
        return sharedPreferences.getStringSet(KEY_LIST_NAMES, new HashSet<>());
    }

    private void saveWorkoutsForList(String listName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> set = new HashSet<>();
        for (Workout workout : workoutLists.get(listName)) {
            set.add(workout.getExercise() + ";" + workout.getReps() + ";" + workout.getWeight());
        }
        editor.putStringSet(listName, set);
        editor.apply();
    }

    private ArrayList<Workout> loadWorkoutsForList(String listName) {
        Set<String> workouts = sharedPreferences.getStringSet(listName, new HashSet<>());
        ArrayList<Workout> workoutList = new ArrayList<>();
        for (String workoutString : workouts) {
            String[] parts = workoutString.split(";");
            String exercise = parts[0];
            String reps = parts.length > 1 ? parts[1] : "";
            String weight = parts.length > 2 ? parts[2] : "";
            workoutList.add(new Workout(exercise, reps, weight));
        }
        return workoutList;
    }

    private void clearWorkoutsForList(String listName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(listName);
        editor.apply();
    }

    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
