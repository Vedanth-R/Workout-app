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

import com.example.myapp.model.Stretch;
import com.example.myapp.model.Workout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Stretching extends AppCompatActivity {

    private static final String PREFS_NAME = "stretch_prefs";
    private static final String KEY_LIST_NAMES = "stretch_list_names";
    private SharedPreferences sharedPreferences;

    private ArrayList<String> listNames;
    private HashMap<String, ArrayList<Stretch>> stretchLists;
    private ArrayAdapter<String> listNamesAdapter;
    private ArrayAdapter<Stretch> stretchAdapter;

    private Spinner listsSpinner;
    private ListView stretchListView;
    private Button shareButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stretching);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        listNames = new ArrayList<>(loadListNames());
        stretchLists = new HashMap<>();
        for (String listName : listNames) {
            stretchLists.put(listName, new ArrayList<>(loadStretchesForList(listName)));
        }

        ImageButton backButton = findViewById(R.id.backButton);
        EditText listNameEditText = findViewById(R.id.listNameEditText);
        Button createListButton = findViewById(R.id.createListButton);
        listsSpinner = findViewById(R.id.listsSpinner);
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        Button addButton = findViewById(R.id.addButton);
        stretchListView = findViewById(R.id.stretchListView);
        Button deleteListButton = findViewById(R.id.deleteListButton);

        String[] listofstretches = getResources().getStringArray(R.array.yoga);
        ArrayAdapter<String> stretchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listofstretches);
        autoCompleteTextView.setAdapter(stretchAdapter);

        listNamesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listNames);
        listNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listsSpinner.setAdapter(listNamesAdapter);


        shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareWorkoutNames();
            }
        });


        if (!listNames.isEmpty()) {
            updateStretchListAdapter(listNames.get(0));
        }

        createListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String listName = listNameEditText.getText().toString().trim();
                if (!listName.isEmpty() && !stretchLists.containsKey(listName)) {
                    listNames.add(listName);
                    stretchLists.put(listName, new ArrayList<>());
                    listNamesAdapter.notifyDataSetChanged();
                    saveListNames();
                    listNameEditText.setText("");
                }
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedStretch = autoCompleteTextView.getText().toString().trim();
                String currentListName = (String) listsSpinner.getSelectedItem();
                if (!selectedStretch.isEmpty() && currentListName != null) {
                    stretchLists.get(currentListName).add(new Stretch(selectedStretch, "", ""));
                    updateStretchListAdapter(currentListName);
                    autoCompleteTextView.setText("");
                    saveStretchesForList(currentListName);
                }
            }
        });

        listsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedListName = (String) parent.getItemAtPosition(position);
                updateStretchListAdapter(selectedListName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Stretching.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        deleteListButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String currentListName = (String) listsSpinner.getSelectedItem();
                if (currentListName != null) {
                    listNames.remove(currentListName);
                    stretchLists.remove(currentListName);
                    listNamesAdapter.notifyDataSetChanged();
                    saveListNames();
                    clearStretchesForList(currentListName);

                    if (!listNames.isEmpty()) {
                        updateStretchListAdapter(listNames.get(0));
                    } else {
                        stretchAdapter.clear();
                        stretchAdapter.notifyDataSetChanged();
                    }
                }
            }
        });








        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_nutrition);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.navigation_nutrition) {
                    return true;
                }
                if (item.getItemId() == R.id.navigation_home) {
                    Intent workoutsIntent = new Intent(Stretching.this, MainActivity.class);
                    startActivity(workoutsIntent);
                    return true;
                }
                if (item.getItemId() == R.id.navigation_settings) {
                    Intent settingsIntent = new Intent(Stretching.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    return true;
                }
                if (item.getItemId() == R.id.navigation_stopwatch) {
                    Intent settingsIntent = new Intent(Stretching.this, StopwatchActivity.class);
                    startActivity(settingsIntent);
                    return true;
                }
                if (item.getItemId() == R.id.navigation_workouts) {
                    Intent settingsIntent = new Intent(Stretching.this, WorkoutsActivity.class);
                    startActivity(settingsIntent);
                    return true;
                }

                return false;
            }
        });
    }

    private void shareWorkoutNames() {
        StringBuilder workoutNames = new StringBuilder("My Stretches:\n");

        // Get the selected workout list
        String currentListName = (String) listsSpinner.getSelectedItem();
        if (currentListName != null) {
            for (Stretch workout : stretchLists.get(currentListName)) {
                workoutNames.append(workout.getExercise()).append("\n");
            }
        }

        // Create an Intent to share the workout names
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, workoutNames.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Stretching Routine"));
    }


    private void updateStretchListAdapter(String listName) {
        stretchAdapter = new ArrayAdapter<Stretch>(this, R.layout.list_item_stretch, R.id.exerciseTextView, stretchLists.get(listName)) {

            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_workout, parent, false);
                }

                Stretch stretch = getItem(position);


                TextView exerciseTextView = convertView.findViewById(R.id.exerciseTextView);
                EditText repsEditText = convertView.findViewById(R.id.repsEditText);
                EditText weightEditText = convertView.findViewById(R.id.weightEditText);
                ImageButton deleteButton = convertView.findViewById(R.id.deleteButton);

                exerciseTextView.setText(stretch.getExercise());
                repsEditText.setText(stretch.getReps());
                weightEditText.setText(stretch.getDuration());

                repsEditText.addTextChangedListener(new Stretching.SimpleTextWatcher() {

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        stretch.setReps(s.toString());
                        saveStretchesForList((String) listsSpinner.getSelectedItem());
                    }
                });

                weightEditText.addTextChangedListener(new Stretching.SimpleTextWatcher() {

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        stretch.setDuration(s.toString());
                        saveStretchesForList((String) listsSpinner.getSelectedItem());
                    }
                });

                exerciseTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(Stretching.this, stretch.getExercise(), Toast.LENGTH_SHORT).show();
                    }
                });

                deleteButton.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        String currentListName = (String) listsSpinner.getSelectedItem();
                        if (currentListName != null) {
                            stretchLists.get(currentListName).remove(stretch);
                            updateStretchListAdapter(currentListName);
                            saveStretchesForList(currentListName);
                        }
                    }
                });

                return convertView;
            }
        };
        stretchListView.setAdapter(stretchAdapter);
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

    private void saveStretchesForList(String listName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> set = new HashSet<>();
        for (Stretch stretch : stretchLists.get(listName)) {
            set.add(stretch.getExercise() + ";" + stretch.getDuration() + ";" + stretch.getReps());
        }
        editor.putStringSet(listName, set);
        editor.apply();
    }

    private ArrayList<Stretch> loadStretchesForList(String listName) {
        Set<String> stretches = sharedPreferences.getStringSet(listName, new HashSet<>());
        ArrayList<Stretch> stretchList = new ArrayList<>();
        for (String stretchString : stretches) {
            String[] parts = stretchString.split(";");
            String exercise = parts[0];
            String reps = parts.length > 1 ? parts[1] : "";
            String duration = parts.length > 2 ? parts[2] : "";
            stretchList.add(new Stretch(exercise, reps, duration));
        }
        return stretchList;
    }


    private void clearStretchesForList(String listName) {
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

