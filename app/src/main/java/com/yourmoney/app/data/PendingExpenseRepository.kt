package com.yourmoney.app.data

import com.yourmoney.app.data.local.ExpenseDao
import com.yourmoney.app.data.local.PendingExpenseDao
import com.yourmoney.app.data.local.PendingExpenseEntity
import com.yourmoney.app.data.local.toDomain
import com.yourmoney.app.data.local.toEntity
import com.yourmoney.app.data.sms.ParsedTransaction
import com.yourmoney.app.model.Category
import com.yourmoney.app.model.Expense
import com.yourmoney.app.model.ExpenseSource
import com.yourmoney.app.model.PendingExpense
import com.yourmoney.app.model.ReviewStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Owns the pending-review queue. Also owns the dedup check and the approve step (which
 * writes the final [Expense]), since both need to see pending AND approved transactions
 * to decide whether a detected SMS transaction is new.
 */
class PendingExpenseRepository(
    private val pendingDao: PendingExpenseDao,
    private val expenseDao: ExpenseDao,
) {
    fun observePending(): Flow<List<PendingExpense>> =
        pendingDao.observeByStatus(ReviewStatus.PENDING).map { list -> list.map { it.toDomain() } }

    fun observePendingCount(): Flow<Int> = pendingDao.observePendingCount()

    suspend fun existsFingerprint(fingerprint: String): Boolean =
        pendingDao.findByFingerprint(fingerprint) != null || expenseDao.findByFingerprint(fingerprint) != null

    suspend fun addCandidate(parsed: ParsedTransaction, category: Category, fingerprint: String, source: ExpenseSource) {
        pendingDao.insert(
            PendingExpenseEntity(
                merchant = parsed.merchant,
                category = category,
                amountPaise = parsed.amountPaise,
                transactionAt = parsed.transactionAt,
                accountRef = parsed.accountRef,
                transactionRef = parsed.transactionRef,
                bankSender = parsed.bankSender,
                confidence = parsed.confidence,
                status = ReviewStatus.PENDING,
                fingerprint = fingerprint,
                source = source,
                detectedAtMillis = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun updateDraft(id: Long, merchant: String, category: Category, amountPaise: Long) {
        val existing = pendingDao.getById(id) ?: return
        pendingDao.update(existing.copy(merchant = merchant, category = category, amountPaise = amountPaise))
    }

    suspend fun reject(id: Long) = pendingDao.updateStatus(id, ReviewStatus.REJECTED)

    suspend fun rejectAll(ids: List<Long>) {
        if (ids.isEmpty()) return
        pendingDao.updateStatusForIds(ids, ReviewStatus.REJECTED)
    }

    suspend fun approve(id: Long) {
        val existing = pendingDao.getById(id) ?: return
        if (existing.status != ReviewStatus.PENDING) return
        expenseDao.insert(
            Expense(
                merchant = existing.merchant,
                category = existing.category,
                amountPaise = existing.amountPaise,
                date = existing.transactionAt.toLocalDate(),
                source = existing.source,
                accountRef = existing.accountRef,
                transactionRef = existing.transactionRef,
                bankSender = existing.bankSender,
                fingerprint = existing.fingerprint,
            ).toEntity(),
        )
        pendingDao.updateStatus(id, ReviewStatus.APPROVED)
    }

    suspend fun approveAll(ids: List<Long>) {
        ids.forEach { approve(it) }
    }
}
