package com.example.myapp.trophies;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.model.Trophy;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class TrophiesAdapter extends RecyclerView.Adapter<TrophiesAdapter.Holder> {

    private final List<Trophy> items = new ArrayList<>();
    @Nullable private String recentlyUnlockedId;

    public TrophiesAdapter(List<Trophy> initial) {
        if (initial != null) items.addAll(initial);
    }

    public void setItems(List<Trophy> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public int indexOf(String id) {
        for (int i = 0; i < items.size(); i++) if (items.get(i).getId().equals(id)) return i;
        return -1;
    }

    public void setRecentlyUnlocked(String id) {
        this.recentlyUnlockedId = id;
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trophy, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Trophy t = items.get(position);
        h.title.setText(t.getTitle());
        h.desc.setText(t.getDescription());

        final boolean unlocked = t.isUnlocked();

        if (unlocked) {
            h.status.setText("UNLOCKED");
            h.icon.setImageResource(R.drawable.ic_unlock);
//            h.icon.setImageTintList(null);
//            h.lock.setVisibility(View.GONE);
            h.progressContainer.setVisibility(View.GONE);
        } else {
            h.status.setText("LOCKED");
            h.icon.setImageResource(R.drawable.ic_lock);
//            h.icon.setImageTintList(ContextCompat.getColorStateList(h.itemView.getContext(), R.color.hintColor));
//            h.lock.setVisibility(View.VISIBLE);

            if (t.isIncremental()) {
                h.progressContainer.setVisibility(View.VISIBLE);
                int pct = (int) (100f * t.getCurrent() / Math.max(1, t.getTotal()));
                h.progress.setProgress(pct);
                h.progressText.setText(t.getCurrent() + " / " + t.getTotal());
            } else {
                h.progressContainer.setVisibility(View.GONE);
            }
        }

        // --- NEW: pulse + snackbar if this is the one that just unlocked ---
        boolean isRecent = t.getId().equals(recentlyUnlockedId) && unlocked;
        MaterialCardView card = (MaterialCardView) h.itemView;

        if (isRecent) {
            // Snackbar (anchor to bottom nav if present)
            View anchor = h.itemView.getRootView().findViewById(R.id.bottom_navigation);
            Snackbar sb = Snackbar.make(h.itemView, "🏆 " + t.getTitle() + " unlocked!", Snackbar.LENGTH_LONG);
            if (anchor != null) sb.setAnchorView(anchor);
            sb.show();

            // Haptic success (optional)
            h.itemView.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM);

            // Pulse highlight
            int strokePx = (int) (h.itemView.getResources().getDisplayMetrics().density * 2);
            card.setStrokeWidth(strokePx);
            card.setStrokeColor(ContextCompat.getColor(h.itemView.getContext(), R.color.vibrantAccent));
            h.itemView.setScaleX(0.94f);
            h.itemView.setScaleY(0.94f);
            h.itemView.animate().scaleX(1f).scaleY(1f).setDuration(220).start();

            // Clear highlight after a moment so it doesn’t stick or repeat
            h.itemView.postDelayed(() -> {
                card.setStrokeWidth(0);
                recentlyUnlockedId = null; // prevents repeat on rebind
            }, 1200);
        } else {
            card.setStrokeWidth(0);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, desc, status, progressText;
        ProgressBar progress;
        View progressContainer;

        Holder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivTrophyIcon);
//            lock = itemView.findViewById(R.id.ivLockOverlay);
            title = itemView.findViewById(R.id.tvTrophyTitle);
            desc = itemView.findViewById(R.id.tvTrophyDesc);
            status = itemView.findViewById(R.id.tvTrophyStatus);
            progress = itemView.findViewById(R.id.pbTrophyProgress);
            progressText = itemView.findViewById(R.id.tvTrophyProgressText);
            progressContainer = itemView.findViewById(R.id.layoutProgress);
        }
    }


}
