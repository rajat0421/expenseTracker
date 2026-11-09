package com.yourmoney.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourmoney.app.model.Expense
import com.yourmoney.app.model.ExpenseSource
import java.time.LocalDate

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchant: String,
    val categoryId: Long,
    val amountPaise: Long,
    val date: LocalDate,
    val note: String?,
    val source: ExpenseSource,
    val accountRef: String? = null,
    val transactionRef: String? = null,
    val bankSender: String? = null,
    val fingerprint: String? = null,
)

fun ExpenseEntity.toDomain() = Expense(
    id = id,
    merchant = merchant,
    categoryId = categoryId,
    amountPaise = amountPaise,
    date = date,
    note = note,
    source = source,
    accountRef = accountRef,
    transactionRef = transactionRef,
    bankSender = bankSender,
    fingerprint = fingerprint,
)

fun Expense.toEntity() = ExpenseEntity(
    id = id,
    merchant = merchant,
    categoryId = categoryId,
    amountPaise = amountPaise,
    date = date,
    note = note,
    source = source,
    accountRef = accountRef,
    transactionRef = transactionRef,
    bankSender = bankSender,
    fingerprint = fingerprint,
)
