package com.example.myapp;

import static java.security.AccessController.getContext;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.ExerciseAdapter;
import com.example.myapp.R;
import com.example.myapp.model.Exercise;
import com.example.myapp.model.Workout;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkoutBottomSheet extends BottomSheetDialogFragment {

    public enum Mode { VIEW_WORKOUT, ADD_EXERCISE }

    private RecyclerView rvExercises;
    private TextInputEditText etSearch;
    private ExerciseAdapter exerciseAdapter;
    private List<Exercise> exerciseList = new ArrayList<>();
    private String workoutName;
    private Mode mode;
    private String[] exerciseArray;
    private String[] currentTabArray;
    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();

    // Safe reference to parent fragment
    private WorkoutsFragment workoutsFragment;

    private static final String ARG_WORKOUT_NAME = "workout_name";
    private static final String ARG_MODE = "mode";

    public static WorkoutBottomSheet newInstance(String workoutName, Mode mode) {
        WorkoutBottomSheet fragment = new WorkoutBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_WORKOUT_NAME, workoutName);
        args.putString(ARG_MODE, mode.name());
        fragment.setArguments(args);
        return fragment;
    }

    public void setWorkoutsFragment(WorkoutsFragment fragment) {
        this.workoutsFragment = fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_workout, container, false);

        rvExercises = view.findViewById(R.id.rvFoodItems);
        etSearch = view.findViewById(R.id.etFoodSearch);

        sharedPreferences = requireActivity().getSharedPreferences("workout_prefs", Context.MODE_PRIVATE);

        exerciseArray = getResources().getStringArray(R.array.exercises);
        currentTabArray = exerciseArray;

        if (getArguments() != null) {
            workoutName = getArguments().getString(ARG_WORKOUT_NAME);
            mode = Mode.valueOf(getArguments().getString(ARG_MODE, Mode.VIEW_WORKOUT.name()));
        }

        rvExercises.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load existing exercises from SharedPreferences
        loadExercises();

        if (mode == Mode.ADD_EXERCISE) {
            etSearch.setVisibility(View.VISIBLE);

            // Local search results adapter
            final List<Exercise> searchResults = new ArrayList<>();
            ExerciseAdapter searchAdapter = new ExerciseAdapter(getContext(), new ArrayList<>(searchResults),
                    new ExerciseAdapter.OnExerciseClickListener() {
                        @Override
                        public void onExerciseClick(Exercise clickedExercise) {
                            Exercise newExercise = new Exercise(clickedExercise.getName(), "", "");
                            editExercise(newExercise, true);
                        }

                        @Override
                        public void onDeleteExercise(Exercise exercise) { }
                    });
            rvExercises.setAdapter(searchAdapter);

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void afterTextChanged(Editable s) { }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim().toLowerCase();
                    List<Exercise> filtered = new ArrayList<>();
                    for (String name : currentTabArray) {
                        if (name.toLowerCase().contains(query)) {
                            filtered.add(new Exercise(name, "", ""));
                        }
                    }
                    searchAdapter.updateList(filtered);
                }
            });

        } else {
            etSearch.setVisibility(View.GONE);

            exerciseAdapter = new ExerciseAdapter(getContext(), new ArrayList<>(exerciseList),
                    new ExerciseAdapter.OnExerciseClickListener() {
                        @Override
                        public void onExerciseClick(Exercise clickedExercise) {
                            editExercise(clickedExercise, false);
                        }

                        @Override
                        public void onDeleteExercise(Exercise exercise) {
                            exerciseList.remove(exercise);
                            saveExercises();
                            exerciseAdapter.updateList(new ArrayList<>(exerciseList));
                            updateWorkoutExerciseCount();
                        }
                    });
            rvExercises.setAdapter(exerciseAdapter);
        }

        return view;
    }

    private void editExercise(Exercise exercise, boolean isNew) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(isNew ? "Add Exercise" : "Edit Exercise");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_exercise, null);
        EditText etReps = dialogView.findViewById(R.id.etReps);
        EditText etWeight = dialogView.findViewById(R.id.etWeight);

        etReps.setText(exercise.getReps());
        etWeight.setText(exercise.getWeight());

        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            exercise.setReps(etReps.getText().toString().trim());
            exercise.setWeight(etWeight.getText().toString().trim());

            boolean wasNew = false;
            if (isNew) {
                if (!exerciseList.contains(exercise)) {
                    exerciseList.add(exercise);
                    wasNew = true;
                }
            }

            saveExercises();

            if (exerciseAdapter != null) {
                exerciseAdapter.updateList(new ArrayList<>(exerciseList));
            }

            if (wasNew) {
                Toast.makeText(getContext(),
                        exercise.getName() + " added to workout",
                        Toast.LENGTH_SHORT).show();
                updateWorkoutExerciseCount();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void loadExercises() {
        String json = sharedPreferences.getString("workout_" + workoutName, "[]");
        Type type = new TypeToken<List<Exercise>>() {}.getType();
        List<Exercise> saved = gson.fromJson(json, type);
        exerciseList.clear();
        if (saved != null) exerciseList.addAll(saved);
    }

    private void saveExercises() {
        String json = gson.toJson(exerciseList);
        sharedPreferences.edit().putString("workout_" + workoutName, json).apply();
    }

    private void updateWorkoutExerciseCount() {
        if (workoutsFragment != null) {
            for (Workout w : workoutsFragment.currentWorkouts) {
                if (w.getName().equals(workoutName)) {
                    w.setExercises(new ArrayList<>(exerciseList));
                    workoutsFragment.workoutAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }
}