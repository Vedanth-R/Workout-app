package com.example.myapp.model;

import java.util.ArrayList;
import java.util.List;

public class Workout {
    private String name;
    private List<Exercise> exercises;

    public Workout(String name) {
        this.name = name;
        this.exercises = new ArrayList<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Exercise> getExercises() { return exercises; }
    public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }

    public void addExercise(Exercise exercise) {
        this.exercises.add(exercise);
    }
}