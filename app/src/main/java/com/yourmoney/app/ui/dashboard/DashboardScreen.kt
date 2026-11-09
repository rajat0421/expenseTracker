package com.yourmoney.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourmoney.app.model.Expense
import com.yourmoney.app.model.paiseToRupees
import com.yourmoney.app.ui.rememberFactoryViewModel
import com.yourmoney.app.ui.scan.ScanSheet
import com.yourmoney.app.ui.theme.BackgroundLight
import com.yourmoney.app.ui.theme.DeepGreen
import com.yourmoney.app.ui.theme.ExpenseRed
import com.yourmoney.app.ui.theme.SoftGreen
import com.yourmoney.app.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

fun formatRupees(amount: Double): String = "₹" + "%,.0f".format(amount)
fun formatPaise(amountPaise: Long): String = formatRupees(amountPaise.paiseToRupees())

@Composable
fun DashboardScreen(onNavigateToReview: () -> Unit = {}) {
    val viewModel: DashboardViewModel = rememberFactoryViewModel { app ->
        DashboardViewModel(app.repository, app.pendingExpenseRepository, app.categoryRepository)
    }
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val categoriesById = remember(categories) { categories.associateBy { it.id } }
    var showScanSheet by remember { mutableStateOf(false) }

    Scaffold(containerColor = BackgroundLight) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { DashboardHeader() }
            item { TotalSpentCard(uiState) }
            item { ScanExpensesButton(onClick = { showScanSheet = true }) }
            if (uiState.pendingReviewCount > 0) {
                item { PendingReviewBanner(count = uiState.pendingReviewCount, onClick = onNavigateToReview) }
            }
            item { QuickStatsRow(uiState) }
            if (uiState.categorySpends.isNotEmpty()) {
                item { CategoryOverviewCard(uiState) }
            }
            if (uiState.insights.isNotEmpty()) {
                item { InsightsCard(uiState) }
            }
            item { RecentTransactionsHeader() }
            items(uiState.recentExpenses) { expense ->
                ExpenseRow(expense, categoryName = categoriesById[expense.categoryId]?.name ?: "Uncategorized")
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showScanSheet) {
        ScanSheet(onDismiss = { showScanSheet = false })
    }
}

@Composable
private fun ScanExpensesButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SoftGreen, contentColor = DeepGreen),
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Scan this month's expenses")
    }
}

@Composable
private fun PendingReviewBanner(count: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "$count expense${if (count == 1) "" else "s"} awaiting review",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text("Review", style = MaterialTheme.typography.bodyMedium, color = DeepGreen, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun DashboardHeader() {
    Column {
        Text("Good day", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(
            "Your Expenses",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun TotalSpentCard(uiState: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepGreen),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Wallet, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Total spent this month", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                formatPaise(uiState.totalSpentThisMonth),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun QuickStatsRow(uiState: DashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            title = "Today's Spending",
            value = formatPaise(uiState.todaySpend),
            icon = Icons.Filled.Today,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            title = "Daily Average",
            value = formatRupees(uiState.dailyAverage),
            icon = Icons.Filled.Insights,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
private fun CategoryOverviewCard(uiState: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Spending by Category", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            uiState.categorySpends.take(5).forEach { spend ->
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(spend.categoryName, style = MaterialTheme.typography.bodyLarge)
                        Text(formatPaise(spend.amountPaise), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { spend.percent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = DeepGreen,
                        trackColor = SoftGreen,
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightsCard(uiState: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SoftGreen),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Insights, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Financial Insights", style = MaterialTheme.typography.titleMedium, color = DeepGreen)
            }
            Spacer(modifier = Modifier.height(12.dp))
            uiState.insights.forEach { insight ->
                Text(
                    "•  $insight",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun RecentTransactionsHeader() {
    Text("Recent Expenses", style = MaterialTheme.typography.titleMedium)
}

@Composable
fun ExpenseRow(expense: Expense, categoryName: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SoftGreen),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    categoryName.take(1),
                    color = DeepGreen,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.merchant, style = MaterialTheme.typography.bodyLarge)
                Text(
                    expense.date.format(DateTimeFormatter.ofPattern("dd MMM")) + " · " + categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            Text(
                "-" + formatPaise(expense.amountPaise),
                color = ExpenseRed,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
