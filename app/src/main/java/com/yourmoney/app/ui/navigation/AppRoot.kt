package com.yourmoney.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yourmoney.app.ui.addexpense.AddExpenseSheet
import com.yourmoney.app.ui.categories.CategoryManagementScreen
import com.yourmoney.app.ui.dashboard.DashboardScreen
import com.yourmoney.app.ui.history.HistoryScreen
import com.yourmoney.app.ui.review.ReviewScreen
import com.yourmoney.app.ui.settings.SettingsScreen
import com.yourmoney.app.ui.stats.StatsScreen
import com.yourmoney.app.ui.theme.DeepGreen
import com.yourmoney.app.ui.yourMoneyApp

private sealed class Destination(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Destination("dashboard", "Home", Icons.Filled.Home)
    data object History : Destination("history", "History", Icons.Filled.List)
    data object Stats : Destination("stats", "Stats", Icons.Filled.BarChart)
    data object Review : Destination("review", "Review", Icons.Filled.FactCheck)
    data object Settings : Destination("settings", "Settings", Icons.Filled.Settings)
}

private const val CATEGORIES_ROUTE = "categories"

private val destinations =
    listOf(Destination.Dashboard, Destination.History, Destination.Stats, Destination.Review, Destination.Settings)

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    var showAddExpense by rememberSaveable { mutableStateOf(false) }
    val app = yourMoneyApp()
    val pendingCount by app.pendingExpenseRepository.observePendingCount().collectAsState(initial = 0)

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
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
                        icon = {
                            if (destination is Destination.Review && pendingCount > 0) {
                                BadgedBox(badge = { Badge { Text(pendingCount.toString()) } }) {
                                    Icon(destination.icon, contentDescription = destination.label)
                                }
                            } else {
                                Icon(destination.icon, contentDescription = destination.label)
                            }
                        },
                        label = { Text(destination.label) },
                        colors = NavigationBarItemDefaults.colors(
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
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable(Destination.Dashboard.route) {
                DashboardScreen(
                    onNavigateToReview = {
                        navController.navigate(Destination.Review.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Destination.History.route) { HistoryScreen() }
            composable(Destination.Stats.route) { StatsScreen() }
            composable(Destination.Review.route) { ReviewScreen() }
            composable(Destination.Settings.route) {
                SettingsScreen(
                    onManageCategories = {
                        navController.navigate(CATEGORIES_ROUTE) { launchSingleTop = true }
                    },
                )
            }
            composable(CATEGORIES_ROUTE) { CategoryManagementScreen() }
        }
    }

    if (showAddExpense) {
        AddExpenseSheet(onDismiss = { showAddExpense = false })
    }
}
