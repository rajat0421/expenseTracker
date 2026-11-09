package com.yourmoney.app.data

/**
 * Keyword-based merchant -> category guesser. Returns a stable [com.yourmoney.app.data.local.CategoryEntity.key]
 * (e.g. "FOOD"), not a display name or id, since the user can rename or delete categories -
 * callers resolve the key to an actual category id via [CategoryRepository.findByKey].
 */
object Categorizer {

    private val keywordMap: Map<String, String> = mapOf(
        "swiggy" to "FOOD",
        "zomato" to "FOOD",
        "starbucks" to "FOOD",
        "restaurant" to "FOOD",
        "cafe" to "FOOD",
        "ola" to "TRANSPORT",
        "uber" to "TRANSPORT",
        "rapido" to "TRANSPORT",
        "metro" to "TRANSPORT",
        "petrol" to "TRANSPORT",
        "fuel" to "TRANSPORT",
        "amazon" to "SHOPPING",
        "myntra" to "SHOPPING",
        "flipkart" to "SHOPPING",
        "ajio" to "SHOPPING",
        "electricity" to "BILLS",
        "airtel" to "BILLS",
        "jio" to "BILLS",
        "vodafone" to "BILLS",
        "vi " to "BILLS",
        "broadband" to "BILLS",
        "recharge" to "BILLS",
        "netflix" to "ENTERTAINMENT",
        "hotstar" to "ENTERTAINMENT",
        "spotify" to "ENTERTAINMENT",
        "prime video" to "ENTERTAINMENT",
        "movie" to "ENTERTAINMENT",
        "bookmyshow" to "ENTERTAINMENT",
        "apollo" to "HEALTH",
        "pharmeasy" to "HEALTH",
        "pharmacy" to "HEALTH",
        "hospital" to "HEALTH",
        "clinic" to "HEALTH",
        "bigbazaar" to "GROCERIES",
        "big bazaar" to "GROCERIES",
        "dmart" to "GROCERIES",
        "grocery" to "GROCERIES",
        "groceries" to "GROCERIES",
        "blinkit" to "GROCERIES",
        "zepto" to "GROCERIES",
        "instamart" to "GROCERIES",
    )

    /** Returns null when no keyword matches - callers fall back to the "Uncategorized" category. */
    fun suggestCategoryKey(merchant: String): String? {
        val normalized = merchant.lowercase()
        return keywordMap.entries.firstOrNull { (keyword, _) -> normalized.contains(keyword) }?.value
    }
}
