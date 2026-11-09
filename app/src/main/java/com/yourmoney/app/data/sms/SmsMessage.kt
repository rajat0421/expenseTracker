package com.yourmoney.app.data.sms

/**
 * Plain, Android-agnostic representation of one SMS message. Kept separate from
 * android.telephony.SmsMessage so the parsing engine below can be unit tested and
 * reasoned about without any Android framework dependency.
 */
data class SmsMessage(
    val sender: String,
    val body: String,
    val timestampMillis: Long,
)
