package com.yourmoney.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A spending category. [key] is a stable internal identifier used only by the built-in
 * seeded categories (so the SMS categorizer and migrations can find them regardless of
 * how the user has renamed [name]); user-added categories have [key] = null. [name] is
 * the user-editable display label - renaming it never breaks anything else since all
 * other tables reference categories by [id], not by name.
 */
@Entity(tableName = "categories", indices = [Index(value = ["key"], unique = true)])
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String?,
    val name: String,
)
