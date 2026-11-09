package com.yourmoney.app.ui.stats

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourmoney.app.model.Expense
import com.yourmoney.app.model.paiseToRupees
import com.yourmoney.app.ui.dashboard.formatRupees
import com.yourmoney.app.ui.rememberFactoryViewModel
import com.yourmoney.app.ui.theme.AccentGold
import com.yourmoney.app.ui.theme.BackgroundLight
import com.yourmoney.app.ui.theme.DeepGreen
import com.yourmoney.app.ui.theme.ExpenseRed
import com.yourmoney.app.ui.theme.SoftGreen
import com.yourmoney.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen() {
    val viewModel: StatsViewModel =
        rememberFactoryViewModel { app -> StatsViewModel(app.repository, app.categoryRepository) }
    val period by viewModel.period.collectAsState()
    val buckets by viewModel.buckets.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val categoryNamesById = remember(categories) { categories.associate { it.id to it.name } }

    Scaffold(containerColor = BackgroundLight) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column {
                    Text("Statistics", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "See where your money actually goes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }

            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    StatsPeriod.entries.forEachIndexed { index, p ->
                        SegmentedButton(
                            selected = period == p,
                            onClick = { viewModel.setPeriod(p) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = StatsPeriod.entries.size),
                            colors = SegmentedButtonDefaults.colors(activeContainerColor = SoftGreen, activeContentColor = DeepGreen),
                        ) {
                            Text(p.label)
                        }
                    }
                }
            }

            item { SummaryRow(summary = summary, period = period) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Spending Trend", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.padding(top = 4.dp))
                        SpendingBarChart(buckets = buckets)
                    }
                }
            }

            item {
                Text(
                    "Tap a period below to see what it was spent on",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }

            items(buckets) { bucket ->
                StatBucketRow(
                    bucket = bucket,
                    isCurrent = bucket == buckets.firstOrNull(),
                    expensesInBucket = remember(bucket, expenses) {
                        expenses.filter { !it.date.isBefore(bucket.startDate) && !it.date.isAfter(bucket.endDate) }
                    },
                    categoryNamesById = categoryNamesById,
                )
            }

            item { Spacer(modifier = Modifier.padding(bottom = 80.dp)) }
        }
    }
}

@Composable
private fun SummaryRow(summary: StatsSummary, period: StatsPeriod) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryCard(
            title = "Total shown",
            value = formatRupees(summary.totalPaise.paiseToRupees()),
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            title = "Avg / ${period.unitLabel}",
            value = formatRupees(summary.averagePaise.paiseToRupees()),
            modifier = Modifier.weight(1f),
        )
        TrendCard(summary = summary, period = period, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun TrendCard(summary: StatsSummary, period: StatsPeriod, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            val trend = summary.trendPercent
            if (trend == null) {
                Text("—", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            } else {
                val color = if (summary.trendIsIncrease) ExpenseRed else DeepGreen
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (summary.trendIsIncrease) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.width(16.dp),
                    )
                    Text(
                        "${"%.0f".format(trend)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color,
                    )
                }
            }
            Text("vs last ${period.unitLabel}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun SpendingBarChart(buckets: List<StatBucket>, modifier: Modifier = Modifier) {
    if (buckets.isEmpty()) return
    val ordered = remember(buckets) { buckets.reversed() }
    val maxValue = remember(ordered) { (ordered.maxOfOrNull { it.totalPaise } ?: 0L).coerceAtLeast(1L) }
    val barWidth = 22.dp
    val chartHeight = 150.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(top = 12.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ordered.forEachIndexed { index, bucket ->
            val isLast = index == ordered.lastIndex
            val fraction = (bucket.totalPaise.toFloat() / maxValue.toFloat()).coerceIn(0.02f, 1f)
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(barWidth + 10.dp)) {
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
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = if (isLast) {
                                        listOf(DeepGreen, DeepGreen)
                                    } else {
                                        listOf(AccentGold.copy(alpha = 0.9f), SoftGreen)
                                    },
                                ),
                            ),
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
}

@Composable
private fun StatBucketRow(
    bucket: StatBucket,
    isCurrent: Boolean,
    expensesInBucket: List<Expense>,
    categoryNamesById: Map<Long, String>,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = expensesInBucket.isNotEmpty()) { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) SoftGreen else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    bucket.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                )
                Text(
                    formatRupees(bucket.totalPaise.paiseToRupees()),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (bucket.totalPaise > 0) MaterialTheme.colorScheme.onSurface else TextSecondary,
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    val byCategory = expensesInBucket
                        .groupBy { it.categoryId }
                        .mapValues { (_, items) -> items.sumOf { it.amountPaise } }
                        .toList()
                        .sortedByDescending { it.second }

                    byCategory.forEach { (categoryId, amountPaise) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor(categoryId)),
                            )
                            Spacer(modifier = Modifier.padding(start = 8.dp))
                            Text(
                                categoryNamesById[categoryId] ?: "Uncategorized",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                formatRupees(amountPaise.paiseToRupees()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}
