package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.model.Food;
import com.google.android.material.button.MaterialButton;
import com.example.myapp.R;

import java.util.List;
import java.util.function.Consumer;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private final List<Food> foodList;
    private final Context context;
    private final boolean isLoggedMode;
    private final Consumer<Food> onItemClick;
    private final Consumer<Food> onDeleteClick;
    private final String mealEmoji;

    public FoodAdapter(List<Food> foodList, Context context, boolean isLoggedMode,
                       Consumer<Food> onItemClick, Consumer<Food> onDeleteClick,
                       String mealEmoji) {
        this.foodList = foodList;
        this.context = context;
        this.isLoggedMode = isLoggedMode;
        this.onItemClick = onItemClick;
        this.onDeleteClick = onDeleteClick;
        this.mealEmoji = mealEmoji;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);

        // Emoji based on meal card
        holder.tvMealEmoji.setText(mealEmoji);

        // Food name
        holder.tvFoodName.setText(food.getName());

        // Show full details (includes calories, protein, carbs, fat)
        holder.tvFoodDetails.setText(food.getDetails());

        // Click to log/search
        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.accept(food);
        });

        // Delete only for logged mode
        holder.btnDelete.setVisibility(isLoggedMode ? View.VISIBLE : View.GONE);
        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteClick != null) onDeleteClick.accept(food);
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public void updateList(List<Food> newList) {
        foodList.clear();
        foodList.addAll(newList);
        notifyDataSetChanged();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvMealEmoji, tvFoodName, tvFoodDetails;
        MaterialButton btnDelete;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealEmoji = itemView.findViewById(R.id.tvMealEmoji);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodDetails = itemView.findViewById(R.id.tvFoodDetails);
            btnDelete = itemView.findViewById(R.id.btnFoodMoreOptions);
        }
    }
}