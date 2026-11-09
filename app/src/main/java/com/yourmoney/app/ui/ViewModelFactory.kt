package com.yourmoney.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourmoney.app.YourMoneyApplication

@Composable
fun yourMoneyApp(): YourMoneyApplication {
    val context = LocalContext.current.applicationContext
    return context as YourMoneyApplication
}

/**
 * Builds a ViewModel from the current [YourMoneyApplication]'s singleton repositories,
 * without reflection — each screen supplies its own constructor call, so a ViewModel can
 * depend on any combination of repositories.
 */
@Composable
inline fun <reified T : ViewModel> rememberFactoryViewModel(crossinline creator: (YourMoneyApplication) -> T): T {
    val app = yourMoneyApp()
    val factory = remember(app) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator(app) as VM
        }
    }
    return viewModel(factory = factory)
}
