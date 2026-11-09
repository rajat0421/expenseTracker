package com.yourmoney.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourmoney.app.model.Category
import com.yourmoney.app.model.ConfidenceLevel
import com.yourmoney.app.model.ExpenseSource
import com.yourmoney.app.model.PendingExpense
import com.yourmoney.app.model.ReviewStatus
import java.time.LocalDateTime

/**
 * A candidate expense detected from SMS, awaiting review. [originalSmsBody] is the one
 * exception to "structured fields only": it is kept so the user can compare the detected
 * fields against the real message, but only while [status] is PENDING - it is nulled out
 * the moment the candidate is approved or rejected (see [PendingExpenseDao]).
 */
@Entity(tableName = "pending_expenses")
data class PendingExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchant: String,
    val category: Category,
    val amountPaise: Long,
    val transactionAt: LocalDateTime,
    val accountRef: String?,
    val transactionRef: String?,
    val bankSender: String?,
    val bankName: String? = null,
    val paymentMethod: String? = null,
    val confidence: ConfidenceLevel,
    val status: ReviewStatus,
    val fingerprint: String,
    val source: ExpenseSource,
    val detectedAtMillis: Long,
    val originalSmsBody: String? = null,
)

fun PendingExpenseEntity.toDomain() = PendingExpense(
    id = id,
    merchant = merchant,
    category = category,
    amountPaise = amountPaise,
    transactionAt = transactionAt,
    accountRef = accountRef,
    transactionRef = transactionRef,
    bankSender = bankSender,
    bankName = bankName,
    paymentMethod = paymentMethod,
    confidence = confidence,
    status = status,
    fingerprint = fingerprint,
    source = source,
    detectedAtMillis = detectedAtMillis,
    originalSmsBody = originalSmsBody,
)
