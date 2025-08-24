package com.example.myapp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
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
    private ProgressBar progressBarSearch;

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

    // USDA API key
    private static final String USDA_API_KEY = "F7bKl8ss5ZK2t3sgLb4GRsbOxDohs8JaRiErvo8Y";

    // Retrofit APIs
    private UsdaApi usdaApi;
    private OffApi offApi;

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

        progressBarSearch = view.findViewById(R.id.progressBarSearch);

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
                    if (mode == Mode.SEARCH) logFood(food);
                },
                food -> {
                    if (mode == Mode.LOGGED) confirmDeleteFood(food);
                },
                currentMealEmoji
        );

        rvFood.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFood.setAdapter(foodAdapter);

        setupApis();

        if (mode == Mode.SEARCH) {
            etSearch.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    String query = etSearch.getText().toString().trim();
                    if (query.isEmpty()) {
                        foodAdapter.updateList(new ArrayList<>()); // clear results
                    } else {
                        searchFood(query);
                    }
                    return true;
                }
                return false;
            });
        } else {
            etSearch.setVisibility(View.GONE);
            loadLoggedFoods();
        }

        return view;
    }

    private void startLoading() {
        progressBarSearch.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        progressBarSearch.setVisibility(View.GONE);
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

    private void setupApis() {
        Retrofit retrofitUsda = new Retrofit.Builder()
                .baseUrl("https://api.nal.usda.gov/fdc/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        usdaApi = retrofitUsda.create(UsdaApi.class);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .header("User-Agent", "PeakForm/1.0 (contact: vedanth.rao.v@gmail.com)")
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofitOff = new Retrofit.Builder()
                .baseUrl("https://world.openfoodfacts.org/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        offApi = retrofitOff.create(OffApi.class);
    }

    // === Search Food ===
    private void searchFood(String query) {
        if (query.isEmpty()) {
            foodAdapter.updateList(new ArrayList<>()); // clear results
            return;
        }

        startLoading(); // show spinner

        List<Food> finalList = new ArrayList<>();

        // Step 1: USDA Foundation
        usdaApi.searchFoods(query, USDA_API_KEY, "Foundation").enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Food> foundationFoods = filterUsdaByKeyword(parseUsdaResults(response.body()), query);
                    finalList.addAll(foundationFoods);
                }
                // Step 2: USDA Branded
                usdaApi.searchFoods(query, USDA_API_KEY, "Branded").enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Food> brandedFoods = filterUsdaByKeyword(parseUsdaResults(response.body()), query);
                            finalList.addAll(brandedFoods);
                        }
                        // Step 3: OFF
                        offApi.searchFoods(query, 1, 1).enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    List<Food> offFoods = parseOffResults(response.body());
                                    finalList.addAll(offFoods);
                                }

                                stopLoading(); // hide spinner

                                if (!finalList.isEmpty()) {
                                    foodAdapter.updateList(finalList);
                                } else {
                                    Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                stopLoading(); // hide spinner
                                if (!finalList.isEmpty()) {
                                    foodAdapter.updateList(finalList);
                                } else {
                                    Toast.makeText(getContext(), "OFF API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        // Even if Branded fails, go to OFF
                        offApi.searchFoods(query, 1, 1).enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    List<Food> offFoods = parseOffResults(response.body());
                                    finalList.addAll(offFoods);
                                }

                                stopLoading(); // hide spinner

                                if (!finalList.isEmpty()) {
                                    foodAdapter.updateList(finalList);
                                } else {
                                    Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                stopLoading(); // hide spinner
                                if (!finalList.isEmpty()) {
                                    foodAdapter.updateList(finalList);
                                } else {
                                    Toast.makeText(getContext(), "OFF API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // If Foundation fails, go directly to USDA Branded -> OFF
                searchUsdaBranded(query); // searchUsdaBranded already handles spinner properly
            }
        });
    }

    private List<Food> filterUsdaByKeyword(List<Food> foods, String query) {
        query = query.toLowerCase();
        List<Food> foundationMatches = new ArrayList<>();
        List<Food> otherMatches = new ArrayList<>();

        Map<String, String> genericOverrides = new HashMap<>();
        genericOverrides.put("rice", "Rice, white, long-grain, raw");
        genericOverrides.put("egg", "Egg, whole, raw, fresh");

        for (Food f : foods) {
            String name = f.getName().toLowerCase();
            if (genericOverrides.containsKey(query) && name.contains(genericOverrides.get(query).toLowerCase())) {
                foundationMatches.add(f);
                continue;
            }

            if (name.contains(query) && (name.contains("raw") || name.contains("unprepared"))) {
                foundationMatches.add(f);
            } else {
                otherMatches.add(f);
            }
        }

        final String queryLower = query;
        foundationMatches.sort((f1, f2) -> {
            String n1 = f1.getName().toLowerCase();
            String n2 = f2.getName().toLowerCase();
            if (n1.equals(queryLower)) return -1;
            if (n2.equals(queryLower)) return 1;
            return n1.indexOf(queryLower) - n2.indexOf(queryLower);
        });

        List<Food> finalList = new ArrayList<>();
        finalList.addAll(foundationMatches);
        finalList.addAll(otherMatches);
        return finalList;
    }

    private void searchUsdaBranded(String query) {
        usdaApi.searchFoods(query, USDA_API_KEY, "Branded").enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Food> foods = parseUsdaResults(response.body());
                    foods = filterUsdaByKeyword(foods, query);
                    if (!foods.isEmpty()) {
                        foodAdapter.updateList(foods);
                    } else {
                        searchOff(query);
                    }
                } else {
                    searchOff(query);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                searchOff(query);
            }
        });
    }

    private void searchOff(String query) {
        offApi.searchFoods(query, 1, 1).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Food> foods = parseOffResults(response.body());
                    if (!foods.isEmpty()) {
                        foodAdapter.updateList(foods);
                    } else {
                        Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "OFF search failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getContext(), "OFF API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // === Parse USDA ===
    private List<Food> parseUsdaResults(JsonObject body) {
        List<Food> foods = new ArrayList<>();
        if (!body.has("foods")) return foods;

        JsonArray array = body.getAsJsonArray("foods");
        for (JsonElement e : array) {
            JsonObject obj = e.getAsJsonObject();

            // Filter out non-English descriptions
            if (obj.has("description") && !obj.get("description").getAsString().matches(".*\\p{IsLatin}.*"))
                continue;

            String name = obj.has("description") ? obj.get("description").getAsString() : "Unknown";

            double cal = 0, protein = 0, carbs = 0, fat = 0;

            if (obj.has("foodNutrients")) {
                JsonArray nutrients = obj.getAsJsonArray("foodNutrients");
                for (JsonElement n : nutrients) {
                    JsonObject nutrient = n.getAsJsonObject();
                    String nutrientName = nutrient.has("nutrientName") ? nutrient.get("nutrientName").getAsString().toLowerCase() : "";
                    double value = nutrient.has("value") ? nutrient.get("value").getAsDouble() : 0;
                    String unit = nutrient.has("unitName") ? nutrient.get("unitName").getAsString().toLowerCase() : "";

                    switch (nutrientName) {
                        case "energy":
                        case "energy (kcal)":
                        case "energy kcal":
                            cal = value;
                            break;
                        case "protein":
                            protein = value;
                            break;
                        case "carbohydrate, by difference":
                            carbs = value;
                            break;
                        case "total lipid (fat)":
                            fat = value;
                            break;
                    }

                    // Fallback: if unit is kcal and calories not yet set
                    if (unit.equals("kcal") && cal == 0) {
                        cal = value;
                    }
                }
            }

            String details = String.format("%.0f kcal | %.0fg P | %.0fg C | %.0fg F", cal, protein, carbs, fat);
            foods.add(new Food(name, (int) cal, details));
        }

        return foods;
    }

    // === Parse OFF ===
    private List<Food> parseOffResults(JsonObject body) {
        List<Food> foods = new ArrayList<>();
        if (!body.has("products")) return foods;

        JsonArray array = body.getAsJsonArray("products");
        for (JsonElement e : array) {
            JsonObject obj = e.getAsJsonObject();
            String name = obj.has("product_name") ? obj.get("product_name").getAsString() : "Unknown";

            double cal = 0, protein = 0, carbs = 0, fat = 0;
            if (obj.has("nutriments")) {
                JsonObject n = obj.getAsJsonObject("nutriments");
                cal = n.has("energy-kcal_100g") ? n.get("energy-kcal_100g").getAsDouble() : 0;
                protein = n.has("proteins_100g") ? n.get("proteins_100g").getAsDouble() : 0;
                carbs = n.has("carbohydrates_100g") ? n.get("carbohydrates_100g").getAsDouble() : 0;
                fat = n.has("fat_100g") ? n.get("fat_100g").getAsDouble() : 0;
            }
            String details = String.format("%.0f kcal | %.0fg P | %.0fg C | %.0fg F", cal, protein, carbs, fat);
            foods.add(new Food(name, (int) cal, details));
        }
        return foods;
    }

    // === Logging / Deleting foods ===
    private void logFood(Food food) {
        List<Food> logged = loadLoggedFoodsList();
        logged.add(food);
        saveLoggedFoodsList(logged);
        if (listener != null) listener.onFoodLogged();
        Toast.makeText(getContext(), "Added " + food.getName(), Toast.LENGTH_SHORT).show();
        dismiss();
    }

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

    // === Retrofit Interfaces ===
    interface UsdaApi {
        @GET("foods/search")
        Call<JsonObject> searchFoods(
                @Query("query") String query,
                @Query("api_key") String apiKey,
                @Query("dataType") String dataType // "Foundation" or "Branded"
        );
    }

    interface OffApi {
        @GET("cgi/search.pl")
        Call<JsonObject> searchFoods(
                @Query("search_terms") String query,
                @Query("search_simple") int simple,
                @Query("json") int json
        );
    }
}