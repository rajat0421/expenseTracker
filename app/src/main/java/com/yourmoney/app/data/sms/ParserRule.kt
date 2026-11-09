package com.yourmoney.app.data.sms

/**
 * One parsing strategy for a family of bank/payment SMS formats. The [TransactionParser]
 * holds an ordered list of these; add a new bank-specific rule here without touching the
 * engine or any other rule.
 */
interface ParserRule {
    val id: String
    fun parse(message: SmsMessage): ParseOutcome
}
