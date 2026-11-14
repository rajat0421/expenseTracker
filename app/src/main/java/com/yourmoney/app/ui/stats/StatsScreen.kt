package com.yourmoney.app.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourmoney.app.model.Expense
import com.yourmoney.app.model.paiseToRupees
import com.yourmoney.app.ui.dashboard.formatRupees
import com.yourmoney.app.ui.review.categoryIcon
import com.yourmoney.app.ui.rememberFactoryViewModel
import com.yourmoney.app.ui.theme.BackgroundLight
import com.yourmoney.app.ui.theme.DeepGreen
import com.yourmoney.app.ui.theme.ExpenseRed
import com.yourmoney.app.ui.theme.SoftGreen
import com.yourmoney.app.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val dayMonthYear = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val dayMonth = DateTimeFormatter.ofPattern("dd MMM")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen() {
    val viewModel: StatsViewModel =
        rememberFactoryViewModel { app -> StatsViewModel(app.repository, app.categoryRepository) }
    val period by viewModel.period.collectAsState()
    val referenceDate by viewModel.referenceDate.collectAsState()
    val buckets by viewModel.buckets.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val categoriesById = remember(categories) { categories.associateBy { it.id } }

    val current = buckets.firstOrNull()
    val currentExpenses = remember(current, expenses) {
        current?.let { expenses.inBucket(it) } ?: emptyList()
    }
    val breakdown = remember(currentExpenses, categoriesById) {
        categoryBreakdown(currentExpenses, categoriesById)
    }
    val topSpends = remember(currentExpenses) { currentExpenses.sortedByDescending { it.amountPaise } }

    var categoriesExpanded by remember(period) { mutableStateOf(false) }
    var topSpendsExpanded by remember(period) { mutableStateOf(false) }

    Scaffold(containerColor = BackgroundLight) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                StatsHeaderRow(
                    referenceDate = referenceDate,
                    onPickMonth = viewModel::setReferenceMonth,
                    onShiftMonth = viewModel::shiftReferenceMonth,
                )
            }

            item {
                PeriodTabsRow(period = period, onSelect = viewModel::setPeriod)
            }

            item {
                TotalSpentCard(
                    period = period,
                    current = current,
                    summary = summary,
                    breakdown = breakdown,
                    currentExpensesCount = currentExpenses.size,
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        TrendChartHeader(period = period, summary = summary)
                        Spacer(modifier = Modifier.padding(top = 4.dp))
                        SpendingBarChart(buckets = buckets, period = period)
                    }
                }
            }

            item {
                SpendingByCategoryCard(
                    period = period,
                    breakdown = if (categoriesExpanded) breakdown else breakdown.take(4),
                    hasMore = breakdown.size > 4,
                    expanded = categoriesExpanded,
                    onToggle = { categoriesExpanded = !categoriesExpanded },
                )
            }

            item {
                TopSpendsCard(
                    period = period,
                    expenses = if (topSpendsExpanded) topSpends else topSpends.take(3),
                    hasMore = topSpends.size > 3,
                    expanded = topSpendsExpanded,
                    onToggle = { topSpendsExpanded = !topSpendsExpanded },
                    categoryName = { categoriesById[it]?.name ?: "Uncategorized" },
                    categoryKey = { categoriesById[it]?.key },
                )
            }

            item { Spacer(modifier = Modifier.padding(bottom = 80.dp)) }
        }
    }
}

@Composable
private fun StatsHeaderRow(
    referenceDate: LocalDate,
    onPickMonth: (LocalDate) -> Unit,
    onShiftMonth: (Int) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val monthLabel = referenceDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " + referenceDate.year
    val isCurrentMonth = referenceDate.month == LocalDate.now().month && referenceDate.year == LocalDate.now().year

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Statistics", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))

        IconButton(onClick = { onShiftMonth(-1) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month", tint = TextSecondary)
        }

        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SoftGreen)
                    .clickable { menuExpanded = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(monthLabel, style = MaterialTheme.typography.bodyMedium, color = DeepGreen, fontWeight = FontWeight.Medium)
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                val today = LocalDate.now()
                (0 until 12).forEach { offset ->
                    val month = today.minusMonths(offset.toLong())
                    DropdownMenuItem(
                        text = { Text(month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " + month.year) },
                        onClick = {
                            menuExpanded = false
                            onPickMonth(month)
                        },
                    )
                }
            }
        }

        IconButton(onClick = { onShiftMonth(1) }, enabled = !isCurrentMonth, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Next month",
                tint = if (isCurrentMonth) TextSecondary.copy(alpha = 0.3f) else TextSecondary,
            )
        }
    }
}

@Composable
private fun PeriodTabsRow(period: StatsPeriod, onSelect: (StatsPeriod) -> Unit) {
    val entries = StatsPeriod.entries
    val selectedIndex = entries.indexOf(period)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
    ) {
        androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val segmentWidth = maxWidth / entries.size
            val indicatorOffset by androidx.compose.animation.core.animateDpAsState(
                targetValue = segmentWidth * selectedIndex,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 250),
                label = "periodTabIndicator",
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(segmentWidth)
                    .height(44.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SoftGreen),
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                entries.forEach { p ->
                    val selected = period == p
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelect(p) },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (selected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = DeepGreen,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            p.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) DeepGreen else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalSpentCard(
    period: StatsPeriod,
    current: StatBucket?,
    summary: StatsSummary,
    breakdown: List<CategoryBreakdown>,
    currentExpensesCount: Int,
) {
    val topCategory = breakdown.maxByOrNull { it.amountPaise }

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
                    when (period) {
                        StatsPeriod.DAILY -> "Total spent today"
                        StatsPeriod.WEEKLY -> "Total spent this period"
                        StatsPeriod.MONTHLY -> "Total spent this month"
                    },
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        when (period) {
                            StatsPeriod.DAILY -> current?.startDate?.format(dayMonthYear) ?: ""
                            else -> current?.label ?: ""
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatRupees((current?.totalPaise ?: 0L).paiseToRupees()),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                )
                if (period == StatsPeriod.MONTHLY && summary.trendPercent != null) {
                    Spacer(modifier = Modifier.width(10.dp))
                    val color = if (summary.trendIsIncrease) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)
                    Text(
                        "${if (summary.trendIsIncrease) "+" else "-"}${"%.1f".format(summary.trendPercent)}%",
                        color = color,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            if (period == StatsPeriod.DAILY) {
                Text(
                    "vs ${formatRupees(summary.averagePaise.paiseToRupees())} avg daily",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        when (period) {
                            StatsPeriod.DAILY -> "Count"
                            StatsPeriod.WEEKLY -> "Weekly Avg"
                            StatsPeriod.MONTHLY -> "Monthly Avg"
                        },
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        when (period) {
                            StatsPeriod.DAILY -> "$currentExpensesCount txns"
                            else -> formatRupees(summary.averagePaise.paiseToRupees())
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Top Category", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                    Text(
                        if (topCategory == null) {
                            "—"
                        } else if (period == StatsPeriod.DAILY) {
                            topCategory.name
                        } else {
                            "${topCategory.name} (${(topCategory.percent * 100).toInt()}%)"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendChartHeader(period: StatsPeriod, summary: StatsSummary) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                when (period) {
                    StatsPeriod.DAILY -> "Today's Trend"
                    StatsPeriod.WEEKLY -> "Spending Trend"
                    StatsPeriod.MONTHLY -> "Monthly Trend"
                },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                when (period) {
                    StatsPeriod.DAILY -> "Last 7 days daily outflow"
                    StatsPeriod.WEEKLY -> "Weekly comparison"
                    StatsPeriod.MONTHLY -> "Last 6 months comparison"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        summary.highest?.let { peak ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(ExpenseRed.copy(alpha = 0.1f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    "Peak: ${peak.axisLabel} (${formatCompactRupees(peak.totalPaise)})",
                    style = MaterialTheme.typography.labelSmall,
                    color = ExpenseRed,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun SpendingBarChart(buckets: List<StatBucket>, period: StatsPeriod, modifier: Modifier = Modifier) {
    if (buckets.isEmpty()) return
    val visible = remember(buckets, period) {
        if (period == StatsPeriod.DAILY) buckets.take(7) else buckets
    }
    val ordered = remember(visible) { visible.reversed() }
    val maxValue = remember(ordered) { (ordered.maxOfOrNull { it.totalPaise } ?: 0L).coerceAtLeast(1L) }
    val peakPaise = remember(ordered) { ordered.maxOfOrNull { it.totalPaise } ?: 0L }
    val barWidth = 28.dp
    val chartHeight = 150.dp
    val scrollState = rememberScrollState()
    androidx.compose.runtime.LaunchedEffect(ordered) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(top = 20.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ordered.forEachIndexed { index, bucket ->
            val isLast = index == ordered.lastIndex
            val isPeak = bucket.totalPaise == peakPaise && peakPaise > 0
            val fraction = (bucket.totalPaise.toFloat() / maxValue.toFloat()).coerceIn(0.02f, 1f)
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(barWidth + 14.dp)) {
                if (bucket.totalPaise > 0) {
                    val labelColor = when {
                        isLast -> DeepGreen
                        isPeak -> ExpenseRed
                        else -> TextSecondary
                    }
                    val labelBg = when {
                        isLast -> DeepGreen.copy(alpha = 0.12f)
                        isPeak -> ExpenseRed.copy(alpha = 0.1f)
                        else -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(labelBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            formatCompactRupees(bucket.totalPaise),
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor,
                            fontWeight = if (isLast || isPeak) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Box(
                    modifier = Modifier
                        .height(chartHeight)
                        .width(barWidth),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .height(chartHeight * fraction)
                            .width(barWidth)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(if (isLast) DeepGreen else SoftGreen),
                    )
                }
                Spacer(modifier = Modifier.padding(top = 6.dp))
                Text(
                    bucket.axisLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isLast) DeepGreen else TextSecondary,
                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }

    if (period == StatsPeriod.DAILY) {
        Spacer(modifier = Modifier.padding(top = 10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendDot(DeepGreen)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Active (${ordered.lastOrNull()?.startDate?.format(dayMonth) ?: ""})",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
            Spacer(modifier = Modifier.width(16.dp))
            LegendDot(SoftGreen)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Prior days", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun SpendingByCategoryCard(
    period: StatsPeriod,
    breakdown: List<CategoryBreakdown>,
    hasMore: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Spending by Category", style = MaterialTheme.typography.titleMedium)
                    if (period == StatsPeriod.DAILY) {
                        Text(
                            "${breakdown.size} categor${if (breakdown.size == 1) "y" else "ies"} recorded today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                    }
                }
                if (hasMore || expanded) {
                    Text(
                        if (expanded) "Show Less" else "View All",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepGreen,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(onClick = onToggle),
                    )
                }
            }
            if (breakdown.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("No expenses recorded for this period.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                breakdown.forEach { item ->
                    CategoryBreakdownRow(item = item, period = period)
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownRow(item: CategoryBreakdown, period: StatsPeriod) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(categoryColor(item.categoryId).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(categoryIcon(item.key), contentDescription = null, tint = categoryColor(item.categoryId), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (period == StatsPeriod.DAILY) "${(item.percent * 100).toInt()}% of today" else "${(item.percent * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatRupees(item.amountPaise.paiseToRupees()), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                if (period == StatsPeriod.DAILY) {
                    Text("${item.count} txn${if (item.count == 1) "" else "s"}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
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
                    .fillMaxWidth(item.percent.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(categoryColor(item.categoryId)),
            )
        }
    }
}

@Composable
private fun TopSpendsCard(
    period: StatsPeriod,
    expenses: List<Expense>,
    hasMore: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    categoryName: (Long) -> String,
    categoryKey: (Long) -> String?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Top Spends", style = MaterialTheme.typography.titleMedium)
                    Text(
                        when (period) {
                            StatsPeriod.DAILY -> "Highest expenses today"
                            StatsPeriod.WEEKLY -> "Highest expenses this period"
                            StatsPeriod.MONTHLY -> "Highest expenses this month"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
                if (hasMore || expanded) {
                    Text(
                        if (expanded) "Show Less" else "View All",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepGreen,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(onClick = onToggle),
                    )
                }
            }
            if (expenses.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("No expenses recorded for this period.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                expenses.forEachIndexed { index, expense ->
                    TopSpendRow(
                        expense = expense,
                        categoryName = categoryName(expense.categoryId),
                        categoryKey = categoryKey(expense.categoryId),
                    )
                    if (index != expenses.lastIndex) Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun TopSpendRow(expense: Expense, categoryName: String, categoryKey: String?) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(categoryColor(expense.categoryId).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(categoryIcon(categoryKey), contentDescription = null, tint = categoryColor(expense.categoryId), modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(expense.merchant, style = MaterialTheme.typography.bodyLarge)
            Text(
                expense.date.format(dayMonth) + " · " + categoryName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Text(formatRupees(expense.amountPaise.paiseToRupees()), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}
