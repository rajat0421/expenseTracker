package com.yourmoney.app.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourmoney.app.data.CategoryRepository
import com.yourmoney.app.data.DeleteCategoryResult
import com.yourmoney.app.data.local.CategoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryManagementViewModel(private val repository: CategoryRepository) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun rename(id: Long, newName: String) {
        viewModelScope.launch { repository.rename(id, newName) }
    }

    fun addCustom(name: String, onResult: (added: Boolean) -> Unit) {
        viewModelScope.launch {
            val id = repository.addCustom(name)
            onResult(id != null)
        }
    }

    fun delete(id: Long, onResult: (DeleteCategoryResult) -> Unit) {
        viewModelScope.launch {
            onResult(repository.delete(id))
        }
    }
}
