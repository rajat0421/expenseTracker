package com.yourmoney.app.data.merchant

import com.yourmoney.app.data.MerchantRepository
import kotlinx.coroutines.flow.first

/** Suspend wrapper around [MerchantMatcher] for callers that don't already hold a cached
 * alias list (the SMS detection path - a few lookups per message, not per keystroke). */
class MerchantRecognitionEngine(private val merchantRepository: MerchantRepository) {

    suspend fun recognize(rawMerchantText: String): MerchantMatch? {
        val aliases = merchantRepository.observeAliasLookup().first()
        return MerchantMatcher.match(rawMerchantText, aliases)
    }
}
