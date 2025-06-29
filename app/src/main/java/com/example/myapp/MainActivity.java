package com.example.myapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.ai.client.generativeai.*;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.PlacesStatusCodes;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.security.auth.callback.Callback;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText locationInput;
    private Spinner radiusSpinner;
    private Button searchButton;

    private PlacesClient placesClient;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private EditText editText;
    private Button sendButton;
    private List<String> messageList = new ArrayList<>();


    private static final String API_KEY = "AIzaSyCPI2IuX6eqAc7NMa8ePAg0l3O9LMvXcXU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Initialize the Places API
        Places.initialize(getApplicationContext(), "AIzaSyBtDHVC_z3vpYPZx0HhQEo33BDq0mX8gdU");
        placesClient = Places.createClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }



        // Setup location input and search button
        locationInput = findViewById(R.id.locationInput);
        radiusSpinner = findViewById(R.id.radiusSpinner);
        searchButton = findViewById(R.id.searchButton);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, chatbot.class);
                startActivity(intent);

            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            public boolean onNavigationItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    return true;
                }
                if (item.getItemId() == R.id.navigation_workouts) {
                    Intent workoutsIntent = new Intent(MainActivity.this, WorkoutsActivity.class);
                    startActivity(workoutsIntent);
                    return true;
                }
                if (item.getItemId() == R.id.navigation_settings) {
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    return true;
                }
                if (item.getItemId() == R.id.navigation_stopwatch) {
                    Intent settingsIntent = new Intent(MainActivity.this, StopwatchActivity.class);
                    startActivity(settingsIntent);
                    return true;
                }
                if (item.getItemId() == R.id.navigation_stretches) {
                    Intent settingsIntent = new Intent(MainActivity.this, Stretching.class);
                    startActivity(settingsIntent);
                    return true;
                }
                return false;
            }
        });

        searchButton.setOnClickListener(v -> {
            searchLocations();
        });
        radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.vibrantAccent));
                String selectedRadius = parent.getItemAtPosition(position).toString();

                int radius = Integer.parseInt(selectedRadius.split(" ")[0]);
                runOnUiThread(() -> radiusSpinner.setSelection(position));

                Log.d("MainActivity", "Selected radius: " + radius);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }


    private void searchLocations() {
        String location = locationInput.getText().toString();
        if (location.isEmpty()) {
            Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
            return;
        }


        LatLng latLng = getLatLngFromAddress(location);
        if (latLng != null) {
            mMap.addMarker(new MarkerOptions().position(latLng).title("Search Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));


            Spinner spinnerRadius = findViewById(R.id.radiusSpinner);
            String selectedRadius = spinnerRadius.getSelectedItem().toString();
            int radius = Integer.parseInt(selectedRadius.replace(" mi", ""));


            // Search for athletic fields, parks, or gyms nearby
            searchNearbyPlaces(latLng.latitude, latLng.longitude, radius, "park");

            // Search for gyms
            searchNearbyPlaces(latLng.latitude, latLng.longitude, radius, "gym");
        } else {
            Toast.makeText(this, "Invalid location. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private LatLng getLatLngFromAddress(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address location = addressList.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void searchNearbyPlaces(double latitude, double longitude, int radius, String placeType) {
        // Your API key
        String apiKey = "AIzaSyBtDHVC_z3vpYPZx0HhQEo33BDq0mX8gdU";

        // URL for the Nearby Search API
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
                + "location=" + latitude + "," + longitude
                + "&radius=" + (radius * 1609)
                + "&type=" + placeType
                + "&key=" + apiKey;

        // Create a request using Volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Parse the response
                        JSONArray results = response.getJSONArray("results");

                        // Loop through the results and add markers for each place
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);
                            JSONObject geometry = place.getJSONObject("geometry");
                            JSONObject location = geometry.getJSONObject("location");
                            double placeLat = location.getDouble("lat");
                            double placeLng = location.getDouble("lng");

                            String placeName = place.getString("name");

                            // Add marker for the place
                            LatLng placeLatLng = new LatLng(placeLat, placeLng);
                            mMap.addMarker(new MarkerOptions().position(placeLatLng).title(placeName));
                        }

                        // Optionally move the camera (if it's the first result or first search)
                        if (results.length() > 0) {
                            JSONObject firstPlace = results.getJSONObject(0);
                            JSONObject firstLocation = firstPlace.getJSONObject("geometry").getJSONObject("location");
                            double firstLat = firstLocation.getDouble("lat");
                            double firstLng = firstLocation.getDouble("lng");

                            LatLng firstLatLng = new LatLng(firstLat, firstLng);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 12));  // Zoom closer to the results
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error parsing results", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle any errors
                    Toast.makeText(getApplicationContext(), "Error fetching nearby places", Toast.LENGTH_SHORT).show();
                });

        // Add the request to the request queue
        requestQueue.add(jsonObjectRequest);
    }


    private void displayPlacesOnMap(String jsonResponse) {
        try {
            // Parse the JSON response
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray results = jsonObject.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject place = results.getJSONObject(i);
                JSONObject geometry = place.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");

                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");
                String name = place.getString("name");

                // Create LatLng for the place and add a marker on the map
                LatLng placeLatLng = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(placeLatLng).title(name));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing place data", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            }
        }
    }
}



