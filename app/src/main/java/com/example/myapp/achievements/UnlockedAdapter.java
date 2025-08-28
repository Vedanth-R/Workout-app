package com.example.myapp.achievements;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapp.R;
import java.util.*;

public class UnlockedAdapter extends RecyclerView.Adapter<UnlockedAdapter.VH> {
    private final List<BadgeProgress> items = new ArrayList<>();

    public void submit(List<BadgeProgress> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_badge_unlocked, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        BadgeProgress bp = items.get(pos);
        h.icon.setImageResource(bp.badge.iconResId);
        h.title.setText(bp.badge.title + (bp.levelIndex >= 0 ? " • L" + (bp.levelIndex+1) : ""));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon; TextView title;
        VH(View v){ super(v); icon=v.findViewById(R.id.icon); title=v.findViewById(R.id.title); }
    }
}
