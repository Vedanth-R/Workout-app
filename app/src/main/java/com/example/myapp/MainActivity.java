package com.example.myapp;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapp.data.TrophyRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_main);

        // Configure status bar with enhanced handling
        setupAdvancedStatusBar();

        // Setup Navigation
        setupNavigation();

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        viewPager.setAdapter(new TabPagerAdapter(this));
        viewPager.setOffscreenPageLimit(4); // keep all five alive; timers and scrolls won’t reset

// When you swipe, update the bottom nav selection
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                int itemId = switch (position) {
                    case 0 -> R.id.navigation_workouts;
                    case 1 -> R.id.navigation_nutrition;
                    case 2 -> R.id.navigation_home;
                    case 3 -> R.id.navigation_timer;
                    case 4 -> R.id.navigation_achievements;
                    default -> R.id.navigation_home;
                };
                if (bottomNav.getSelectedItemId() != itemId) {
                    bottomNav.setSelectedItemId(itemId);
                }
            }
        });

	// When you tap a nav item, update the pager
        bottomNav.setOnItemSelectedListener(item -> {
            int page;
            if (item.getItemId() == R.id.navigation_workouts) page = 0;
            else if (item.getItemId() == R.id.navigation_nutrition) page = 1;
            else if (item.getItemId() == R.id.navigation_home) page = 2;
            else if (item.getItemId() == R.id.navigation_timer) page = 3;
            else if (item.getItemId() == R.id.navigation_achievements) page = 4;
            else page = 2;
            if (viewPager.getCurrentItem() != page) {
                viewPager.setCurrentItem(page, true);
            }
            return true;
        });

	// Optional: start on Home and remember it across process death
        int startIndex = 2;
        if (savedInstanceState != null) {
            startIndex = savedInstanceState.getInt("tabIndex", 2);
        }
        viewPager.setCurrentItem(startIndex, false);


    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        outState.putInt("tabIndex", viewPager.getCurrentItem());
    }


    @Override
    protected void onResume() {
        super.onResume();
        TrophyRepository.getInstance(this).onAppOpenedToday();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        NavController navController = navHostFragment.getNavController();
        
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // --- TROPHIES: record "Explorer" progress on destination changes ---
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            String tabKey = null;

            // Map your destination IDs to the repository keys
            int id = destination.getId();
            if (id == R.id.navigation_home)          tabKey = "home";
            else if (id == R.id.navigation_workouts)  tabKey = "workouts";
            else if (id == R.id.navigation_nutrition) tabKey = "nutrition";
            else if (id == R.id.navigation_timer)     tabKey = "timer";
            else if (id == R.id.navigation_achievements) tabKey = "achievements";

            if (tabKey != null) {
                TrophyRepository.getInstance(this).onTabVisited(tabKey);
            }
        });

        // (Optional) immediately record the first visible tab after setup:
        if (navController.getCurrentDestination() != null) {
            int id = navController.getCurrentDestination().getId();
            String firstTabKey = null;
            if (id == R.id.navigation_home)          firstTabKey = "home";
            else if (id == R.id.navigation_workouts)  firstTabKey = "workouts";
            else if (id == R.id.navigation_nutrition) firstTabKey = "nutrition";
            else if (id == R.id.navigation_timer)     firstTabKey = "timer";
            else if (id == R.id.navigation_achievements) firstTabKey = "achievements";
            if (firstTabKey != null) {
                TrophyRepository.getInstance(this).onTabVisited(firstTabKey);
            }
        }
    }
    
    private void setupAdvancedStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - Use WindowInsetsController
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            
            // Ensure status bar icons are white for better visibility
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
            controller.setAppearanceLightStatusBars(false); // false = white icons
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android 5+ - Use traditional methods
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            
            // For older versions, we need to check if we can set light status bar
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
            }
        }
    }

    // TROPHY TOAST
    private final TrophyRepository.TrophyUnlockListener activityUnlockListener = trophyId -> {
        // Ensure UI thread
        runOnUiThread(() -> {
            String name = displayNameFor(trophyId);
            BottomNavigationView bnv = findViewById(R.id.bottom_navigation);
            Snackbar sb = Snackbar.make(bnv, "Unlocked: " + name + " 🎉", Snackbar.LENGTH_SHORT);
            sb.setAnchorView(bnv); // <- keeps it above bottom nav
            sb.show();

            // subtle success haptic
            bnv.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM);
        });
    };

    private String displayNameFor(String id) {
        if (TrophyRepository.ID_EXPLORER.equals(id)) return "Explorer";
        if (TrophyRepository.ID_FIRST_STEPS.equals(id)) return "First Steps";
        if (TrophyRepository.ID_PLANNER.equals(id)) return "Planner";
        if (TrophyRepository.ID_TIMER_ROOKIE.equals(id)) return "Timer Rookie";
        return "Achievement";
    }

    @Override protected void onStart() {
        super.onStart();
        TrophyRepository.getInstance(this).addListener(activityUnlockListener);
    }

    @Override protected void onStop() {
        TrophyRepository.getInstance(this).removeListener(activityUnlockListener);
        super.onStop();
    }
}
