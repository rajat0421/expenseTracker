package com.yourmoney.app.data.sms

/**
 * Loose matcher for known Indian bank / payment-app SMS sender IDs (e.g. "VM-HDFCBK",
 * "AD-SBIINB", "JD-ICICIB", "AX-PAYTM"). Used only as a confidence signal, never as a
 * hard requirement, since sender ID formats vary by telecom operator and change over time.
 */
object BankSenderRegistry {

    private val knownSenderKeywords = listOf(
        "HDFC", "ICICI", "SBI", "AXIS", "KOTAK", "YESBANK", "PNB", "BOB", "CANBK", "UNION",
        "IDFC", "INDUS", "RBL", "FEDERAL", "PAYTM", "PHONEPE", "GPAY", "UPI", "SBIUPI",
        "CENTBK", "MAHABANK", "IOB", "UCOBK", "INDBNK", "SCBANK", "HSBC", "CITI", "AMEX",
        "SLICE", "JUPITER", "CRED", "BOI", "DEUTSCHE", "DBS", "AUBANK", "IDBI",
    )

    fun isLikelyBankSender(sender: String): Boolean {
        val normalized = sender.uppercase()
        return knownSenderKeywords.any { normalized.contains(it) }
    }
}
