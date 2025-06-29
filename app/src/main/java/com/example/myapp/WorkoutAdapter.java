package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapp.model.Workout;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {
    private List<Workout> workoutList;
    private Context context;

    public WorkoutAdapter(List<Workout> workoutList, Context context) {
        this.workoutList = workoutList;
        this.context = context;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workoutList.get(position);
        holder.exerciseTextView.setText(workout.getExercise());
        holder.repsTextView.setText(workout.getReps());
        holder.weightTextView.setText(workout.getWeight());

        holder.itemView.setOnClickListener(v -> {
            // Handle item click if needed
        });
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseTextView, repsTextView, weightTextView;

        WorkoutViewHolder(View itemView) {
            super(itemView);
            exerciseTextView = itemView.findViewById(R.id.exerciseTextView);
            repsTextView = itemView.findViewById(R.id.repsEditText);
            weightTextView = itemView.findViewById(R.id.weightEditText);
        }
    }
}
