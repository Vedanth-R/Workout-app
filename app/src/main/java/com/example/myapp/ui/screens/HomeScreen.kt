package com.example.myapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.ui.components.BentoCard
import com.example.myapp.ui.theme.AppTypography
import com.github.mikephil.charting.charts.LineChart
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.background)
                )
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Good Morning, User",
            style = AppTypography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "3-Day Workout Streak",
            style = AppTypography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        AndroidView(
            factory = { context -> LineChart(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 16.dp)
                .border(width = 4.dp, color = MaterialTheme.colorScheme.primary),
            update = { chart ->
                // Setup chart data here
            }
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                BentoCard(
                    "Workout Routine",
                    "Full Body Workout",
                    "Add Workout",
                    modifier = Modifier.weight(.5f)
                ) {}
                BentoCard(
                    "Nutrition",
                    "1800 / 2200 kcal",
                    "Log Meal",
                    modifier = Modifier.weight(.5f)
                ) {}
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                BentoCard(
                    "Timer",
                    "HIIT 15:00",
                    "Start",
                    modifier = Modifier.weight(.5f)) {}
                BentoCard(
                    "Achievements",
                    "5 Milestones",
                    "View",
                    modifier = Modifier.weight(.5f)
                ) {}
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
            tonalElevation =  4.dp,
            shadowElevation = 4.dp
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
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Star icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
