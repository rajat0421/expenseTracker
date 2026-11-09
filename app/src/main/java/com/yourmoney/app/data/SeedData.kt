package com.yourmoney.app.data

import com.yourmoney.app.model.Category
import com.yourmoney.app.model.Expense
import com.yourmoney.app.model.ExpenseSource
import java.time.LocalDate

/** Realistic sample expenses used to seed an empty database on first run. */
object SeedData {

    fun expenses(): List<Expense> {
        val today = LocalDate.now()
        return listOf(
            Expense(merchant = "Big Bazaar Groceries", category = Category.GROCERIES, amountPaise = 245_000, date = today, source = ExpenseSource.MANUAL),
            Expense(merchant = "Swiggy Order", category = Category.FOOD, amountPaise = 48_000, date = today, source = ExpenseSource.MANUAL),
            Expense(merchant = "Ola Cab", category = Category.TRANSPORT, amountPaise = 22_000, date = today.minusDays(1), source = ExpenseSource.MANUAL),
            Expense(merchant = "Electricity Bill", category = Category.BILLS, amountPaise = 180_000, date = today.minusDays(1), source = ExpenseSource.MANUAL),
            Expense(merchant = "Myntra Shopping", category = Category.SHOPPING, amountPaise = 329_900, date = today.minusDays(2), source = ExpenseSource.MANUAL),
            Expense(merchant = "Netflix Subscription", category = Category.ENTERTAINMENT, amountPaise = 64_900, date = today.minusDays(3), source = ExpenseSource.MANUAL),
            Expense(merchant = "Apollo Pharmacy", category = Category.HEALTH, amountPaise = 56_000, date = today.minusDays(3), source = ExpenseSource.MANUAL),
            Expense(merchant = "Starbucks Coffee", category = Category.FOOD, amountPaise = 34_000, date = today.minusDays(4), source = ExpenseSource.MANUAL),
            Expense(merchant = "Metro Card Recharge", category = Category.TRANSPORT, amountPaise = 50_000, date = today.minusDays(5), source = ExpenseSource.MANUAL),
            Expense(merchant = "Amazon Order", category = Category.SHOPPING, amountPaise = 189_900, date = today.minusDays(6), source = ExpenseSource.MANUAL),
            Expense(merchant = "Mobile Recharge", category = Category.BILLS, amountPaise = 59_900, date = today.minusDays(7), source = ExpenseSource.MANUAL),
            Expense(merchant = "Movie Tickets", category = Category.ENTERTAINMENT, amountPaise = 76_000, date = today.minusDays(8), source = ExpenseSource.MANUAL),
            Expense(merchant = "DMart Groceries", category = Category.GROCERIES, amountPaise = 312_000, date = today.minusDays(9), source = ExpenseSource.MANUAL),
        )
    }
}
