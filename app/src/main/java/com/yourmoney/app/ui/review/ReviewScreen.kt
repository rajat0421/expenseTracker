package com.yourmoney.app.ui.review

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourmoney.app.model.Category
import com.yourmoney.app.model.ConfidenceLevel
import com.yourmoney.app.model.PendingExpense
import com.yourmoney.app.model.paiseToRupees
import com.yourmoney.app.ui.dashboard.formatRupees
import com.yourmoney.app.ui.rememberFactoryViewModel
import com.yourmoney.app.ui.theme.AccentGold
import com.yourmoney.app.ui.theme.BackgroundLight
import com.yourmoney.app.ui.theme.DeepGreen
import com.yourmoney.app.ui.theme.ExpenseRed
import com.yourmoney.app.ui.theme.SoftGreen
import com.yourmoney.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ReviewScreen() {
    val viewModel: ReviewViewModel = rememberFactoryViewModel { app -> ReviewViewModel(app.pendingExpenseRepository) }
    val pending by viewModel.pending.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var editingCandidate by remember { mutableStateOf<PendingExpense?>(null) }

    fun notify(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    val needsAttentionCount = pending.count { it.confidence == ConfidenceLevel.LOW }

    Scaffold(
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ReviewHeader(pendingCount = pending.size, needsAttentionCount = needsAttentionCount)

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
                        }) { Text("Approve") }
                        TextButton(onClick = {
                            val count = selectedIds.size
                            viewModel.rejectSelected()
                            notify("Rejected $count expense${if (count == 1) "" else "s"}")
                        }) { Text("Reject") }
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
                            selected = candidate.id in selectedIds,
                            onToggleSelected = { viewModel.toggleSelected(candidate.id) },
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
            onDismiss = { editingCandidate = null },
            onSave = { merchant, category, amountPaise, date ->
                viewModel.updateDraft(candidate.id, merchant, category, amountPaise, date)
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
            "Detected from SMS. Approve to add to your history, or reject to discard.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.padding(top = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatChip(label = "$pendingCount pending", color = DeepGreen, background = SoftGreen)
            if (needsAttentionCount > 0) {
                StatChip(
                    label = "$needsAttentionCount need${if (needsAttentionCount == 1) "s" else ""} attention",
                    color = AccentGold,
                    background = AccentGold.copy(alpha = 0.15f),
                    icon = Icons.Filled.WarningAmber,
                )
            }
        }
    }
}

@Composable
private fun StatChip(label: String, color: androidx.compose.ui.graphics.Color, background: androidx.compose.ui.graphics.Color, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
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
private fun PendingExpenseCard(
    candidate: PendingExpense,
    selected: Boolean,
    onToggleSelected: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onEditClick: () -> Unit,
) {
    var smsExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Checkbox(checked = selected, onCheckedChange = { onToggleSelected() })
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
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "-" + formatRupees(candidate.amountPaise.paiseToRupees()),
                        style = MaterialTheme.typography.titleMedium,
                        color = ExpenseRed,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                    ConfidenceBadge(candidate.confidence)
                }
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

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(10.dp), color = SoftGreen) {
                    Text(
                        candidate.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                candidate.paymentMethod?.let { method ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CreditCard, contentDescription = null, tint = TextSecondary, modifier = Modifier.width(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(method, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
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
                ActionButton(label = "Approve", icon = Icons.Filled.Check, tint = DeepGreen, onClick = onApprove)
                ActionButton(label = "Edit", icon = Icons.Filled.Edit, tint = TextSecondary, onClick = onEditClick)
                ActionButton(label = "Reject", icon = Icons.Filled.Close, tint = ExpenseRed, onClick = onReject)
            }
        }
    }
}

@Composable
private fun ActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tint.copy(alpha = 0.1f),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.width(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = tint)
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: ConfidenceLevel) {
    val (label, color) = when (confidence) {
        ConfidenceLevel.HIGH -> "High confidence" to DeepGreen
        ConfidenceLevel.MEDIUM -> "Medium confidence" to AccentGold
        ConfidenceLevel.LOW -> "Low confidence" to ExpenseRed
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.15f)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
