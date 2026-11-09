package com.yourmoney.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourmoney.app.data.ExpenseRepository
import com.yourmoney.app.data.PendingExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    expenseRepository: ExpenseRepository,
    pendingExpenseRepository: PendingExpenseRepository,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        expenseRepository.observeAll(),
        pendingExpenseRepository.observePendingCount(),
    ) { expenses, pendingCount ->
        buildDashboardState(expenses).copy(pendingReviewCount = pendingCount)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}
