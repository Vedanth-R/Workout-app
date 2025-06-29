package com.example.myapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class Goals extends AppCompatActivity {
    private EditText goalTitleEditText;
    private EditText goalTimeEditText; // For setting time in HH:MM format
    private Button setGoalButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        goalTitleEditText = findViewById(R.id.goal_title);
        goalTimeEditText = findViewById(R.id.goal_time);
        setGoalButton = findViewById(R.id.set_goal_button);

        setGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String goalTitle = goalTitleEditText.getText().toString();
                String goalTime = goalTimeEditText.getText().toString();

                if (!goalTitle.isEmpty() && !goalTime.isEmpty()) {
                    setGoalNotification(goalTitle, goalTime);
                    Toast.makeText(Goals.this, "Goal reminder set!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Goals.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setGoalNotification(String goalTitle, String time) {
        String[] timeParts = time.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        Intent intent = new Intent(Goals.this, NotificationReciever.class);
        intent.putExtra("goalTitle", goalTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}
