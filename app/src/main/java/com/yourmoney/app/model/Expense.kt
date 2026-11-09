package com.yourmoney.app.model

import java.time.LocalDate

/**
 * [amountPaise] is the expense amount stored as integer paise (₹1 = 100 paise)
 * to avoid floating point rounding errors in totals.
 */
data class Expense(
    val id: Long = 0,
    val merchant: String,
    val category: Category,
    val amountPaise: Long,
    val date: LocalDate,
    val note: String? = null,
    val source: ExpenseSource = ExpenseSource.MANUAL,
)

fun Long.paiseToRupees(): Double = this / 100.0
fun Double.rupeesToPaise(): Long = Math.round(this * 100.0)
