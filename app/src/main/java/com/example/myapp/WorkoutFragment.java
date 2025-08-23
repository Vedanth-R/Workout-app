package com.example.myapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.WorkoutAdapter;
import com.example.myapp.model.Workout;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class WorkoutFragment extends Fragment {

    private RecyclerView rvWorkouts;
    private LinearLayout llEmptyState;
    private MaterialButton btnCreateWorkout;

    private List<Workout> workoutList = new ArrayList<>();
    private WorkoutAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        rvWorkouts = view.findViewById(R.id.rvWorkouts);
        btnCreateWorkout = view.findViewById(R.id.btnCreateWorkout);

        adapter = new WorkoutAdapter(workoutList, getContext(), null);
        rvWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWorkouts.setAdapter(adapter);

        updateEmptyState();

        btnCreateWorkout.setOnClickListener(v -> {
            // Add a new workout with empty reps/weight
            workoutList.add(new Workout("New Exercise", "", ""));
            adapter.notifyItemInserted(workoutList.size() - 1);
            updateEmptyState();
            rvWorkouts.scrollToPosition(workoutList.size() - 1);
        });

        return view;
    }

    private void updateEmptyState() {
        if (workoutList.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvWorkouts.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvWorkouts.setVisibility(View.VISIBLE);
        }
    }
}