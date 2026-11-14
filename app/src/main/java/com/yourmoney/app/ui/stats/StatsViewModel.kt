package com.yourmoney.app.ui.stats

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class StatsViewModel(
    expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _period = MutableStateFlow(StatsPeriod.DAILY)
    val period: StateFlow<StatsPeriod> = _period

    private val _referenceDate = MutableStateFlow(LocalDate.now())
    val referenceDate: StateFlow<LocalDate> = _referenceDate

    val expenses: StateFlow<List<Expense>> = expenseRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val buckets: StateFlow<List<StatBucket>> = combine(expenses, _period, _referenceDate) { expenseList, period, referenceDate ->
        buildBuckets(expenseList, period, referenceDate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val summary: StateFlow<StatsSummary> = buckets
        .map { buildSummary(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), buildSummary(emptyList()))

    fun setPeriod(value: StatsPeriod) {
        _period.value = value
    }

    fun setReferenceMonth(monthAnchor: LocalDate) {
        val today = LocalDate.now()
        val clampedDay = monthAnchor.dayOfMonth.coerceAtMost(monthAnchor.lengthOfMonth())
        val candidate = monthAnchor.withDayOfMonth(clampedDay)
        _referenceDate.value = if (candidate.isAfter(today)) today else candidate
    }

    fun shiftReferenceMonth(deltaMonths: Int) {
        setReferenceMonth(_referenceDate.value.plusMonths(deltaMonths.toLong()))
    }
}
