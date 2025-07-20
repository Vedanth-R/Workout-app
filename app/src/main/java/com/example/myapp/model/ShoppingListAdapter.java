package com.example.myapp.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;

import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private List<String> items;

    public ShoppingListAdapter(List<String> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemTextView;
        public ViewHolder(View view) {
            super(view);
            itemTextView = view.findViewById(R.id.shoppingItemText);
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
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}