package com.yourmoney.app.data

import com.yourmoney.app.model.Category

/**
 * Keyword-based merchant -> category guesser. This is the seam Phase 2
 * (SMS / notification parsing) will also call to auto-categorize expenses
 * extracted from bank and payment-app messages.
 */
object Categorizer {

    private val keywordMap: Map<String, Category> = mapOf(
        "swiggy" to Category.FOOD,
        "zomato" to Category.FOOD,
        "starbucks" to Category.FOOD,
        "restaurant" to Category.FOOD,
        "cafe" to Category.FOOD,
        "ola" to Category.TRANSPORT,
        "uber" to Category.TRANSPORT,
        "rapido" to Category.TRANSPORT,
        "metro" to Category.TRANSPORT,
        "petrol" to Category.TRANSPORT,
        "fuel" to Category.TRANSPORT,
        "amazon" to Category.SHOPPING,
        "myntra" to Category.SHOPPING,
        "flipkart" to Category.SHOPPING,
        "ajio" to Category.SHOPPING,
        "electricity" to Category.BILLS,
        "airtel" to Category.BILLS,
        "jio" to Category.BILLS,
        "vodafone" to Category.BILLS,
        "vi " to Category.BILLS,
        "broadband" to Category.BILLS,
        "recharge" to Category.BILLS,
        "netflix" to Category.ENTERTAINMENT,
        "hotstar" to Category.ENTERTAINMENT,
        "spotify" to Category.ENTERTAINMENT,
        "prime video" to Category.ENTERTAINMENT,
        "movie" to Category.ENTERTAINMENT,
        "bookmyshow" to Category.ENTERTAINMENT,
        "apollo" to Category.HEALTH,
        "pharmeasy" to Category.HEALTH,
        "pharmacy" to Category.HEALTH,
        "hospital" to Category.HEALTH,
        "clinic" to Category.HEALTH,
        "bigbazaar" to Category.GROCERIES,
        "big bazaar" to Category.GROCERIES,
        "dmart" to Category.GROCERIES,
        "grocery" to Category.GROCERIES,
        "groceries" to Category.GROCERIES,
        "blinkit" to Category.GROCERIES,
        "zepto" to Category.GROCERIES,
        "instamart" to Category.GROCERIES,
    )

    fun suggestCategory(merchant: String): Category {
        val normalized = merchant.lowercase()
        return keywordMap.entries.firstOrNull { (keyword, _) -> normalized.contains(keyword) }
            ?.value
            ?: Category.OTHER
    }
}
