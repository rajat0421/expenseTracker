package com.yourmoney.app.data.sms

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/** Notifies the user a new expense candidate is ready for review, if permitted. */
object PendingExpenseNotifier {

    private const val CHANNEL_ID = "pending_expenses"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Pending Expenses",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "New expenses detected from SMS, awaiting your review"
        }
        manager.createNotificationChannel(channel)
    }

    fun notifyNewCandidate(context: Context, merchant: String, amountPaise: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        ensureChannel(context)
        val amountRupees = amountPaise / 100.0
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("New expense detected")
            .setContentText("₹%,.2f at %s — tap YourMoney to review".format(amountRupees, merchant))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    }
}
