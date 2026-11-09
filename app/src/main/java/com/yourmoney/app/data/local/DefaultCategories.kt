package com.yourmoney.app.data.local

/**
 * Seeded once on first run. Order here also fixes the ids used in [Migrations.MIGRATION_3_4]
 * (id = index + 1) when converting old enum-backed expenses to category ids - keep both in sync
 * if this list ever changes.
 */
object DefaultCategories {

    const val UNCATEGORIZED_KEY = "UNCATEGORIZED"

    fun seed(): List<CategoryEntity> = listOf(
        CategoryEntity(key = "FOOD", name = "Food & Dining"),
        CategoryEntity(key = "GROCERIES", name = "Groceries"),
        CategoryEntity(key = "TRANSPORT", name = "Transportation"),
        CategoryEntity(key = "SHOPPING", name = "Shopping"),
        CategoryEntity(key = "ENTERTAINMENT", name = "Entertainment"),
        CategoryEntity(key = "BILLS", name = "Bills & Utilities"),
        CategoryEntity(key = "HEALTH", name = "Healthcare"),
        CategoryEntity(key = "EDUCATION", name = "Education"),
        CategoryEntity(key = "TRAVEL", name = "Travel"),
        CategoryEntity(key = "OTHER", name = "Other"),
        CategoryEntity(key = UNCATEGORIZED_KEY, name = "Uncategorized"),
    )
}
