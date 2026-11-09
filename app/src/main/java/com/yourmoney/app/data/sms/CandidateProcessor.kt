package com.yourmoney.app.data.sms

import com.yourmoney.app.data.Categorizer
import com.yourmoney.app.data.CategoryRepository
import com.yourmoney.app.data.PendingExpenseRepository
import com.yourmoney.app.model.ExpenseSource

/**
 * The single place that turns a [ParseOutcome] into a pending-review row (or decides to
 * ignore it). Shared by historical scanning and live SMS detection so both paths behave
 * identically for categorization and deduplication.
 */
class CandidateProcessor(
    private val pendingExpenseRepository: PendingExpenseRepository,
    private val categoryRepository: CategoryRepository,
) {

    /**
     * Returns true if a new pending candidate was created (false for ignored/duplicate).
     * [originalSmsBody] is kept only in the pending row, only until the user approves or
     * rejects the candidate - never logged, never sent anywhere.
     */
    suspend fun process(outcome: ParseOutcome, source: ExpenseSource, originalSmsBody: String): Boolean {
        val parsed = when (outcome) {
            is ParseOutcome.Candidate -> outcome.parsed
            is ParseOutcome.Transfer, ParseOutcome.NotTransaction -> return false
        }

        val fingerprint = FingerprintCalculator.compute(parsed)
        if (pendingExpenseRepository.existsFingerprint(fingerprint)) return false

        val key = Categorizer.suggestCategoryKey(parsed.merchant)
        val categoryId = key?.let { categoryRepository.findByKey(it)?.id } ?: categoryRepository.uncategorizedId()

        pendingExpenseRepository.addCandidate(parsed, categoryId, fingerprint, source, originalSmsBody)
        return true
    }
}
