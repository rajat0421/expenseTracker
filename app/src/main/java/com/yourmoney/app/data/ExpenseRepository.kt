package com.yourmoney.app.data

import com.yourmoney.app.data.local.ExpenseDao
import com.yourmoney.app.data.local.toDomain
import com.yourmoney.app.data.local.toEntity
import com.yourmoney.app.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepository(private val dao: ExpenseDao) {

    fun observeAll(): Flow<List<Expense>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun addExpense(expense: Expense) {
        dao.insert(expense.toEntity())
    }

    suspend fun isEmpty(): Boolean = dao.count() == 0

    suspend fun existsFingerprint(fingerprint: String): Boolean = dao.findByFingerprint(fingerprint) != null

    suspend fun seedIfEmpty(sample: List<Expense>) {
        if (isEmpty()) {
            sample.forEach { dao.insert(it.toEntity()) }
        }
    }
}
