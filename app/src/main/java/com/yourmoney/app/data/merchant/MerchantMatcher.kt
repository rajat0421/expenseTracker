package com.yourmoney.app.data.merchant

import com.yourmoney.app.data.local.MerchantAliasLookup

data class MerchantMatch(val merchantId: Long, val displayName: String, val categoryId: Long)

/**
 * Pure matching logic, separated from any database access so it can run synchronously
 * (e.g. on every keystroke while typing a merchant name) against an already-loaded alias
 * list, as well as from the suspend SMS-detection path.
 *
 * Matching is whole-word/whole-phrase (never raw substring), so a short alias like "VI"
 * can't accidentally match inside an unrelated word. When several aliases match, the
 * longest one wins - e.g. "SWIGGY INSTAMART" (Groceries) is preferred over the shorter
 * "SWIGGY" (Food & Dining) for a narration that contains both words.
 */
object MerchantMatcher {

    // Narrations that only ever identify a payment app/gateway, never the real merchant -
    // if the *entire* cleaned narration reduces to one of these, we deliberately return no
    // match rather than mis-categorize by the wallet/gateway name.
    private val paymentAppOnlyNames = setOf(
        "PHONEPE", "GOOGLEPAY", "GPAY", "PAYTM", "CRED", "RAZORPAY", "BHIM", "AMAZONPAY", "PAYU", "CCAVENUE",
    )

    fun match(rawMerchantText: String, aliases: List<MerchantAliasLookup>): MerchantMatch? {
        val cleaned = MerchantTextCleaner.clean(rawMerchantText)
        if (cleaned.isBlank()) return null

        val compact = cleaned.replace(" ", "")
        if (compact in paymentAppOnlyNames) return null

        val padded = " $cleaned "
        val best = aliases
            .filter { padded.contains(" ${it.alias} ") }
            .maxByOrNull { it.alias.length }
            ?: return null

        return MerchantMatch(best.merchantId, best.displayName, best.categoryId)
    }
}
