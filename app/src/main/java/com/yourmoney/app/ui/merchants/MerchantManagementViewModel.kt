package com.yourmoney.app.ui.merchants

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourmoney.app.data.CategoryRepository
import com.yourmoney.app.data.DeleteMerchantResult
import com.yourmoney.app.data.MerchantRepository
import com.yourmoney.app.data.local.CategoryEntity
import com.yourmoney.app.data.local.MerchantEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MerchantManagementViewModel(
    private val merchantRepository: MerchantRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    var searchText by mutableStateOf("")
        private set

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val merchants: StateFlow<List<MerchantEntity>> = combine(
        merchantRepository.observeAll(),
        query,
    ) { merchants, q ->
        if (q.isBlank()) merchants else merchants.filter { it.displayName.contains(q, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearchTextChange(text: String) {
        searchText = text
        query.value = text
    }

    fun changeCategory(merchantId: Long, categoryId: Long) = viewModelScope.launch {
        merchantRepository.updateCategory(merchantId, categoryId)
    }

    fun delete(merchantId: Long, onResult: (DeleteMerchantResult) -> Unit) = viewModelScope.launch {
        onResult(merchantRepository.delete(merchantId))
    }
}
