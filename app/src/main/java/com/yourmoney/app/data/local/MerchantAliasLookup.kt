package com.yourmoney.app.data.local

/** Flat projection of an alias joined with its merchant, for in-memory matching. */
data class MerchantAliasLookup(
    val alias: String,
    val merchantId: Long,
    val displayName: String,
    val categoryId: Long,
    val isUserAdded: Boolean,
)
