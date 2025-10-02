package com.PeakForm.workoutapp.model;

import java.util.Objects;

public class Food {
    private String name;
    private int calories;
    private String details; // e.g., "P: 5g C: 20g F: 3g"

    public Food(String name, int calories, String details) {
        this.name = name;
        this.calories = calories;
        this.details = details;
    }

    public String getName() { return name; }
    public int getCalories() { return calories; }
    public String getDetails() { return details; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Food other = (Food) obj;
        return name.equals(other.name) && calories == other.calories && details.equals(other.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, calories, details);
    }
}