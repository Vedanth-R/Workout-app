package com.example.myapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapp.ui.theme.AppTypography
import com.example.myapp.ui.theme.backgroundDark
import com.example.myapp.ui.theme.onSecondaryContainerDark
import com.example.myapp.ui.theme.onTertiaryContainerDark
import com.example.myapp.ui.theme.onTertiaryDark
import com.example.myapp.ui.theme.primaryContainerDark
import com.example.myapp.ui.theme.primaryDark
import com.example.myapp.ui.theme.secondaryContainerDark
import com.example.myapp.ui.theme.secondaryDark
import com.example.myapp.ui.theme.tertiaryContainerDark
import com.example.myapp.ui.theme.tertiaryDark
import com.github.mikephil.charting.charts.LineChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.Icons

@Preview
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            //.background(color = backgroundDark)
            .background(Brush.verticalGradient(colors = listOf(Color(0xFFE1E6FE), Color(0xFFE1F5FE))))
            .padding(16.dp)
    ) {
        Text(
            text = "Good Morning, User \uD83D\uDC4B",
            style = AppTypography.displayLarge,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0288D1)
            //color = primaryDark
        )

        Text(
            text = "\uD83D\uDCAA 3-Day Workout Streak",
            style = AppTypography.displayMedium,
            fontSize = 16.sp,
            color = Color.DarkGray,
            //color = primaryDark,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        AndroidView(
            factory = { context -> LineChart(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 16.dp)
                .border(width = 4.dp, color = secondaryDark),
            update = { chart ->
                // Setup chart data here
            }
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                BentoCard("Workout Routine", "Full Body Workout", "Add Workout", modifier = Modifier.weight(.5f)) {}
                BentoCard("Nutrition", "1800 / 2200 kcal", "Log Meal", modifier = Modifier.weight(.5f)) {}
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                BentoCard("Timer", "HIIT 15:00", "Start", modifier = Modifier.weight(.5f)) {}
                BentoCard("Achievements", "5 Milestones", "View", modifier = Modifier.weight(.5f)) {}
            }
        }

        // Motivation Banner
        Surface(
            //color = tertiaryContainerDark,
            color = Color(0xff6e74f9),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(top = 24.dp).fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "You’re only one workout away from a good mood.",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = onTertiaryContainerDark
                )

                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Star icon",
                    tint = Color.Black // Or your desired tint
                )
            }
        }
    }
}


@Composable
fun BentoCard(title: String, subtitle: String, buttonText: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        //color = secondaryContainerDark,
        color = Color(0xfffeffff),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        modifier = modifier
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)//, color = onSecondaryContainerDark)
            Text(text = subtitle, fontSize = 14.sp)//, color = onSecondaryContainerDark)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xfff6f7fb),//tertiaryDark,
                    contentColor = Color.Black
                )
            ) {
                Text(buttonText)
            }
        }
    }
}










// ================================
// Jetpack Compose Version: Full Home Screen
// ================================

/*@Preview
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Brush.verticalGradient(colors = listOf(Color(0xFFB3E5FC), Color(0xFFE1F5FE))))
            .padding(16.dp)
    ) {
        // Greeting Section
        Text(
            text = "Good Morning, User 👋",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0288D1)
        )

        Text(
            text = "💪 3-Day Workout Streak",
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Line Chart Section
        AndroidView(
            factory = { context -> LineChart(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 16.dp),
            update = { chart ->
                // Setup chart data here
            }
        )

        // Bento Grid Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                BentoCard("Workout Routine", "Full Body Workout", "Add Workout") {}
                BentoCard("Nutrition", "1800 / 2200 kcal", "Log Meal") {}
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                BentoCard("Timer", "HIIT 15:00", "Start") {}
                BentoCard("Achievements", "5 Milestones", "View") {}
            }
        }

        // Motivation Banner
        Surface(
            color = Color(0xFFE3F2FD),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(top = 24.dp).fillMaxWidth()
        ) {
            Text(
                text = "You’re only one workout away from a good mood.",
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BentoCard(title: String, subtitle: String, buttonText: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .padding(4.dp)
            //.weight(1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = subtitle, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}*/
