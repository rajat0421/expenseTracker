package com.yourmoney.app.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.yourmoney.app.ui.theme.BackgroundLight
import com.yourmoney.app.ui.theme.DeepGreen
import com.yourmoney.app.ui.theme.ExpenseRed
import com.yourmoney.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var readSmsGranted by remember { mutableStateOf(hasPermission(context, Manifest.permission.READ_SMS)) }
    var receiveSmsGranted by remember { mutableStateOf(hasPermission(context, Manifest.permission.RECEIVE_SMS)) }
    var notificationsGranted by remember { mutableStateOf(notificationsAllowed(context)) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        readSmsGranted = results[Manifest.permission.READ_SMS] ?: readSmsGranted
        receiveSmsGranted = results[Manifest.permission.RECEIVE_SMS] ?: receiveSmsGranted
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsGranted = granted
    }

    Scaffold(containerColor = BackgroundLight) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)

            PermissionStatusCard(
                title = "SMS Access",
                granted = readSmsGranted && receiveSmsGranted,
                explanation = "Required to detect expenses automatically from bank SMS — both for " +
                    "scanning past messages and for new messages as they arrive. Messages are read " +
                    "on-device only; nothing is uploaded or shared with any server.",
                onRequest = {
                    smsPermissionLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS))
                },
            )

            PermissionStatusCard(
                title = "Notifications",
                granted = notificationsGranted,
                explanation = "Lets YourMoney notify you when a new expense is detected from an incoming " +
                    "SMS, so you remember to review it.",
                onRequest = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("About SMS permissions", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.padding(top = 8.dp))
                    Text(
                        "READ_SMS and RECEIVE_SMS are restricted permissions on the Google Play Store, " +
                            "normally available only to apps approved as a default SMS/dialer handler or " +
                            "an equivalent core use case. YourMoney is built for personal, sideloaded " +
                            "installation, where you can grant these permissions directly. If this app is " +
                            "ever distributed through the Play Store, SMS scanning will not be available " +
                            "unless that policy requirement is separately satisfied.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }

            if (!readSmsGranted || !receiveSmsGranted) {
                TextButton(onClick = { openAppSettings(context) }) { Text("Open App Settings") }
            }

            Spacer(modifier = Modifier.padding(bottom = 40.dp))
        }
    }
}

@Composable
private fun PermissionStatusCard(title: String, granted: Boolean, explanation: String, onRequest: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (granted) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = null,
                    tint = if (granted) DeepGreen else ExpenseRed,
                    modifier = Modifier.width(20.dp),
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.padding(top = 8.dp))
            Text(explanation, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            if (!granted) {
                Spacer(modifier = Modifier.padding(top = 12.dp))
                Button(onClick = onRequest, colors = ButtonDefaults.buttonColors(containerColor = DeepGreen)) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

private fun hasPermission(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

private fun notificationsAllowed(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    } else {
        true
    }

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
    context.startActivity(intent)
}
