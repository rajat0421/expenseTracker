package com.yourmoney.app.data.sms.rules

import com.yourmoney.app.data.sms.ParseOutcome
import com.yourmoney.app.data.sms.ParsedTransaction
import com.yourmoney.app.data.sms.ParserRule
import com.yourmoney.app.data.sms.SmsMessage
import com.yourmoney.app.data.sms.TransactionDirection
import com.yourmoney.app.model.ConfidenceLevel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Axis Bank's UPI debit/credit alert format is multi-line and fixed-shape, e.g.:
 *
 * ```
 * INR 330.00 debited
 * A/c no. XX6823
 * 08-11-26, 15:32:53
 * UPI/P2M/667808817430/Swiggy Ltd
 * Not you? SMS BLOCKUPI Cust ID to 1234567890
 * Axis Bank
 * ```
 *
 * Rather than one global regex over the whole body (which is how a phone/customer ID or
 * account number previously got mistaken for the amount or merchant), this rule parses
 * line by line: each field is read only from the one line shaped like that field, so a
 * number on an unrelated line (customer ID, account number, UPI reference) can never be
 * picked up as the amount, and text after the UPI line ("Not you?", "Cust ID", the bank
 * sign-off) can never be picked up as part of the merchant.
 */
object AxisBankSmsRule : ParserRule {

    override val id: String = "axis-bank-sms-v1"

    private val reversalPattern = Regex("""\b(reversed|refund(ed)?|reversal)\b""", RegexOption.IGNORE_CASE)
    private val otpPattern = Regex("""\botp\b""", RegexOption.IGNORE_CASE)

    private val amountLinePattern = Regex(
        """^(?:inr|rs\.?|₹)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)\s+(debited|credited)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val accountLinePattern = Regex(
        """^a/?c\s*no\.?\s*[:.]?\s*([a-zA-Z]{0,2}\d{3,8})$""",
        RegexOption.IGNORE_CASE,
    )
    private val dateTimeLinePattern = Regex(
        """^(\d{2})-(\d{2})-(\d{2}),\s*(\d{2}):(\d{2}):(\d{2})$""",
    )
    private val upiLinePattern = Regex(
        """^UPI/(P2M|P2A)/(\d+)/(.+)$""",
        RegexOption.IGNORE_CASE,
    )
    private val signOffPattern = Regex("""^axis\s*bank\.?$""", RegexOption.IGNORE_CASE)

    override fun parse(message: SmsMessage): ParseOutcome {
        val body = message.body
        if (otpPattern.containsMatchIn(body)) return ParseOutcome.NotTransaction
        if (reversalPattern.containsMatchIn(body)) return ParseOutcome.NotTransaction

        val lines = body.lines().map { it.trim() }.filter { it.isNotBlank() }

        val isAxisMessage = message.sender.uppercase().contains("AXIS") ||
            lines.any { signOffPattern.matches(it) }
        if (!isAxisMessage) return ParseOutcome.NotTransaction

        val amountLine = lines.firstNotNullOfOrNull { amountLinePattern.find(it) } ?: return ParseOutcome.NotTransaction
        val direction = if (amountLine.groupValues[2].equals("credited", ignoreCase = true)) {
            TransactionDirection.CREDIT
        } else {
            TransactionDirection.DEBIT
        }
        if (direction == TransactionDirection.CREDIT) return ParseOutcome.NotTransaction

        val amountPaise = parseAmountToPaise(amountLine.groupValues[1]) ?: return ParseOutcome.NotTransaction

        val accountRef = lines.firstNotNullOfOrNull { accountLinePattern.find(it)?.groupValues?.get(1) }

        val transactionAt = lines.firstNotNullOfOrNull { line -> dateTimeLinePattern.find(line) }
            ?.let { match ->
                val (dd, mm, yy, hh, mi, ss) = match.destructured
                runCatching {
                    LocalDateTime.of(2000 + yy.toInt(), mm.toInt(), dd.toInt(), hh.toInt(), mi.toInt(), ss.toInt())
                }.getOrNull()
            }
            ?: Instant.ofEpochMilli(message.timestampMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()

        val upiMatch = lines.firstNotNullOfOrNull { upiLinePattern.find(it) }
        val upiType = upiMatch?.groupValues?.get(1)?.uppercase()
        val transactionRef = upiMatch?.groupValues?.get(2)
        val merchantRaw = upiMatch?.groupValues?.get(3)?.let(::normalizeMerchantName)

        val isPersonToPerson = upiType == "P2A"
        val merchant = merchantRaw?.takeIf { it.isNotBlank() } ?: "Unknown Merchant"

        val confidence = when {
            merchantRaw == null -> ConfidenceLevel.LOW
            isPersonToPerson -> ConfidenceLevel.MEDIUM
            transactionRef != null -> ConfidenceLevel.HIGH
            else -> ConfidenceLevel.MEDIUM
        }

        val parsed = ParsedTransaction(
            amountPaise = amountPaise,
            merchant = merchant,
            transactionAt = transactionAt,
            accountRef = accountRef,
            transactionRef = transactionRef,
            bankSender = message.sender,
            direction = TransactionDirection.DEBIT,
            isLikelyTransfer = false,
            confidence = confidence,
            bankName = "Axis Bank",
            paymentMethod = if (upiMatch != null) "UPI" else null,
        )

        // P2A (person-to-person) is not silently discarded or blindly auto-approved here -
        // it still becomes a review candidate, just at lower confidence than a merchant (P2M) payment.
        return ParseOutcome.Candidate(parsed)
    }

    private fun parseAmountToPaise(raw: String): Long? {
        val value = raw.replace(",", "").toDoubleOrNull() ?: return null
        return Math.round(value * 100.0)
    }

    private fun normalizeMerchantName(raw: String): String {
        val cleaned = raw.trim().trimEnd('.', ',').replace(Regex("\\s+"), " ")
        return cleaned.split(" ").joinToString(" ") { word ->
            if (word.isEmpty()) word else word.lowercase().replaceFirstChar { it.titlecase() }
        }
    }
}
