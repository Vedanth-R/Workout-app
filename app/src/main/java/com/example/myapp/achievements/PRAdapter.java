package com.example.myapp.achievements;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapp.R;
import java.util.*;

public class PRAdapter extends RecyclerView.Adapter<PRAdapter.VH> {
    private final List<PersonalRecord> items = new ArrayList<>();

    public void submit(List<PersonalRecord> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_pr, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        PersonalRecord pr = items.get(pos);
        h.exercise.setText(pr.exerciseName + " (" + pr.metric + ")");
        h.value.setText(String.valueOf(pr.value));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView exercise, value;
        VH(View v){ super(v); exercise=v.findViewById(R.id.exercise); value=v.findViewById(R.id.value); }
    }
}
