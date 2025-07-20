package com.example.myapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    private EditText editTextAge;
    private EditText editTextWeight;
    private EditText editTextHeight;
    private EditText editTextNewPassword;
    private EditText editTextGoal;
    private TimePicker timePicker;

    private TextView textViewCurrentAge;
    private TextView textViewCurrentWeight;
    private TextView textViewCurrentHeight;
    private TextView textViewCurrentPassword;
    private TextView textViewCurrentGoal;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editTextAge = findViewById(R.id.editTextAge);
        editTextWeight = findViewById(R.id.editTextWeight);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextGoal = findViewById(R.id.editTextGoals);
        timePicker = findViewById(R.id.timePicker);
        ImageButton backButton = findViewById(R.id.backButton);
        Button buttonSave = findViewById(R.id.buttonSave);

        textViewCurrentAge = findViewById(R.id.textViewCurrentAge);
        textViewCurrentWeight = findViewById(R.id.textViewCurrentWeight);
        textViewCurrentHeight = findViewById(R.id.textViewCurrentHeight);
        textViewCurrentPassword = findViewById(R.id.textViewCurrentPassword);
        textViewCurrentGoal = findViewById(R.id.textViewCurrentGoal);

        sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE);

        loadSettings();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_settings);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            public boolean onNavigationItemSelected(MenuItem item) {
                    if (item.getItemId()==R.id.navigation_settings) {
                        return true;
                    }

                    if (item.getItemId()==R.id.navigation_workouts) {
                        Intent workoutsIntent = new Intent(SettingsActivity.this, WorkoutsActivity.class);
                        startActivity(workoutsIntent);
                        return true;
                    }

                    if (item.getItemId()==R.id.navigation_home) {
                        Intent settingsIntent = new Intent(SettingsActivity.this, MainActivity.class);
                        startActivity(settingsIntent);
                        return true;
                    }
                    if (item.getItemId() == R.id.navigation_stopwatch) {
                        Intent settingsIntent = new Intent(SettingsActivity.this, StopwatchActivity.class);
                        startActivity(settingsIntent);
                        return true;
                    }
                    if (item.getItemId() == R.id.navigation_nutrition) {
                        Intent settingsIntent = new Intent(SettingsActivity.this, Stretching.class);
                        startActivity(settingsIntent);
                        return true;
                    }

                    return false;
            }
        });
    }

    private void loadSettings() {
        int age = sharedPreferences.getInt("age", 0);
        float weight = sharedPreferences.getFloat("weight", 0f);
        float height = sharedPreferences.getFloat("height", 0f);
        String password = sharedPreferences.getString("password", "");
        String goal = sharedPreferences.getString("goal", "");

        textViewCurrentAge.setText("Current Age: " + (age == 0 ? "Not set" : String.valueOf(age)));
        textViewCurrentWeight.setText("Current Weight: " + (weight == 0f ? "Not set" : String.valueOf(weight)));
        textViewCurrentHeight.setText("Current Height: " + (height == 0f ? "Not set" : String.valueOf(height)));
        textViewCurrentPassword.setText("Gender: " + (password.isEmpty() ? "Not set" : "****"));
        textViewCurrentGoal.setText("Current Goal: " + (goal.isEmpty() ? "Not set" : goal));

        editTextAge.setText(age == 0 ? "" : String.valueOf(age));
        editTextWeight.setText(weight == 0f ? "" : String.valueOf(weight));
        editTextHeight.setText(height == 0f ? "" : String.valueOf(height));
        editTextNewPassword.setText(password);
        editTextGoal.setText(goal);
    }

    private void saveSettings() {
        String ageText = editTextAge.getText().toString();
        String weightText = editTextWeight.getText().toString();
        String heightText = editTextHeight.getText().toString();
        String password = editTextNewPassword.getText().toString();
        String goal = editTextGoal.getText().toString();

        if (ageText.isEmpty() || weightText.isEmpty() || heightText.isEmpty() || password.isEmpty() || goal.isEmpty()) {
            Toast.makeText(SettingsActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageText);
        float weight = Float.parseFloat(weightText);
        float height = Float.parseFloat(heightText);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("age", age);
        editor.putFloat("weight", weight);
        editor.putFloat("height", height);
        editor.putString("password", password);
        editor.putString("goal", goal);
        editor.apply();

        // Set notification for the goal
        setNotification();

        textViewCurrentAge.setText("Current Age: " + String.valueOf(age));
        textViewCurrentWeight.setText("Current Weight: " + String.valueOf(weight));
        textViewCurrentHeight.setText("Current Height: " + String.valueOf(height));
        textViewCurrentPassword.setText("Current Password: " + (password.isEmpty() ? "Not set" : "****"));
        textViewCurrentGoal.setText("Current Goal: " + goal);

        Toast.makeText(SettingsActivity.this, "Settings Saved", Toast.LENGTH_SHORT).show();
    }

    private void setNotification() {
        Intent notificationIntent = new Intent(this, NotificationReciever.class);
        notificationIntent.putExtra("goal", editTextGoal.getText());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Use FLAG_IMMUTABLE
        );
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
        calendar.set(Calendar.MINUTE, timePicker.getMinute());
        calendar.set(Calendar.SECOND, 0);

        // Set the alarm or notification
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // Example: Setting a repeating alarm
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);



        Toast.makeText(this, "Notification set for " + timePicker.getHour() + ":" + timePicker.getMinute(), Toast.LENGTH_SHORT).show();
    }
}
