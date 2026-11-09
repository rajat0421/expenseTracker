package com.yourmoney.app.data

import com.yourmoney.app.data.local.CategoryDao
import com.yourmoney.app.data.local.CategoryEntity
import com.yourmoney.app.data.local.DefaultCategories
import kotlinx.coroutines.flow.Flow

sealed class DeleteCategoryResult {
    data object Deleted : DeleteCategoryResult()
    data object InUse : DeleteCategoryResult()
    data object NotFound : DeleteCategoryResult()
}

class CategoryRepository(private val dao: CategoryDao) {

    fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()

    suspend fun ensureSeeded() {
        if (dao.count() == 0) {
            dao.insertAll(DefaultCategories.seed())
        }
    }

    suspend fun findByKey(key: String): CategoryEntity? = dao.findByKey(key)

    suspend fun findById(id: Long): CategoryEntity? = dao.findById(id)

    /** Falls back to the seeded "Uncategorized" row, creating it if somehow missing. */
    suspend fun uncategorizedId(): Long {
        dao.findByKey(DefaultCategories.UNCATEGORIZED_KEY)?.let { return it.id }
        return dao.insert(CategoryEntity(key = DefaultCategories.UNCATEGORIZED_KEY, name = "Uncategorized"))
    }

    suspend fun rename(id: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return
        dao.findById(id)?.let { dao.update(it.copy(name = trimmed)) }
    }

    suspend fun addCustom(name: String): Long? {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return null
        return dao.insert(CategoryEntity(key = null, name = trimmed))
    }

    suspend fun delete(id: Long): DeleteCategoryResult {
        val existing = dao.findById(id) ?: return DeleteCategoryResult.NotFound
        val inUse = dao.countExpensesUsing(id) > 0 || dao.countPendingUsing(id) > 0
        if (inUse) return DeleteCategoryResult.InUse
        dao.delete(existing)
        return DeleteCategoryResult.Deleted
    }
}
