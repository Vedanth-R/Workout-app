package com.PeakForm.workoutapp.model;

public class Stretch {
    private String stretch;
    private String reps;
    private String weight;

    public Stretch(String exercise, String reps, String weight) {
        this.stretch = exercise;
        this.reps = reps;
        this.weight = weight;
    }

    public String getExercise() {
        return stretch;
    }

    public void setExercise(String exercise) {
        this.stretch = exercise;
    }

    public String getReps() {
        return reps;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }

    public String getDuration() {
        return weight;
    }

    public void setDuration(String weight) {
        this.weight = weight;
    }
}

