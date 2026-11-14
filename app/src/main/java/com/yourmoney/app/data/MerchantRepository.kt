package com.yourmoney.app.data

import com.yourmoney.app.data.local.MerchantAliasEntity
import com.yourmoney.app.data.local.MerchantAliasLookup
import com.yourmoney.app.data.local.MerchantDao
import com.yourmoney.app.data.local.MerchantEntity
import com.yourmoney.app.data.merchant.DefaultMerchantData
import com.yourmoney.app.data.merchant.MerchantTextCleaner
import kotlinx.coroutines.flow.Flow

sealed class DeleteMerchantResult {
    data object Deleted : DeleteMerchantResult()
    data object InUse : DeleteMerchantResult()
    data object BuiltIn : DeleteMerchantResult()
    data object NotFound : DeleteMerchantResult()
}

class MerchantRepository(private val dao: MerchantDao) {

    fun observeAll(): Flow<List<MerchantEntity>> = dao.observeAll()

    fun observeAliasLookup(): Flow<List<MerchantAliasLookup>> = dao.observeAliasLookup()

    suspend fun ensureSeeded(categoryRepository: CategoryRepository) {
        if (dao.count() > 0) return
        DefaultMerchantData.seed().forEach { seedMerchant ->
            val categoryId = categoryRepository.findByKey(seedMerchant.categoryKey)?.id
                ?: categoryRepository.uncategorizedId()
            val merchantId = dao.insertMerchant(
                MerchantEntity(displayName = seedMerchant.displayName, categoryId = categoryId, isBuiltIn = true),
            )
            dao.insertAliases(
                seedMerchant.aliases.map {
                    MerchantAliasEntity(merchantId = merchantId, alias = MerchantTextCleaner.clean(it), isUserAdded = false)
                },
            )
        }
    }

    suspend fun updateCategory(merchantId: Long, categoryId: Long) {
        dao.findById(merchantId)?.let { dao.updateMerchant(it.copy(categoryId = categoryId)) }
    }

    suspend fun delete(merchantId: Long): DeleteMerchantResult {
        val existing = dao.findById(merchantId) ?: return DeleteMerchantResult.NotFound
        if (existing.isBuiltIn) return DeleteMerchantResult.BuiltIn
        if (dao.countExpensesUsing(merchantId) > 0) return DeleteMerchantResult.InUse
        dao.deleteAliasesForMerchant(merchantId)
        dao.deleteMerchant(existing)
        return DeleteMerchantResult.Deleted
    }

    /**
     * Called when the user manually corrects a merchant/category during review. Creates (or
     * reuses, by exact display name) a merchant and attaches the original narration as a new
     * alias, so the same business is recognized automatically next time.
     */
    suspend fun rememberMerchant(originalMerchantText: String, displayName: String, categoryId: Long) {
        val cleanedAlias = MerchantTextCleaner.clean(originalMerchantText)
        if (cleanedAlias.isBlank()) return

        val trimmedName = displayName.trim()
        if (trimmedName.isBlank()) return

        val existing = dao.findByDisplayName(trimmedName)
        val merchantId = if (existing != null) {
            if (existing.categoryId != categoryId) {
                dao.updateMerchant(existing.copy(categoryId = categoryId))
            }
            existing.id
        } else {
            dao.insertMerchant(MerchantEntity(displayName = trimmedName, categoryId = categoryId, isBuiltIn = false))
        }

        if (dao.findAliasExact(cleanedAlias) == null) {
            dao.insertAlias(MerchantAliasEntity(merchantId = merchantId, alias = cleanedAlias, isUserAdded = true))
        }
    }
}
