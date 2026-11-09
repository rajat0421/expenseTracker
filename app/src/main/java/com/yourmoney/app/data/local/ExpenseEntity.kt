package com.yourmoney.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourmoney.app.model.Category
import com.yourmoney.app.model.Expense
import com.yourmoney.app.model.ExpenseSource
import java.time.LocalDate

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchant: String,
    val category: Category,
    val amountPaise: Long,
    val date: LocalDate,
    val note: String?,
    val source: ExpenseSource,
)

fun ExpenseEntity.toDomain() = Expense(
    id = id,
    merchant = merchant,
    category = category,
    amountPaise = amountPaise,
    date = date,
    note = note,
    source = source,
)

fun Expense.toEntity() = ExpenseEntity(
    id = id,
    merchant = merchant,
    category = category,
    amountPaise = amountPaise,
    date = date,
    note = note,
    source = source,
)
