package com.yourmoney.app.data.sms

import android.content.Context
import android.provider.Telephony
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reads SMS inbox messages within a time range via ContentResolver. Requires READ_SMS. */
class SmsMessageReader(private val context: Context) {

    suspend fun readInbox(startMillis: Long, endMillis: Long): List<SmsMessage> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<SmsMessage>()
        val projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE)
        val selection = "${Telephony.Sms.DATE} >= ? AND ${Telephony.Sms.DATE} <= ?"
        val selectionArgs = arrayOf(startMillis.toString(), endMillis.toString())

        try {
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${Telephony.Sms.DATE} ASC",
            )?.use { cursor ->
                val addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE)
                if (addressIndex < 0 || bodyIndex < 0 || dateIndex < 0) return@use

                while (cursor.moveToNext()) {
                    val sender = cursor.getString(addressIndex) ?: continue
                    val body = cursor.getString(bodyIndex) ?: continue
                    val date = cursor.getLong(dateIndex)
                    messages.add(SmsMessage(sender = sender, body = body, timestampMillis = date))
                }
            }
        } catch (_: SecurityException) {
            // Permission revoked between the caller's check and this read; surface
            // whatever was already read rather than crashing the scan.
        }

        messages
    }
}
