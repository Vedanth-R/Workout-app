package com.PeakForm.workoutapp.model;

// Exercise.java
public class Exercise { // or Workout if you reuse it
    private String name;
    private String reps;
    private String weight;

    public Exercise(String name, String reps, String weight) {
        this.name = name;
        this.reps = reps;
        this.weight = weight;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getReps() { return reps; }
    public void setReps(String reps) { this.reps = reps; }

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Exercise exercise = (Exercise) o;
        return name != null && name.equalsIgnoreCase(exercise.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.toLowerCase().hashCode() : 0;
    }
}

