package com.example.myapp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.model.Food;
import com.example.myapp.FoodAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class FoodBottomSheet extends BottomSheetDialogFragment {

    public enum Mode {SEARCH, LOGGED}

    private RecyclerView rvFood;
    private EditText etSearch;

    private FoodAdapter foodAdapter;
    private List<Food> foodList = new ArrayList<>();

    private String mealType;
    private Mode mode;
    private String currentMealEmoji;

    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();

    private static final String ARG_MEAL_TYPE = "meal_type";
    private static final String ARG_MODE = "mode";

    private OnFoodLoggedListener listener;

    public interface OnFoodLoggedListener {
        void onFoodLogged();
    }

    public void setListener(OnFoodLoggedListener listener) {
        this.listener = listener;
    }

    private static final String API_KEY = "9d76aaff6887c66ce729a1de941351b1";
    private static final String APP_ID = "39100170";
    private NutritionixApi api;

    public static FoodBottomSheet newInstance(String mealType, Mode mode) {
        FoodBottomSheet fragment = new FoodBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_MEAL_TYPE, mealType);
        args.putString(ARG_MODE, mode.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_food, container, false);

        sharedPreferences = requireActivity().getSharedPreferences("nutrition_prefs", getActivity().MODE_PRIVATE);

        if (getArguments() != null) {
            mealType = getArguments().getString(ARG_MEAL_TYPE);
            currentMealEmoji = getEmojiForMeal(mealType);
            mode = Mode.valueOf(getArguments().getString(ARG_MODE, Mode.SEARCH.name()));
        }

        rvFood = view.findViewById(R.id.rvFoodItems);
        etSearch = view.findViewById(R.id.etFoodSearch);

        foodAdapter = new FoodAdapter(
                foodList,
                getContext(),
                mode == Mode.LOGGED,
                food -> {
                    if (mode == Mode.SEARCH) logFood(food); // Only log in SEARCH mode
                },
                food -> {
                    if (mode == Mode.LOGGED) confirmDeleteFood(food); // Only delete in LOGGED mode
                },
                currentMealEmoji
        );

        rvFood.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFood.setAdapter(foodAdapter);

        if (mode == Mode.SEARCH) {
            // === SEARCH MODE: leave fully intact ===
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://trackapi.nutritionix.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            api = retrofit.create(NutritionixApi.class);

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 0) searchFood(s.toString());
                }
            });
        } else {
            // === LOGGED MODE: show saved foods, hide search ===
            etSearch.setVisibility(View.GONE);
            loadLoggedFoods();
        }

        return view;
    }

    private String getEmojiForMeal(String mealType) {
        switch (mealType.toLowerCase()) {
            case "breakfast": return "🌅";
            case "lunch": return "☀️";
            case "dinner": return "🌙";
            case "snacks": return "🍿";
            default: return "";
        }
    }

    // SEARCH MODE methods remain unchanged
    private void searchFood(String query) {
        api.searchFoods(query, APP_ID, API_KEY).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Food> foods = parseNutritionixResults(response.body());
                    foodAdapter.updateList(foods);
                } else {
                    Toast.makeText(getContext(),
                            "No results: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(),
                        "API error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logFood(Food food) {
        List<Food> logged = loadLoggedFoodsList();
        logged.add(food);
        saveLoggedFoodsList(logged);
        if (listener != null) listener.onFoodLogged();
        Toast.makeText(getContext(), "Added " + food.getName(), Toast.LENGTH_SHORT).show();
        dismiss();
    }

    // LOGGED MODE: delete with confirmation
    private void confirmDeleteFood(Food food) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete food?")
                .setMessage("Are you sure you want to delete " + food.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    List<Food> logged = loadLoggedFoodsList();
                    logged.remove(food);
                    saveLoggedFoodsList(logged);
                    foodAdapter.updateList(logged);
                    if (listener != null) listener.onFoodLogged();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private List<Food> loadLoggedFoodsList() {
        String json = sharedPreferences.getString(mealType + "_foods", "[]");
        Type type = new TypeToken<List<Food>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void saveLoggedFoodsList(List<Food> list) {
        sharedPreferences.edit().putString(mealType + "_foods", gson.toJson(list)).apply();
    }

    private void loadLoggedFoods() {
        foodAdapter.updateList(loadLoggedFoodsList());
    }

    // Parsing remains unchanged
    private List<Food> parseNutritionixResults(JsonObject body) {
        List<Food> foods = new ArrayList<>();

        JsonArray common = body.has("common") ? body.getAsJsonArray("common") : null;
        if (common != null) {
            for (JsonElement e : common) {
                JsonObject o = e.getAsJsonObject();
                String name = o.get("food_name").getAsString();
                double cal = o.has("nf_calories") ? o.get("nf_calories").getAsDouble() : 0;
                double protein = o.has("nf_protein") ? o.get("nf_protein").getAsDouble() : 0;
                double carbs = o.has("nf_total_carbohydrate") ? o.get("nf_total_carbohydrate").getAsDouble() : 0;
                double fat = o.has("nf_total_fat") ? o.get("nf_total_fat").getAsDouble() : 0;
                String details = String.format("%.0f kcal | %.0fg P | %.0fg C | %.0fg F", cal, protein, carbs, fat);
                foods.add(new Food(name, (int) cal, details));
            }
        }

        JsonArray branded = body.has("branded") ? body.getAsJsonArray("branded") : null;
        if (branded != null) {
            for (JsonElement e : branded) {
                JsonObject o = e.getAsJsonObject();
                String name = o.get("food_name").getAsString();
                double cal = o.has("nf_calories") ? o.get("nf_calories").getAsDouble() : 0;
                double protein = o.has("nf_protein") ? o.get("nf_protein").getAsDouble() : 0;
                double carbs = o.has("nf_total_carbohydrate") ? o.get("nf_total_carbohydrate").getAsDouble() : 0;
                double fat = o.has("nf_total_fat") ? o.get("nf_total_fat").getAsDouble() : 0;
                String details = String.format("%.0f kcal | %.0fg P | %.0fg C | %.0fg F", cal, protein, carbs, fat);
                foods.add(new Food(name, (int) cal, details));
            }
        }

        return foods;
    }

    interface NutritionixApi {
        @GET("v2/search/instant")
        Call<JsonObject> searchFoods(
                @Query("query") String query,
                @Header("x-app-id") String appId,
                @Header("x-app-key") String apiKey
        );
    }
}