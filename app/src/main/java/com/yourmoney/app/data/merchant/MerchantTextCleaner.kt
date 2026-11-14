package com.yourmoney.app.data.merchant

/**
 * Normalizes a raw merchant narration (e.g. "Swiggy Ltd", "UPI/P2M/.../RELIANCE JIO INFOCO")
 * into a clean, uppercased, whitespace-collapsed form, keeping only letters, digits, `&` and
 * `'` (so "H&M" and "Spencer's" survive intact). Aliases are stored pre-cleaned in the same
 * form, so matching is a plain string comparison - no regex at lookup time.
 */
object MerchantTextCleaner {

    private val keepCharsPattern = Regex("[^A-Z0-9&']+")

    fun clean(raw: String): String {
        val upper = raw.uppercase()
        val collapsed = keepCharsPattern.replace(upper, " ")
        return collapsed.trim().replace(Regex("\\s+"), " ")
    }
}
