package com.yourmoney.app.data.sms.rules

import com.yourmoney.app.data.sms.BankSenderRegistry
import com.yourmoney.app.data.sms.ParseOutcome
import com.yourmoney.app.data.sms.ParsedTransaction
import com.yourmoney.app.data.sms.ParserRule
import com.yourmoney.app.data.sms.SmsMessage
import com.yourmoney.app.data.sms.TransactionDirection
import com.yourmoney.app.model.ConfidenceLevel
import java.time.Instant
import java.time.ZoneId

/**
 * Covers the common shape of Indian bank/UPI debit-alert SMS using sender identity,
 * debit/credit keywords, amount + reference patterns and contextual guards (OTP,
 * promotions, balance alerts, reversals, own-account transfers) rather than a single
 * universal regex. This is the first/default rule in the registry; additional
 * bank-specific rules can be added to [com.yourmoney.app.data.sms.TransactionParser]
 * without changing this one.
 */
object GenericBankSmsRule : ParserRule {

    override val id: String = "generic-bank-sms-v1"

    private val otpPattern = Regex("""\botp\b""", RegexOption.IGNORE_CASE)
    private val promoPattern = Regex(
        """\b(offer|cashback upto|win|congratulations|discount|sale|coupon|lucky draw)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val balanceOnlyPattern = Regex(
        """\b(available balance|avl bal|a/?c bal(ance)?)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val creditPattern = Regex(
        """\b(credited|credit of|deposited|received|cr\.)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val debitPattern = Regex(
        """\b(debited|debit of|spent|paid|payment of|purchase of|withdrawn|dr\.)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val transferPattern = Regex(
        """\b(transferred to your (own |linked )?account|self\s*transfer|own account|to your (other )?a/?c)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val reversalPattern = Regex(
        """\b(reversed|refund(ed)?|reversal)\b""",
        RegexOption.IGNORE_CASE,
    )

    private val amountPattern = Regex(
        """(?:rs\.?|inr)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)""",
        RegexOption.IGNORE_CASE,
    )
    private val accountRefPattern = Regex(
        """a/?c\s*(?:no\.?|number)?\s*[:\-]?\s*[xX*]*([0-9]{2,6})""",
        RegexOption.IGNORE_CASE,
    )
    private val txnRefPattern = Regex(
        """(?:ref(?:erence)?\s*(?:no\.?|number)?|rrn|txn\s*id|utr)\s*[:\-]?\s*([a-zA-Z0-9]{4,})""",
        RegexOption.IGNORE_CASE,
    )
    private val merchantPattern = Regex(
        """(?:to|towards|at)\s+([A-Za-z0-9@._&'\-\s]{2,40}?)(?=\s+on\b|\s+dated\b|\.|\s+ref\b|\s+txn\b|\s+a/?c\b|$)""",
        RegexOption.IGNORE_CASE,
    )

    override fun parse(message: SmsMessage): ParseOutcome {
        val body = message.body

        if (otpPattern.containsMatchIn(body)) return ParseOutcome.NotTransaction
        if (reversalPattern.containsMatchIn(body)) return ParseOutcome.NotTransaction

        val isDebit = debitPattern.containsMatchIn(body)
        val isCredit = creditPattern.containsMatchIn(body)

        if (promoPattern.containsMatchIn(body) && !isDebit) return ParseOutcome.NotTransaction
        if (balanceOnlyPattern.containsMatchIn(body) && !isDebit) return ParseOutcome.NotTransaction
        if (isCredit && !isDebit) return ParseOutcome.NotTransaction
        if (!isDebit) return ParseOutcome.NotTransaction

        val amountMatch = amountPattern.find(body) ?: return ParseOutcome.NotTransaction
        val amountPaise = parseAmountToPaise(amountMatch.groupValues[1]) ?: return ParseOutcome.NotTransaction

        val accountRef = accountRefPattern.find(body)?.groupValues?.get(1)
        val transactionRef = txnRefPattern.find(body)?.groupValues?.get(1)
        val merchantRaw = merchantPattern.find(body)?.groupValues?.get(1)?.trim()
        val merchant = merchantRaw?.takeIf { it.isNotBlank() } ?: "Unknown Merchant"

        val isTransfer = transferPattern.containsMatchIn(body)
        val looksLikeBank = BankSenderRegistry.isLikelyBankSender(message.sender)

        val confidence = when {
            looksLikeBank && transactionRef != null && merchantRaw != null -> ConfidenceLevel.HIGH
            looksLikeBank && (transactionRef != null || merchantRaw != null) -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }

        val parsed = ParsedTransaction(
            amountPaise = amountPaise,
            merchant = merchant,
            transactionAt = Instant.ofEpochMilli(message.timestampMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime(),
            accountRef = accountRef,
            transactionRef = transactionRef,
            bankSender = message.sender,
            direction = TransactionDirection.DEBIT,
            isLikelyTransfer = isTransfer,
            confidence = confidence,
        )

        return if (isTransfer) ParseOutcome.Transfer(parsed) else ParseOutcome.Candidate(parsed)
    }

    private fun parseAmountToPaise(raw: String): Long? {
        val value = raw.replace(",", "").toDoubleOrNull() ?: return null
        return Math.round(value * 100.0)
    }
}
