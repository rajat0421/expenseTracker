package com.yourmoney.app.ui.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourmoney.app.model.Expense
import com.yourmoney.app.model.paiseToRupees
import com.yourmoney.app.ui.dashboard.formatRupees
import com.yourmoney.app.ui.rememberFactoryViewModel
import com.yourmoney.app.ui.theme.BackgroundLight
import com.yourmoney.app.ui.theme.DeepGreen
import com.yourmoney.app.ui.theme.SoftGreen
import com.yourmoney.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen() {
    val viewModel: StatsViewModel =
        rememberFactoryViewModel { app -> StatsViewModel(app.repository, app.categoryRepository) }
    val period by viewModel.period.collectAsState()
    val buckets by viewModel.buckets.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val categoryNamesById = remember(categories) { categories.associate { it.id to it.name } }

    val maxTotal = (buckets.maxOfOrNull { it.totalPaise } ?: 0L).coerceAtLeast(1L)

    Scaffold(containerColor = BackgroundLight) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column {
                    Text("Statistics", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Spending over time, broken down daily, weekly or monthly.",
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

            items(buckets) { bucket ->
                StatBucketRow(
                    bucket = bucket,
                    maxTotal = maxTotal,
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
private fun StatBucketRow(
    bucket: StatBucket,
    maxTotal: Long,
    expensesInBucket: List<Expense>,
    categoryNamesById: Map<Long, String>,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = expensesInBucket.isNotEmpty()) { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(bucket.label, style = MaterialTheme.typography.bodyLarge)
                Text(
                    formatRupees(bucket.totalPaise.paiseToRupees()),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(modifier = Modifier.padding(top = 6.dp))
            LinearProgressIndicator(
                progress = { bucket.totalPaise.toFloat() / maxTotal.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = DeepGreen,
                trackColor = SoftGreen,
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    val byCategory = expensesInBucket
                        .groupBy { categoryNamesById[it.categoryId] ?: "Uncategorized" }
                        .mapValues { (_, items) -> items.sumOf { it.amountPaise } }
                        .toList()
                        .sortedByDescending { it.second }

                    byCategory.forEach { (name, amountPaise) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(name, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
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
