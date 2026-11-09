package com.yourmoney.app.model

import java.time.LocalDateTime

/**
 * A candidate expense detected from SMS, awaiting user review. Never shown
 * in expense history/dashboard totals until approved into [Expense].
 */
data class PendingExpense(
    val id: Long = 0,
    val merchant: String,
    val categoryId: Long,
    val amountPaise: Long,
    val transactionAt: LocalDateTime,
    val accountRef: String? = null,
    val transactionRef: String? = null,
    val bankSender: String? = null,
    val bankName: String? = null,
    val paymentMethod: String? = null,
    val confidence: ConfidenceLevel,
    val status: ReviewStatus = ReviewStatus.PENDING,
    val fingerprint: String,
    val source: ExpenseSource,
    val detectedAtMillis: Long = System.currentTimeMillis(),
    /** Full original SMS body, kept only while [status] is PENDING; cleared on approve/reject. */
    val originalSmsBody: String? = null,
)
