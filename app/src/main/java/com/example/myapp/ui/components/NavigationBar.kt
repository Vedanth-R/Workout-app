package com.example.myapp.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

// Small data holder for each bottom nav entry.
data class NavItem(
    val route: String,                // unique route string used by NavController
    val label: String,                // short label shown under the icon
    val icon: ImageVector,            // icon when NOT selected
    val selectedIcon: ImageVector     // icon when selected
)

@Composable
fun BottomNavBar(
    items: List<NavItem>,
    currentRoute: String?,                    // current route (from navController)
    onItemSelected: (route: String) -> Unit   // called when user taps an item
) {
    NavigationBar (
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    {
        items.forEach { item ->
            val selected = item.route == currentRoute
            NavigationBarItem(
                icon = {
                    // show selectedIcon when selected, otherwise the regular icon
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                label = { Text(item.label, color = MaterialTheme.colorScheme.onPrimaryContainer) },
                selected = selected,
                onClick = { onItemSelected(item.route) },
                alwaysShowLabel = false,
            )
        }
    }
}