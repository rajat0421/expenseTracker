package com.yourmoney.app.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourmoney.app.data.CategoryRepository
import com.yourmoney.app.data.PendingExpenseRepository
import com.yourmoney.app.data.local.CategoryEntity
import com.yourmoney.app.model.PendingExpense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class SortOption(val label: String) {
    DATE_NEWEST("Date (newest)"),
    DATE_OLDEST("Date (oldest)"),
    AMOUNT_HIGH("Amount (high to low)"),
    AMOUNT_LOW("Amount (low to high)"),
}

class ReviewViewModel(
    private val repository: PendingExpenseRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _sortOption = MutableStateFlow(SortOption.DATE_NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption

    val pending: StateFlow<List<PendingExpense>> = combine(
        repository.observePending(),
        _sortOption,
    ) { list, sort ->
        when (sort) {
            SortOption.DATE_NEWEST -> list.sortedByDescending { it.transactionAt }
            SortOption.DATE_OLDEST -> list.sortedBy { it.transactionAt }
            SortOption.AMOUNT_HIGH -> list.sortedByDescending { it.amountPaise }
            SortOption.AMOUNT_LOW -> list.sortedBy { it.amountPaise }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun toggleSelected(id: Long) {
        _selectedIds.update { current -> if (id in current) current - id else current + id }
    }

    fun toggleSelectAll(allIds: List<Long>) {
        _selectedIds.value = if (allIds.isNotEmpty() && _selectedIds.value.containsAll(allIds)) {
            emptySet()
        } else {
            allIds.toSet()
        }
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

    fun updateDraft(id: Long, merchant: String, categoryId: Long, amountPaise: Long, transactionDate: LocalDate? = null) =
        viewModelScope.launch {
            repository.updateDraft(id, merchant, categoryId, amountPaise, transactionDate)
        }
}
