package com.yourmoney.app.ui.addexpense

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourmoney.app.data.Categorizer
import com.yourmoney.app.data.ExpenseRepository
import com.yourmoney.app.model.Category
import com.yourmoney.app.model.Expense
import com.yourmoney.app.model.ExpenseSource
import com.yourmoney.app.model.rupeesToPaise
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    var merchant by mutableStateOf("")
        private set
    var amountText by mutableStateOf("")
        private set
    var category by mutableStateOf(Category.OTHER)
        private set
    var categoryManuallySet by mutableStateOf(false)
        private set

    val canSave: Boolean
        get() = merchant.isNotBlank() && amountText.toDoubleOrNull()?.let { it > 0 } == true

    fun onMerchantChange(value: String) {
        merchant = value
        if (!categoryManuallySet) {
            category = Categorizer.suggestCategory(value)
        }
    }

    fun onAmountChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            amountText = value
        }
    }

    fun onCategoryChange(value: Category) {
        category = value
        categoryManuallySet = true
    }

    fun save(onSaved: () -> Unit) {
        val amount = amountText.toDoubleOrNull() ?: return
        if (merchant.isBlank() || amount <= 0) return
        viewModelScope.launch {
            repository.addExpense(
                Expense(
                    merchant = merchant.trim(),
                    category = category,
                    amountPaise = amount.rupeesToPaise(),
                    date = LocalDate.now(),
                    source = ExpenseSource.MANUAL,
                )
            )
            onSaved()
        }
    }
}
