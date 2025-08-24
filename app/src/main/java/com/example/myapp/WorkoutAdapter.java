package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.model.Workout;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
        void onAddExerciseClick(Workout workout);
        void onRenameWorkout(Workout workout);
        void onDeleteWorkout(Workout workout);
    }

    private Context context;
    private List<Workout> workouts;
    private OnWorkoutClickListener listener;

    public WorkoutAdapter(Context context, List<Workout> workouts, OnWorkoutClickListener listener) {
        this.context = context;
        this.workouts = workouts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.tvName.setText(workout.getName());

        int exerciseCount = workout.getExercises() != null ? workout.getExercises().size() : 0;
        holder.tvDetails.setText(exerciseCount + " exercises");

        holder.itemView.setOnClickListener(v -> listener.onWorkoutClick(workout));

        holder.btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnMenu);
            popup.getMenuInflater().inflate(R.menu.workout_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> handleMenuClick(item, workout));
            popup.show();
        });
    }

    private boolean handleMenuClick(MenuItem item, Workout workout) {
        int id = item.getItemId();
        if (id == R.id.menu_add_exercise) {
            listener.onAddExerciseClick(workout);
            return true;
        } else if (id == R.id.menu_rename) {
            listener.onRenameWorkout(workout);
            return true;
        } else if (id == R.id.menu_delete) {
            listener.onDeleteWorkout(workout);
            return true;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;
        MaterialButton btnMenu;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvWorkoutName);
            tvDetails = itemView.findViewById(R.id.tvWorkoutDetails);
            btnMenu = itemView.findViewById(R.id.btnMoreOptions);
        }
    }
}