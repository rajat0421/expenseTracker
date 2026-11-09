package com.yourmoney.app.ui.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourmoney.app.data.ExpenseRepository
import com.yourmoney.app.model.Expense
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val query = MutableStateFlow("")

    var searchText by mutableStateOf("")
        private set

    val expenses: StateFlow<List<Expense>> = query
        .flatMapLatest { q -> if (q.isBlank()) repository.observeAll() else repository.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearchTextChange(text: String) {
        searchText = text
        query.value = text
    }
}
