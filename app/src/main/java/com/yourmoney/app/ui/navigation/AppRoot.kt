package com.yourmoney.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yourmoney.app.ui.addexpense.AddExpenseSheet
import com.yourmoney.app.ui.dashboard.DashboardScreen
import com.yourmoney.app.ui.history.HistoryScreen
import com.yourmoney.app.ui.theme.DeepGreen

private sealed class Destination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Dashboard : Destination("dashboard", "Home", Icons.Filled.Home)
    data object History : Destination("history", "History", Icons.Filled.List)
}

private val destinations = listOf(Destination.Dashboard, Destination.History)

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    var showAddExpense by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination

                destinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepGreen,
                            selectedTextColor = DeepGreen,
                            indicatorColor = Color(0xFFDDEEE2),
                        ),
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExpense = true },
                containerColor = DeepGreen,
                contentColor = Color.White,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add expense")
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Dashboard.route,
            modifier = androidx.compose.ui.Modifier.padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable(Destination.Dashboard.route) { DashboardScreen() }
            composable(Destination.History.route) { HistoryScreen() }
        }
    }

    if (showAddExpense) {
        AddExpenseSheet(onDismiss = { showAddExpense = false })
    }
}
