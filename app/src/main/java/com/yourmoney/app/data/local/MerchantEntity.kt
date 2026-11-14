package com.yourmoney.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A recognized business (e.g. "Swiggy"), distinct from the payment app or gateway that may
 * have processed the transaction. [isBuiltIn] marks the curated seed data so the UI can
 * protect it from accidental deletion; built-in merchants can still be recategorized.
 */
@Entity(tableName = "merchants", indices = [Index(value = ["displayName"], unique = true)])
data class MerchantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val categoryId: Long,
    val isBuiltIn: Boolean,
)
