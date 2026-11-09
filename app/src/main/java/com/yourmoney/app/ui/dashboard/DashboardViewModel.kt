package com.yourmoney.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourmoney.app.data.CategoryRepository
import com.yourmoney.app.data.ExpenseRepository
import com.yourmoney.app.data.PendingExpenseRepository
import com.yourmoney.app.data.local.CategoryEntity
import com.yourmoney.app.model.Expense
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class DashboardViewModel(
    private val expenseRepository: ExpenseRepository,
    pendingExpenseRepository: PendingExpenseRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<DashboardUiState> = combine(
        expenseRepository.observeAll(),
        pendingExpenseRepository.observePendingCount(),
        categoryRepository.observeAll(),
    ) { expenses, pendingCount, categories ->
        val categoryNames = categories.associate { it.id to it.name }
        buildDashboardState(expenses, categoryNames).copy(pendingReviewCount = pendingCount)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun updateExpense(expense: Expense, merchant: String, categoryId: Long, amountPaise: Long, date: LocalDate) =
        viewModelScope.launch {
            expenseRepository.updateExpense(
                expense.copy(merchant = merchant, categoryId = categoryId, amountPaise = amountPaise, date = date),
            )
        }
}
