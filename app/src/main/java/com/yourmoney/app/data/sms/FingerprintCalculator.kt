package com.yourmoney.app.data.sms

/**
 * Computes a stable identity string for a parsed transaction so the same real-world
 * transaction reported twice (e.g. re-scanned, or seen via both historical scan and
 * live receiver) resolves to the same fingerprint instead of creating a duplicate.
 *
 * Priority, per the dedup requirement:
 *  1. transaction reference id (+ account, if present)
 *  2. transaction reference id alone
 *  3. account + amount + merchant + date/time (minute precision)
 *  4. conservative fallback: amount + merchant + bank sender + date/time (minute precision)
 */
object FingerprintCalculator {

    fun compute(parsed: ParsedTransaction): String {
        val ref = parsed.transactionRef?.trim()?.takeIf { it.isNotBlank() }
        val account = parsed.accountRef?.trim()?.takeIf { it.isNotBlank() }
        val minutePrecise = "${parsed.transactionAt.toLocalDate()}T${parsed.transactionAt.hour}:${parsed.transactionAt.minute}"
        val merchantKey = parsed.merchant.lowercase().trim()

        return when {
            ref != null && account != null -> "ref:$account:$ref"
            ref != null -> "ref:$ref"
            account != null -> "acct:$account:${parsed.amountPaise}:$merchantKey:$minutePrecise"
            else -> "fallback:${parsed.amountPaise}:$merchantKey:${parsed.bankSender}:$minutePrecise"
        }
    }
}
