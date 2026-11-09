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
 * A candidate expense detected from SMS, awaiting review. Deliberately does not store
 * the raw SMS body — only the structured fields needed for review and deduplication.
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
    val confidence: ConfidenceLevel,
    val status: ReviewStatus,
    val fingerprint: String,
    val source: ExpenseSource,
    val detectedAtMillis: Long,
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
    confidence = confidence,
    status = status,
    fingerprint = fingerprint,
    source = source,
    detectedAtMillis = detectedAtMillis,
)
