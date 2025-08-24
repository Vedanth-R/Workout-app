package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.model.Exercise;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
        void onDeleteExercise(Exercise exercise);
    }

    private Context context;
    private List<Exercise> displayList;
    private OnExerciseClickListener listener;

    public ExerciseAdapter(Context context, List<Exercise> displayList, OnExerciseClickListener listener) {
        this.context = context;
        this.displayList = displayList != null ? displayList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = displayList.get(position);

        holder.tvName.setText(exercise.getName());

        String details = "";
        if (exercise.getReps() != null && !exercise.getReps().isEmpty()) {
            details += exercise.getReps() + " reps";
        }
        if (exercise.getWeight() != null && !exercise.getWeight().isEmpty()) {
            details += (details.isEmpty() ? "" : " • ") + exercise.getWeight() + " kg";
        }
        holder.tvDetails.setText(details);

        holder.itemView.setOnClickListener(v -> listener.onExerciseClick(exercise));

        boolean showDelete = exercise.getReps() != null && !exercise.getReps().isEmpty();
        holder.btnDelete.setVisibility(showDelete ? View.VISIBLE : View.GONE);
        if (showDelete) {
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteExercise(exercise));
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public void updateList(List<Exercise> newList) {
        displayList.clear();
        if (newList != null) displayList.addAll(newList);
        notifyDataSetChanged();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;
        ImageButton btnDelete;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvDetails = itemView.findViewById(R.id.tvExerciseDetails);
            btnDelete = itemView.findViewById(R.id.btnDeleteExercise);
        }
    }
}