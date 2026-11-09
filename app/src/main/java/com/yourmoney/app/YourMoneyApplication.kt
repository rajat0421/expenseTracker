package com.yourmoney.app

import android.app.Application
import com.yourmoney.app.data.ExpenseRepository
import com.yourmoney.app.data.PendingExpenseRepository
import com.yourmoney.app.data.ScanHistoryRepository
import com.yourmoney.app.data.local.AppDatabase
import com.yourmoney.app.data.sms.CandidateProcessor
import com.yourmoney.app.data.sms.HistoricalScanner
import com.yourmoney.app.data.sms.PendingExpenseNotifier
import com.yourmoney.app.data.sms.SmsMessageReader
import com.yourmoney.app.data.sms.TransactionParser

class YourMoneyApplication : Application() {

    private val database by lazy { AppDatabase.getInstance(this) }

    val repository: ExpenseRepository by lazy { ExpenseRepository(database.expenseDao()) }

    val pendingExpenseRepository: PendingExpenseRepository by lazy {
        PendingExpenseRepository(database.pendingExpenseDao(), database.expenseDao())
    }

    val scanHistoryRepository: ScanHistoryRepository by lazy { ScanHistoryRepository(database.scanHistoryDao()) }

    val transactionParser: TransactionParser by lazy { TransactionParser() }

    val candidateProcessor: CandidateProcessor by lazy { CandidateProcessor(pendingExpenseRepository) }

    val historicalScanner: HistoricalScanner by lazy {
        HistoricalScanner(
            smsMessageReader = SmsMessageReader(this),
            parser = transactionParser,
            candidateProcessor = candidateProcessor,
            scanHistoryRepository = scanHistoryRepository,
        )
    }

    override fun onCreate() {
        super.onCreate()
        PendingExpenseNotifier.ensureChannel(this)
    }
}
