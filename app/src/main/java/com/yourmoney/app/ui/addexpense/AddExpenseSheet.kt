package com.yourmoney.app.ui.addexpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourmoney.app.model.Category
import com.yourmoney.app.ui.rememberViewModel
import com.yourmoney.app.ui.theme.DeepGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseSheet(onDismiss: () -> Unit) {
    val viewModel: AddExpenseViewModel = rememberViewModel()
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Add Expense", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = viewModel.merchant,
                onValueChange = viewModel::onMerchantChange,
                label = { Text("Merchant") },
                placeholder = { Text("e.g. Swiggy, Ola, Amazon") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepGreen),
            )

            OutlinedTextField(
                value = viewModel.amountText,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount (₹)") },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepGreen),
            )

            Column {
                Text("Category", style = MaterialTheme.typography.bodyMedium)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Category.entries.forEach { cat ->
                        FilterChip(
                            selected = viewModel.category == cat,
                            onClick = { viewModel.onCategoryChange(cat) },
                            label = { Text(cat.displayName) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = DeepGreen.copy(alpha = 0.15f)),
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.save(onSaved = onDismiss) },
                enabled = viewModel.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
            ) {
                Text("Save Expense")
            }
        }
    }
}
