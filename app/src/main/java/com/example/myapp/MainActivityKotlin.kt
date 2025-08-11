package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapp.navigation.NavGraph
import com.example.myapp.ui.theme.AppTheme

class MainActivityKotlin : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme(
                darkTheme = true,
                dynamicColor = false
            ) {
                // Create NavController and pass it to your main container or directly use HomeScreenContainer for now
                val navController = rememberNavController()


                // Replace MainContainer() with your navigation setup or just HomeScreenContainer for testing
                NavGraph(navController = navController)
            }
        }
    }
}

@Composable
fun HomeScreenContainer(navController: NavController) {
    com.example.myapp.ui.screens.HomeScreen(
        onWorkoutClick = { navController.navigate("workouts") },
        onNutritionClick = { navController.navigate("nutrition") },
        onTimerClick = { navController.navigate("timer") },
        onAchievementsClick = { navController.navigate("achievements") }
    )
}