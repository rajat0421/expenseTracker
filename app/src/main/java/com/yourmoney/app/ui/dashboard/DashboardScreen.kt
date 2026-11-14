package com.yourmoney.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.yourmoney.app.ui.history.EditExpenseSheet
import com.yourmoney.app.ui.rememberFactoryViewModel
import com.yourmoney.app.ui.scan.ScanSheet
import com.yourmoney.app.ui.stats.categoryColor
import com.yourmoney.app.ui.theme.BackgroundLight
import com.yourmoney.app.ui.theme.DeepGreen
import com.yourmoney.app.ui.theme.ExpenseRed
import com.yourmoney.app.ui.theme.SoftGreen
import com.yourmoney.app.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

fun formatRupees(amount: Double): String = "₹" + "%,.0f".format(amount)
fun formatPaise(amountPaise: Long): String = formatRupees(amountPaise.paiseToRupees())

@Composable
fun DashboardScreen(
    onNavigateToReview: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
) {
    val viewModel: DashboardViewModel = rememberFactoryViewModel { app ->
        DashboardViewModel(app.repository, app.pendingExpenseRepository, app.categoryRepository)
    }
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val categoriesById = remember(categories) { categories.associateBy { it.id } }
    var showScanSheet by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }

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
                item { CategoryOverviewCard(uiState, onBreakdownClick = onNavigateToStats) }
            }
            item { RecentTransactionsHeader(onViewAllClick = onNavigateToHistory) }
            items(uiState.recentExpenses, key = { it.id }) { expense ->
                ExpenseRow(
                    expense,
                    categoryName = categoriesById[expense.categoryId]?.name ?: "Uncategorized",
                    onClick = { editingExpense = expense },
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showScanSheet) {
        ScanSheet(onDismiss = { showScanSheet = false })
    }

    editingExpense?.let { expense ->
        EditExpenseSheet(
            expense = expense,
            categories = categories,
            onDismiss = { editingExpense = null },
            onSave = { merchant, categoryId, amountPaise, date ->
                viewModel.updateExpense(expense, merchant, categoryId, amountPaise, date)
                editingExpense = null
            },
        )
    }
}

@Composable
private fun DashboardHeader() {
    Column {
        Text("Good day 🌱", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(
            "Your Expenses",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(ExpenseRed.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$count expense${if (count == 1) "" else "s"} awaiting review",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    "Auto-imported from SMS & UPI",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text("Review", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun TotalSpentCard(uiState: DashboardUiState) {
    val now = remember { java.time.LocalDate.now() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepGreen),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Wallet, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Total spent this month",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                MonthPill(now)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                formatPaise(uiState.totalSpentThisMonth),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            )
            uiState.trendPercent?.let { percent ->
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Divider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (percent <= 0) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${kotlin.math.abs(percent)}% ${if (percent <= 0) "less" else "more"} than last month",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthPill(date: java.time.LocalDate) {
    val label = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " + date.year
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
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
            badge = "Today",
            icon = Icons.Filled.CalendarMonth,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            title = "Daily Average",
            value = formatRupees(uiState.dailyAverage),
            badge = "Avg / Day",
            icon = Icons.Filled.ShowChart,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(title: String, value: String, badge: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SoftGreen),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(BackgroundLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(badge, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
private fun CategoryOverviewCard(uiState: DashboardUiState, onBreakdownClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Spending by Category", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    "Breakdown",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeepGreen,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onBreakdownClick),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            uiState.categorySpends.take(3).forEach { spend ->
                Column(modifier = Modifier.padding(bottom = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(categoryColor(spend.categoryId)),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(spend.categoryName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(
                            "${formatPaise(spend.amountPaise)} (${(spend.percent * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SoftGreen),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(spend.percent.coerceIn(0f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(categoryColor(spend.categoryId)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionsHeader(onViewAllClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Recent Expenses", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text(
            "View all",
            style = MaterialTheme.typography.bodyMedium,
            color = DeepGreen,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(onClick = onViewAllClick),
        )
    }
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
