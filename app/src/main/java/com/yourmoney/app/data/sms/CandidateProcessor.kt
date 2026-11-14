package com.yourmoney.app.data.sms

import com.yourmoney.app.data.CategoryRepository
import com.yourmoney.app.data.PendingExpenseRepository
import com.yourmoney.app.data.merchant.MerchantRecognitionEngine
import com.yourmoney.app.model.ExpenseSource

/**
 * The single place that turns a [ParseOutcome] into a pending-review row (or decides to
 * ignore it). Shared by historical scanning and live SMS detection so both paths behave
 * identically for categorization and deduplication.
 */
class CandidateProcessor(
    private val pendingExpenseRepository: PendingExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val merchantRecognitionEngine: MerchantRecognitionEngine,
) {

    /**
     * Returns the new pending candidate's id, or null if it was ignored/a duplicate.
     * [originalSmsBody] is kept only in the pending row, only until the user approves or
     * rejects the candidate - never logged, never sent anywhere.
     */
    suspend fun process(outcome: ParseOutcome, source: ExpenseSource, originalSmsBody: String): Long? {
        val parsed = when (outcome) {
            is ParseOutcome.Candidate -> outcome.parsed
            is ParseOutcome.Transfer, ParseOutcome.NotTransaction -> return null
        }

        val fingerprint = FingerprintCalculator.compute(parsed)
        if (pendingExpenseRepository.existsFingerprint(fingerprint)) return null

        val match = merchantRecognitionEngine.recognize(parsed.merchant)
        val categoryId = match?.categoryId ?: categoryRepository.uncategorizedId()
        // Canonicalize to the recognized merchant's display name (e.g. "SWIGGY LTD" -> "Swiggy").
        val finalParsed = if (match != null) parsed.copy(merchant = match.displayName) else parsed

        return pendingExpenseRepository.addCandidate(finalParsed, categoryId, fingerprint, source, originalSmsBody)
    }
}
