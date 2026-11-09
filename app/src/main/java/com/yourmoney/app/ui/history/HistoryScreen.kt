package com.yourmoney.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourmoney.app.ui.dashboard.ExpenseRow
import com.yourmoney.app.ui.rememberFactoryViewModel
import com.yourmoney.app.ui.theme.BackgroundLight
import com.yourmoney.app.ui.theme.DeepGreen
import com.yourmoney.app.ui.theme.TextSecondary

@Composable
fun HistoryScreen() {
    val viewModel: HistoryViewModel = rememberFactoryViewModel { app -> HistoryViewModel(app.repository) }
    val expenses by viewModel.expenses.collectAsState()

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
                    Text(
                        "Expense History",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 12.dp))
                    OutlinedTextField(
                        value = viewModel.searchText,
                        onValueChange = viewModel::onSearchTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search merchant or category") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepGreen),
                    )
                }
            }

            if (expenses.isEmpty()) {
                item {
                    Text(
                        "No expenses found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            } else {
                items(expenses) { expense -> ExpenseRow(expense) }
            }

            item { androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(bottom = 80.dp)) }
        }
    }
}
