package com.yourmoney.app

import android.app.Application
import com.yourmoney.app.data.ExpenseRepository
import com.yourmoney.app.data.SeedData
import com.yourmoney.app.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class YourMoneyApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val repository: ExpenseRepository by lazy {
        ExpenseRepository(AppDatabase.getInstance(this).expenseDao())
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            repository.seedIfEmpty(SeedData.expenses())
        }
    }
}
