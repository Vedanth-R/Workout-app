package com.example.myapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapp.ui.screens.*


@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "home",
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("workout") { WorkoutScreen() }
        composable("food") { NutritionScreen() }
        composable("home") { HomeScreen() }
        composable("timer") { TimerScreen() }
        composable("stars") { StarsScreen() }
    }
}
