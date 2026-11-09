package com.yourmoney.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.yourmoney.app.YourMoneyApplication
import com.yourmoney.app.data.ExpenseRepository

class ViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass
            .getConstructor(ExpenseRepository::class.java)
            .newInstance(repository)
    }
}

@Composable
fun yourMoneyRepository(): ExpenseRepository {
    val app = LocalContext.current.applicationContext as YourMoneyApplication
    return app.repository
}

@Composable
inline fun <reified T : ViewModel> rememberViewModel(): T {
    val factory = ViewModelFactory(yourMoneyRepository())
    return viewModel(factory = factory)
}
