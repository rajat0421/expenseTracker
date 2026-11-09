package com.yourmoney.app.ui.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourmoney.app.data.CategoryRepository
import com.yourmoney.app.data.ExpenseRepository
import com.yourmoney.app.data.local.CategoryEntity
import com.yourmoney.app.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    repository: ExpenseRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    var searchText by mutableStateOf("")
        private set

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val expenses: StateFlow<List<Expense>> = combine(
        repository.observeAll(),
        categoryRepository.observeAll(),
        query,
    ) { expenses, categories, q ->
        if (q.isBlank()) {
            expenses
        } else {
            val categoryNames = categories.associate { it.id to it.name }
            expenses.filter { expense ->
                expense.merchant.contains(q, ignoreCase = true) ||
                    (categoryNames[expense.categoryId]?.contains(q, ignoreCase = true) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearchTextChange(text: String) {
        searchText = text
        query.value = text
    }
}
