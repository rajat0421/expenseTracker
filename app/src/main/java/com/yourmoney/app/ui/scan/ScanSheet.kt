package com.yourmoney.app.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.yourmoney.app.ui.rememberFactoryViewModel
import com.yourmoney.app.ui.theme.DeepGreen
import com.yourmoney.app.ui.theme.ExpenseRed
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanSheet(onDismiss: () -> Unit) {
    val viewModel: ScanViewModel = rememberFactoryViewModel { app -> ScanViewModel(app.historicalScanner) }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    var range by remember { mutableStateOf(thisMonthRange()) }
    var showCustomRangePicker by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            permissionDenied = false
            viewModel.startScan(range.first, range.second)
        } else {
            permissionDenied = true
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Scan SMS for Expenses", style = MaterialTheme.typography.titleLarge)

            Text(
                "YourMoney can scan your SMS inbox to find bank debit messages and suggest them as " +
                    "expenses. This runs entirely on your device — nothing is uploaded anywhere — and " +
                    "every suggestion waits for your approval before it becomes an expense.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                "Range: ${range.first} to ${range.second}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { range = thisMonthRange() }) { Text("This month") }
                TextButton(onClick = { range = lastMonthRange() }) { Text("Last month") }
                TextButton(onClick = { showCustomRangePicker = true }) { Text("Custom range") }
            }

            when {
                uiState.isScanning -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        "Examined ${uiState.messagesExamined} messages · found ${uiState.candidatesFound} candidates",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                uiState.completed -> {
                    val errorSuffix = if (uiState.errors > 0) ", ${uiState.errors} errors" else ""
                    Text(
                        "Scan complete: examined ${uiState.messagesExamined} messages, found " +
                            "${uiState.candidatesFound} new candidate${if (uiState.candidatesFound == 1) "" else "s"}$errorSuffix.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (uiState.candidatesFound > 0) {
                        Text(
                            "Open the Review tab to approve or reject them.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeepGreen,
                        )
                    }
                }
                permissionDenied -> {
                    Text(
                        "SMS permission was denied, so scanning is unavailable. Manual expense entry still " +
                            "works normally. You can grant permission anytime from Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ExpenseRed,
                    )
                }
                uiState.errorMessage != null -> {
                    Text(uiState.errorMessage ?: "", style = MaterialTheme.typography.bodyMedium, color = ExpenseRed)
                }
            }

            Button(
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
                        PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        viewModel.startScan(range.first, range.second)
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_SMS)
                    }
                },
                enabled = !uiState.isScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
            ) {
                Text(if (uiState.isScanning) "Scanning…" else "Start Scan")
            }
        }
    }

    if (showCustomRangePicker) {
        CustomRangePickerDialog(
            onDismiss = { showCustomRangePicker = false },
            onRangeSelected = { start, end ->
                range = start to end
                showCustomRangePicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRangePickerDialog(onDismiss: () -> Unit, onRangeSelected: (LocalDate, LocalDate) -> Unit) {
    var pickingStart by remember { mutableStateOf(true) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = datePickerState.selectedDateMillis
                if (millis != null) {
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    if (pickingStart) {
                        startDate = date
                        pickingStart = false
                    } else {
                        val start = startDate ?: date
                        if (start.isAfter(date)) onRangeSelected(date, start) else onRangeSelected(start, date)
                    }
                }
            }) {
                Text(if (pickingStart) "Next" else "Done")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        Column {
            Text(
                if (pickingStart) "Pick start date" else "Pick end date",
                modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            DatePicker(state = datePickerState)
        }
    }
}

private fun thisMonthRange(): Pair<LocalDate, LocalDate> {
    val today = LocalDate.now()
    return today.withDayOfMonth(1) to today
}

private fun lastMonthRange(): Pair<LocalDate, LocalDate> {
    val firstOfThisMonth = LocalDate.now().withDayOfMonth(1)
    val lastMonthEnd = firstOfThisMonth.minusDays(1)
    return lastMonthEnd.withDayOfMonth(1) to lastMonthEnd
}
