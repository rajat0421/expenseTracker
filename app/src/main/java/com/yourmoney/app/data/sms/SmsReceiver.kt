package com.yourmoney.app.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.yourmoney.app.YourMoneyApplication
import com.yourmoney.app.model.ExpenseSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Live SMS detection entry point. Runs the exact same [TransactionParser] +
 * [CandidateProcessor] used by [HistoricalScanner], so historical and live detection
 * never disagree about what counts as an expense.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (parts.isNullOrEmpty()) return

        // Multi-part messages share one sender/timestamp; join bodies into one logical message.
        val sender = parts.first().originatingAddress ?: return
        val body = parts.joinToString(separator = "") { it.messageBody ?: "" }
        val timestamp = parts.first().timestampMillis
        val message = SmsMessage(sender = sender, body = body, timestampMillis = timestamp)

        val app = context.applicationContext as YourMoneyApplication
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val outcome = app.transactionParser.parse(message)
                val created = app.candidateProcessor.process(outcome, ExpenseSource.SMS)
                if (created && outcome is ParseOutcome.Candidate) {
                    PendingExpenseNotifier.notifyNewCandidate(
                        context.applicationContext,
                        outcome.parsed.merchant,
                        outcome.parsed.amountPaise,
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
