package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabPagerAdapter extends FragmentStateAdapter {

    public TabPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new WorkoutsFragment();
            case 1 -> new NutritionFragment();
            case 2 -> new HomeFragment();
            case 3 -> new TimerFragment();
            case 4 -> new AchievementsFragment();
            default -> throw new IllegalArgumentException("Bad tab index: " + position);
        };
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
