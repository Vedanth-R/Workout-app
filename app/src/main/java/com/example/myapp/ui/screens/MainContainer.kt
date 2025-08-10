package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapp.navigation.NavGraph
import com.example.myapp.ui.components.BottomNavBar
import com.example.myapp.ui.components.NavItem

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MainContainer() {
    val navController = rememberNavController()

    val navItems = listOf(
        NavItem("workout", "Routines", Icons.Outlined.FitnessCenter, Icons.Filled.FitnessCenter),
        NavItem("food", "Nutrition", Icons.Outlined.Restaurant, Icons.Filled.Restaurant),
        NavItem("home", "Home", Icons.Outlined.Home, Icons.Filled.Home),
        NavItem("timer", "Stopwatch", Icons.Outlined.AccessTime, Icons.Filled.AccessTime),
        NavItem("stars", "Milestones", Icons.Outlined.EmojiEvents, Icons.Filled.EmojiEvents)
    )

    Scaffold(
        /*topBar = {
            TopAppBar(
                title = { Text("Peak Form") },
                navigationIcon = {
                    IconButton(onClick = { *//* handle menu *//* }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },*/
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Fab action */ }, containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            BottomNavBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavGraph(navController = navController, startDestination = "home")
        }
    }
}
