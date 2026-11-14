package com.yourmoney.app.ui.review

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourmoney.app.data.local.CategoryEntity
import com.yourmoney.app.model.ConfidenceLevel
import com.yourmoney.app.model.PendingExpense
import com.yourmoney.app.model.paiseToRupees
import com.yourmoney.app.ui.dashboard.formatRupees
import com.yourmoney.app.ui.rememberFactoryViewModel
import com.yourmoney.app.ui.stats.categoryColor
import com.yourmoney.app.ui.theme.AccentGold
import com.yourmoney.app.ui.theme.BackgroundLight
import com.yourmoney.app.ui.theme.DeepGreen
import com.yourmoney.app.ui.theme.ExpenseRed
import com.yourmoney.app.ui.theme.SoftGreen
import com.yourmoney.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun ReviewScreen() {
    val viewModel: ReviewViewModel =
        rememberFactoryViewModel { app -> ReviewViewModel(app.pendingExpenseRepository, app.categoryRepository) }
    val pending by viewModel.pending.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val categoriesById = remember(categories) { categories.associateBy { it.id } }
    val selectedIds by viewModel.selectedIds.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var editingCandidate by remember { mutableStateOf<PendingExpense?>(null) }

    fun notify(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    val needsAttentionCount = pending.count { it.confidence == ConfidenceLevel.LOW }
    val allSelected = pending.isNotEmpty() && selectedIds.containsAll(pending.map { it.id })

    Scaffold(
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ReviewHeader(pendingCount = pending.size, needsAttentionCount = needsAttentionCount)

            if (pending.isNotEmpty()) {
                SelectAllAndSortRow(
                    allSelected = allSelected,
                    onToggleSelectAll = { viewModel.toggleSelectAll(pending.map { it.id }) },
                    sortOption = sortOption,
                    onSortOptionChange = viewModel::setSortOption,
                )
            }

            AnimatedVisibility(visible = selectedIds.isNotEmpty()) {
                Surface(color = SoftGreen) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${selectedIds.size} selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeepGreen,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = {
                            val count = selectedIds.size
                            viewModel.approveSelected()
                            notify("Approved $count expense${if (count == 1) "" else "s"}")
                        }) { Text("Approve (${selectedIds.size})") }
                        TextButton(onClick = {
                            val count = selectedIds.size
                            viewModel.rejectSelected()
                            notify("Rejected $count expense${if (count == 1) "" else "s"}")
                        }) { Text("Reject (${selectedIds.size})") }
                    }
                }
            }

            if (pending.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No expenses waiting for review.",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.padding(top = 4.dp))
                        Text(
                            "Scan your SMS from the dashboard to find new expenses.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(pending, key = { it.id }) { candidate ->
                        PendingExpenseCard(
                            candidate = candidate,
                            category = categoriesById[candidate.categoryId],
                            categories = categories,
                            selected = candidate.id in selectedIds,
                            onToggleSelected = { viewModel.toggleSelected(candidate.id) },
                            onCategoryChange = { newCategoryId ->
                                viewModel.updateDraft(candidate.id, candidate.merchant, newCategoryId, candidate.amountPaise)
                            },
                            onApprove = {
                                viewModel.approve(candidate.id)
                                notify("Approved ${candidate.merchant}")
                            },
                            onReject = {
                                viewModel.reject(candidate.id)
                                notify("Rejected ${candidate.merchant}")
                            },
                            onEditClick = { editingCandidate = candidate },
                        )
                    }
                    item { Spacer(modifier = Modifier.padding(bottom = 80.dp)) }
                }
            }
        }
    }

    editingCandidate?.let { candidate ->
        EditPendingExpenseSheet(
            candidate = candidate,
            categories = categories,
            onDismiss = { editingCandidate = null },
            onSave = { merchant, categoryId, amountPaise, date ->
                viewModel.updateDraft(candidate.id, merchant, categoryId, amountPaise, date)
                editingCandidate = null
                notify("Expense updated")
            },
        )
    }
}

@Composable
private fun ReviewHeader(pendingCount: Int, needsAttentionCount: Int) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("Review Expenses", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Detected from SMS. Review and approve to add to your history.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.padding(top = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatChip(label = "$pendingCount Pending Review", color = DeepGreen, background = SoftGreen)
            if (needsAttentionCount > 0) {
                StatChip(
                    label = "$needsAttentionCount Needs Attention",
                    color = AccentGold,
                    background = AccentGold.copy(alpha = 0.15f),
                    icon = Icons.Filled.WarningAmber,
                )
            }
        }
    }
}

@Composable
private fun StatChip(label: String, color: Color, background: Color, icon: ImageVector? = null) {
    Surface(shape = RoundedCornerShape(12.dp), color = background) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(it, contentDescription = null, tint = color, modifier = Modifier.width(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SelectAllAndSortRow(
    allSelected: Boolean,
    onToggleSelectAll: () -> Unit,
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onToggleSelectAll() },
        ) {
            Checkbox(checked = allSelected, onCheckedChange = { onToggleSelectAll() })
            Text("Select all", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }

        Box {
            Row(
                modifier = Modifier.clickable { sortMenuExpanded = true },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Sort by: ${sortOption.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Sort options", tint = TextSecondary)
            }
            DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSortOptionChange(option)
                            sortMenuExpanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingExpenseCard(
    candidate: PendingExpense,
    category: CategoryEntity?,
    categories: List<CategoryEntity>,
    selected: Boolean,
    onToggleSelected: () -> Unit,
    onCategoryChange: (Long) -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onEditClick: () -> Unit,
) {
    var smsExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Checkbox(checked = selected, onCheckedChange = { onToggleSelected() })
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(categoryColor(candidate.categoryId).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        categoryIcon(category?.key),
                        contentDescription = null,
                        tint = categoryColor(candidate.categoryId),
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        candidate.merchant,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        candidate.transactionAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
                Text(
                    "-" + formatRupees(candidate.amountPaise.paiseToRupees()),
                    style = MaterialTheme.typography.titleMedium,
                    color = ExpenseRed,
                    fontWeight = FontWeight.Bold,
                )
            }

            val bankLine = buildString {
                candidate.bankName?.let { append(it) } ?: candidate.bankSender?.let { append(it) }
                candidate.accountRef?.let {
                    if (isNotEmpty()) append(" · ")
                    append(it)
                }
            }
            if (bankLine.isNotEmpty()) {
                Spacer(modifier = Modifier.padding(top = 8.dp))
                Text(bankLine, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }

            Spacer(modifier = Modifier.padding(top = 10.dp))

            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SoftGreen)
                        .clickable { categoryMenuExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        category?.name ?: "Uncategorized",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepGreen,
                    )
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Change category",
                        tint = DeepGreen,
                        modifier = Modifier.size(16.dp),
                    )
                }
                DropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                categoryMenuExpanded = false
                                onCategoryChange(cat.id)
                            },
                        )
                    }
                }
            }

            if (!candidate.originalSmsBody.isNullOrBlank()) {
                Spacer(modifier = Modifier.padding(top = 10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { smsExpanded = !smsExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Original SMS", style = MaterialTheme.typography.bodyMedium, color = DeepGreen, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        if (smsExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (smsExpanded) "Collapse" else "Expand",
                        tint = DeepGreen,
                    )
                }
                AnimatedVisibility(visible = smsExpanded) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            candidate.bankSender.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                        Spacer(modifier = Modifier.padding(top = 4.dp))
                        Surface(shape = RoundedCornerShape(12.dp), color = BackgroundLight) {
                            Text(
                                candidate.originalSmsBody.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .heightIn(max = 220.dp)
                                    .verticalScroll(rememberScrollState()),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledActionButton(
                    label = "Approve",
                    icon = Icons.Filled.Check,
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                )
                OutlinedActionButton(
                    label = "Edit",
                    icon = Icons.Filled.Edit,
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                )
                TintedActionButton(
                    label = "Reject",
                    icon = Icons.Filled.Close,
                    tint = ExpenseRed,
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FilledActionButton(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = DeepGreen,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun OutlinedActionButton(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
private fun TintedActionButton(label: String, icon: ImageVector, tint: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = tint.copy(alpha = 0.12f),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = tint)
        }
    }
}
