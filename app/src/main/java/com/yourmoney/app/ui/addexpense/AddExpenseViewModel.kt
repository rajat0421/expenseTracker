package com.yourmoney.app.ui.addexpense

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourmoney.app.data.Categorizer
import com.yourmoney.app.data.CategoryRepository
import com.yourmoney.app.data.ExpenseRepository
import com.yourmoney.app.data.local.CategoryEntity
import com.yourmoney.app.model.Expense
import com.yourmoney.app.model.ExpenseSource
import com.yourmoney.app.model.rupeesToPaise
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddExpenseViewModel(
    private val repository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var merchant by mutableStateOf("")
        private set
    var amountText by mutableStateOf("")
        private set
    var date by mutableStateOf(LocalDate.now())
        private set
    var category by mutableStateOf<CategoryEntity?>(null)
        private set
    var categoryManuallySet by mutableStateOf(false)
        private set

    val canSave: Boolean
        get() = merchant.isNotBlank() && amountText.toDoubleOrNull()?.let { it > 0 } == true && category != null

    init {
        viewModelScope.launch {
            val loaded = categoryRepository.observeAll().filter { it.isNotEmpty() }.first()
            if (category == null) {
                category = loaded.firstOrNull { it.key == "OTHER" } ?: loaded.first()
            }
        }
    }

    fun onMerchantChange(value: String) {
        merchant = value
        if (!categoryManuallySet) {
            val key = Categorizer.suggestCategoryKey(value)
            val match = key?.let { k -> categories.value.firstOrNull { it.key == k } }
            if (match != null) category = match
        }
    }

    fun onAmountChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            amountText = value
        }
    }

    fun onDateChange(value: LocalDate) {
        date = value
    }

    fun onCategoryChange(value: CategoryEntity) {
        category = value
        categoryManuallySet = true
    }

    fun save(onSaved: () -> Unit) {
        val amount = amountText.toDoubleOrNull() ?: return
        val selectedCategory = category ?: return
        if (merchant.isBlank() || amount <= 0) return
        viewModelScope.launch {
            repository.addExpense(
                Expense(
                    merchant = merchant.trim(),
                    categoryId = selectedCategory.id,
                    amountPaise = amount.rupeesToPaise(),
                    date = date,
                    source = ExpenseSource.MANUAL,
                )
            )
            onSaved()
        }
    }
}
