package com.example.myapp.achievements;

import java.util.Date;

public class PersonalRecord {
    public final String exerciseName; // e.g., "Bench Press"
    public final String metric;       // "maxWeight", "maxReps", "maxVolume"
    public final float value;
    public final Date date;

    public PersonalRecord(String exerciseName, String metric, float value, Date date) {
        this.exerciseName = exerciseName;
        this.metric = metric;
        this.value = value;
        this.date = date;
    }
}
