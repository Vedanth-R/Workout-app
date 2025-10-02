package com.PeakForm.workoutapp.model;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.PeakForm.workoutapp.R;

import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {


    public interface OnItemChangedListener {
        void onShoppingListChanged(List<String> newList);
    }

    private final List<String> items;
    private OnItemChangedListener listener;// position callback

    public ShoppingListAdapter(List<String> shoppingListItems, OnItemChangedListener listener) {
        this.items = shoppingListItems;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemTextView;
        AppCompatImageButton btnDelete;

        public ViewHolder(View view) {
            super(view);
            itemTextView = view.findViewById(R.id.shoppingItemText);
            btnDelete = itemView.findViewById(R.id.btnShoppingDelete);
        }
    }

    @Override
    public ShoppingListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ShoppingListAdapter.ViewHolder holder, int position) {
        String item = items.get(position);
        holder.itemTextView.setText(item);

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Delete Item")
                    .setMessage("Remove this item?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        items.remove(holder.getAdapterPosition());
                        notifyDataSetChanged();
                        if (listener != null) {
                            listener.onShoppingListChanged(items);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}