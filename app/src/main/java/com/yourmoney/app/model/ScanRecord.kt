package com.yourmoney.app.model

/** Summary of one historical SMS scan run, kept for display/debugging only. */
data class ScanRecord(
    val id: Long = 0,
    val rangeStartEpochDay: Long,
    val rangeEndEpochDay: Long,
    val scannedAtMillis: Long,
    val messagesExamined: Int,
    val candidatesFound: Int,
    val errorCount: Int,
)
