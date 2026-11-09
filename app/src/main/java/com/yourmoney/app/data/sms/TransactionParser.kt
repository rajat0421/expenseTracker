package com.yourmoney.app.data.sms

import com.yourmoney.app.data.sms.rules.GenericBankSmsRule

/**
 * Engine that runs an ordered registry of [ParserRule]s over a message and returns the
 * first non-[ParseOutcome.NotTransaction] result. Independent of Android/UI/SMS-receiver
 * classes so it can be reused identically for historical scanning and live detection,
 * and extended with bank-specific rules without rewriting it.
 */
class TransactionParser(
    private val rules: List<ParserRule> = listOf(GenericBankSmsRule),
) {
    fun parse(message: SmsMessage): ParseOutcome {
        for (rule in rules) {
            val outcome = rule.parse(message)
            if (outcome !is ParseOutcome.NotTransaction) return outcome
        }
        return ParseOutcome.NotTransaction
    }
}
