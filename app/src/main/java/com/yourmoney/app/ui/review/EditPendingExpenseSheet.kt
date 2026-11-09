package com.yourmoney.app.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourmoney.app.model.Category
import com.yourmoney.app.model.PendingExpense
import com.yourmoney.app.model.paiseToRupees
import com.yourmoney.app.ui.theme.DeepGreen
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditPendingExpenseSheet(
    candidate: PendingExpense,
    onDismiss: () -> Unit,
    onSave: (merchant: String, category: Category, amountPaise: Long, date: LocalDate?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    var merchant by remember(candidate.id) { mutableStateOf(candidate.merchant) }
    var amountText by remember(candidate.id) { mutableStateOf("%.2f".format(candidate.amountPaise.paiseToRupees())) }
    var category by remember(candidate.id) { mutableStateOf(candidate.category) }
    var date by remember(candidate.id) { mutableStateOf(candidate.transactionAt.toLocalDate()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val canSave = merchant.isNotBlank() && amountText.toDoubleOrNull()?.let { it > 0 } == true

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Edit Expense", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepGreen),
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { value ->
                    if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amountText = value
                },
                label = { Text("Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepGreen),
            )

            Column {
                Text("Transaction date", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { showDatePicker = true }) {
                    Text(date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                }
            }

            Column {
                Text("Category", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.padding(top = 4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Category.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.displayName) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DeepGreen.copy(alpha = 0.15f)),
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: return@Button
                    onSave(merchant.trim(), category, Math.round(amount * 100.0), date)
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
            ) {
                Text("Save Changes")
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Done") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
