package com.example.myapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeViewModel extends ViewModel {
    
    private MutableLiveData<String> greeting = new MutableLiveData<>();
    private MutableLiveData<String> date = new MutableLiveData<>();
    private MutableLiveData<Integer> workoutCount = new MutableLiveData<>();
    private MutableLiveData<Integer> caloriesBurned = new MutableLiveData<>();
    private MutableLiveData<Integer> streakCount = new MutableLiveData<>();
    
    public HomeViewModel() {
        // Initialize with default values
        initializeData();
    }
    
    private void initializeData() {
        // Set greeting based on time of day
        setGreetingBasedOnTime();
        
        // Set current date
        setCurrentDate();
        
        // Set default values
        workoutCount.setValue(12);
        caloriesBurned.setValue(1450);
        streakCount.setValue(7);
    }
    
    private void setGreetingBasedOnTime() {
        int hour = new Date().getHours();
        String greetingText;
        
        if (hour < 12) {
            greetingText = "Good Morning";
        } else if (hour < 17) {
            greetingText = "Good Afternoon";
        } else {
            greetingText = "Good Evening";
        }
        
        greeting.setValue(greetingText);
    }
    
    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        date.setValue(currentDate);
    }
    
    // Getters for LiveData
    public LiveData<String> getGreeting() {
        return greeting;
    }
    
    public LiveData<String> getDate() {
        return date;
    }
    
    public LiveData<Integer> getWorkoutCount() {
        return workoutCount;
    }
    
    public LiveData<Integer> getCaloriesBurned() {
        return caloriesBurned;
    }
    
    public LiveData<Integer> getStreakCount() {
        return streakCount;
    }
    
    // Methods to update data
    public void updateWorkoutCount(int count) {
        workoutCount.setValue(count);
    }
    
    public void updateCaloriesBurned(int calories) {
        caloriesBurned.setValue(calories);
    }
    
    public void updateStreakCount(int streak) {
        streakCount.setValue(streak);
    }
    
    public void refreshData() {
        setGreetingBasedOnTime();
        setCurrentDate();
    }
}
