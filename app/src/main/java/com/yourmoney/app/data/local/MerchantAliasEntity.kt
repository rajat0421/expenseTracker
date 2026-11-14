package com.yourmoney.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One recognizable narration fragment for a [MerchantEntity] (e.g. "SWIGGY", "SWIGGY LTD").
 * Always stored pre-cleaned (see MerchantTextCleaner) so matching is a simple string
 * comparison at lookup time. [isUserAdded] distinguishes curated seed aliases from ones
 * learned via "remember this merchant".
 */
@Entity(tableName = "merchant_aliases", indices = [Index(value = ["alias"], unique = true)])
data class MerchantAliasEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantId: Long,
    val alias: String,
    val isUserAdded: Boolean,
)
