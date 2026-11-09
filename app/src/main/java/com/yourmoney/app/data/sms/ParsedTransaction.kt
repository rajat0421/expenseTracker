package com.yourmoney.app.data.sms

import com.yourmoney.app.model.ConfidenceLevel
import java.time.LocalDateTime

/** Structured data extracted from one SMS message by a [ParserRule]. */
data class ParsedTransaction(
    val amountPaise: Long,
    val merchant: String,
    val transactionAt: LocalDateTime,
    val accountRef: String?,
    val transactionRef: String?,
    val bankSender: String,
    val direction: TransactionDirection,
    val isLikelyTransfer: Boolean,
    val confidence: ConfidenceLevel,
)

/** Outcome of running the parsing engine over a single SMS message. */
sealed class ParseOutcome {
    /** Not a transaction message at all (OTP, promo, balance alert, credit, unrelated). */
    data object NotTransaction : ParseOutcome()

    /** A debit that looks like an identifiable own-account transfer, not a purchase. */
    data class Transfer(val parsed: ParsedTransaction) : ParseOutcome()

    /** A debit that looks like a real expense and should go to the pending review queue. */
    data class Candidate(val parsed: ParsedTransaction) : ParseOutcome()
}
