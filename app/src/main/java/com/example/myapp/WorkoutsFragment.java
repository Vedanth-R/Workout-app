package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.model.Workout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WorkoutsFragment extends Fragment {

    private TabLayout tabLayout;
    private RecyclerView rvWorkouts;
    private MaterialButton btnCreateWorkout;
    private LinearLayout emptyLayout;

    public  WorkoutAdapter workoutAdapter;
    public  List<Workout> currentWorkouts = new ArrayList<>();

    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();

    private String currentTabKey = "workouts"; // Default tab key

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        // --- Views ---
        tabLayout = view.findViewById(R.id.tabLayout);
        rvWorkouts = view.findViewById(R.id.rvWorkouts);
        btnCreateWorkout = view.findViewById(R.id.btnCreateWorkout);
        emptyLayout = view.findViewById(R.id.emptyLayout);

        sharedPreferences = requireActivity().getSharedPreferences("workout_prefs", Context.MODE_PRIVATE);

        // --- RecyclerView setup ---
        workoutAdapter = new WorkoutAdapter(getContext(), currentWorkouts, new WorkoutAdapter.OnWorkoutClickListener() {
            @Override
            public void onWorkoutClick(Workout workout) {
                // Open VIEW_WORKOUT BottomSheet
                WorkoutBottomSheet bottomSheet = WorkoutBottomSheet.newInstance(
                        workout.getName(),
                        WorkoutBottomSheet.Mode.VIEW_WORKOUT
                );
                bottomSheet.setWorkoutsFragment(WorkoutsFragment.this); // ✅ Pass fragment reference
                bottomSheet.show(getParentFragmentManager(), "WorkoutBottomSheet");
            }

            @Override
            public void onAddExerciseClick(Workout workout) {
                // Open ADD_EXERCISE BottomSheet
                WorkoutBottomSheet bottomSheet = WorkoutBottomSheet.newInstance(
                        workout.getName(),
                        WorkoutBottomSheet.Mode.ADD_EXERCISE
                );
                bottomSheet.setWorkoutsFragment(WorkoutsFragment.this); // ✅ Pass fragment reference
                bottomSheet.show(getParentFragmentManager(), "WorkoutBottomSheet");
            }

            @Override
            public void onDeleteWorkout(Workout workout) {
                currentWorkouts.remove(workout);
                saveWorkouts(currentTabKey, currentWorkouts);
                workoutAdapter.notifyDataSetChanged();
                updateEmptyState();
                Toast.makeText(getContext(), "Deleted: " + workout.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRenameWorkout(Workout workout) {
                showRenameDialog(workout);
            }
        });

        rvWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWorkouts.setAdapter(workoutAdapter);

        // --- Load default tab ---
        loadWorkouts(currentTabKey);

        // --- Tabs ---
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabKey = tab.getPosition() == 0 ? "workouts" : "stretches";
                loadWorkouts(currentTabKey);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        // --- Create workout button ---
        btnCreateWorkout.setOnClickListener(v -> showCreateWorkoutDialog());

        return view;
    }

    // --- Load/save workouts ---
    private void loadWorkouts(String key) {
        String json = sharedPreferences.getString(key, "[]");
        Type type = new TypeToken<List<Workout>>() {}.getType();
        List<Workout> loaded = gson.fromJson(json, type);
        currentWorkouts.clear();
        if (loaded != null) currentWorkouts.addAll(loaded);
        workoutAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void saveWorkouts(String key, List<Workout> list) {
        sharedPreferences.edit().putString(key, gson.toJson(list)).apply();
    }

    // --- Empty state handling ---
    private void updateEmptyState() {
        if (currentWorkouts.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            rvWorkouts.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            rvWorkouts.setVisibility(View.VISIBLE);
        }
    }

    // --- Dialogs ---
    private void showCreateWorkoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("New Workout");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                Workout newWorkout = new Workout(name);
                currentWorkouts.add(newWorkout);
                saveWorkouts(currentTabKey, currentWorkouts);
                workoutAdapter.notifyDataSetChanged();
                updateEmptyState();
                Toast.makeText(getContext(), "Created: " + name, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showRenameDialog(Workout workout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Rename Workout");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(workout.getName());
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                workout.setName(newName);
                saveWorkouts(currentTabKey, currentWorkouts);
                workoutAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Renamed to: " + newName, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}