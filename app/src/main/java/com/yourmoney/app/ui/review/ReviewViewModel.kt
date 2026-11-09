package com.yourmoney.app.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourmoney.app.data.PendingExpenseRepository
import com.yourmoney.app.model.Category
import com.yourmoney.app.model.PendingExpense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReviewViewModel(private val repository: PendingExpenseRepository) : ViewModel() {

    val pending: StateFlow<List<PendingExpense>> = repository.observePending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds

    fun toggleSelected(id: Long) {
        _selectedIds.update { current -> if (id in current) current - id else current + id }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun approve(id: Long) = viewModelScope.launch { repository.approve(id) }

    fun reject(id: Long) = viewModelScope.launch { repository.reject(id) }

    fun approveSelected() = viewModelScope.launch {
        repository.approveAll(_selectedIds.value.toList())
        clearSelection()
    }

    fun rejectSelected() = viewModelScope.launch {
        repository.rejectAll(_selectedIds.value.toList())
        clearSelection()
    }

    fun updateDraft(id: Long, merchant: String, category: Category, amountPaise: Long, transactionDate: LocalDate? = null) =
        viewModelScope.launch {
            repository.updateDraft(id, merchant, category, amountPaise, transactionDate)
        }
}
