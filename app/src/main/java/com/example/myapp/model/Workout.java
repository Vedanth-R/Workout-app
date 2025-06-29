package com.example.myapp.model;

public class Workout {
    private String exercise;
    private String reps;
    private String weight;

    public Workout(String exercise, String reps, String weight) {
        this.exercise = exercise;
        this.reps = reps;
        this.weight = weight;
    }

    public String getExercise() {
        return exercise;
    }

    public void setExercise(String exercise) {
        this.exercise = exercise;
    }

    public String getReps() {
        return reps;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
