package com.yourmoney.app

import android.app.Application
import com.yourmoney.app.data.CategoryRepository
import com.yourmoney.app.data.ExpenseRepository
import com.yourmoney.app.data.MerchantRepository
import com.yourmoney.app.data.PendingExpenseRepository
import com.yourmoney.app.data.ScanHistoryRepository
import com.yourmoney.app.data.local.AppDatabase
import com.yourmoney.app.data.merchant.MerchantRecognitionEngine
import com.yourmoney.app.data.sms.CandidateProcessor
import com.yourmoney.app.data.sms.HistoricalScanner
import com.yourmoney.app.data.sms.PendingExpenseNotifier
import com.yourmoney.app.data.sms.SmsMessageReader
import com.yourmoney.app.data.sms.TransactionParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class YourMoneyApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database by lazy { AppDatabase.getInstance(this) }

    val repository: ExpenseRepository by lazy { ExpenseRepository(database.expenseDao()) }

    val categoryRepository: CategoryRepository by lazy { CategoryRepository(database.categoryDao()) }

    val merchantRepository: MerchantRepository by lazy { MerchantRepository(database.merchantDao()) }

    val merchantRecognitionEngine: MerchantRecognitionEngine by lazy { MerchantRecognitionEngine(merchantRepository) }

    val pendingExpenseRepository: PendingExpenseRepository by lazy {
        PendingExpenseRepository(database.pendingExpenseDao(), database.expenseDao())
    }

    val scanHistoryRepository: ScanHistoryRepository by lazy { ScanHistoryRepository(database.scanHistoryDao()) }

    val transactionParser: TransactionParser by lazy { TransactionParser() }

    val candidateProcessor: CandidateProcessor by lazy {
        CandidateProcessor(pendingExpenseRepository, categoryRepository, merchantRecognitionEngine)
    }

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
        applicationScope.launch {
            // Merchant seeding looks up category ids by key, so categories must be seeded first.
            categoryRepository.ensureSeeded()
            merchantRepository.ensureSeeded(categoryRepository)
        }
    }
}
