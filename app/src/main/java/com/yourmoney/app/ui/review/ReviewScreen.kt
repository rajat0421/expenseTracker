package com.yourmoney.app.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourmoney.app.model.Category
import com.yourmoney.app.model.ConfidenceLevel
import com.yourmoney.app.model.PendingExpense
import com.yourmoney.app.model.paiseToRupees
import com.yourmoney.app.ui.rememberFactoryViewModel
import com.yourmoney.app.ui.theme.BackgroundLight
import com.yourmoney.app.ui.theme.DeepGreen
import com.yourmoney.app.ui.theme.ExpenseRed
import com.yourmoney.app.ui.theme.SoftGreen
import com.yourmoney.app.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

@Composable
fun ReviewScreen() {
    val viewModel: ReviewViewModel = rememberFactoryViewModel { app -> ReviewViewModel(app.pendingExpenseRepository) }
    val pending by viewModel.pending.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()

    Scaffold(containerColor = BackgroundLight) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text("Review Expenses", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Detected from SMS. Approve to add to your history, or reject to discard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }

            if (selectedIds.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextButton(onClick = { viewModel.approveSelected() }) { Text("Approve Selected (${selectedIds.size})") }
                    TextButton(onClick = { viewModel.rejectSelected() }) { Text("Reject Selected (${selectedIds.size})") }
                }
            }

            if (pending.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No expenses waiting for review.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
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
                            onApprove = { viewModel.approve(candidate.id) },
                            onReject = { viewModel.reject(candidate.id) },
                            onEdit = { merchant, category, amountPaise ->
                                viewModel.updateDraft(candidate.id, merchant, category, amountPaise)
                            },
                        )
                    }
                    item { Spacer(modifier = Modifier.padding(bottom = 80.dp)) }
                }
            }
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
    onEdit: (merchant: String, category: Category, amountPaise: Long) -> Unit,
) {
    var merchantText by remember(candidate.id) { mutableStateOf(candidate.merchant) }
    var amountText by remember(candidate.id) { mutableStateOf("%.2f".format(candidate.amountPaise.paiseToRupees())) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = selected, onCheckedChange = { onToggleSelected() })
                OutlinedTextField(
                    value = merchantText,
                    onValueChange = {
                        merchantText = it
                        amountText.toDoubleOrNull()?.let { amount ->
                            onEdit(it, candidate.category, Math.round(amount * 100.0))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepGreen),
                )
                ConfidenceBadge(candidate.confidence)
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = value
                            value.toDoubleOrNull()?.let { amount ->
                                onEdit(merchantText, candidate.category, Math.round(amount * 100.0))
                            }
                        }
                    },
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.width(140.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepGreen),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box {
                    TextButton(onClick = { categoryMenuExpanded = true }) {
                        Text(candidate.category.displayName)
                    }
                    DropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                        Category.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    categoryMenuExpanded = false
                                    amountText.toDoubleOrNull()?.let { amount ->
                                        onEdit(merchantText, cat, Math.round(amount * 100.0))
                                    }
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            Text(
                buildString {
                    append(candidate.transactionAt.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")))
                    candidate.bankSender?.let { append(" · $it") }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.padding(top = 12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = onApprove) {
                    Icon(Icons.Filled.Check, contentDescription = "Approve", tint = DeepGreen)
                }
                IconButton(onClick = onReject) {
                    Icon(Icons.Filled.Close, contentDescription = "Reject", tint = ExpenseRed)
                }
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: ConfidenceLevel) {
    val (label, color) = when (confidence) {
        ConfidenceLevel.HIGH -> "High" to DeepGreen
        ConfidenceLevel.MEDIUM -> "Medium" to MaterialTheme.colorScheme.secondary
        ConfidenceLevel.LOW -> "Low" to TextSecondary
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
