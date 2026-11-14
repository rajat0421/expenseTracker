package com.yourmoney.app.data.sms

import com.yourmoney.app.data.ScanHistoryRepository
import com.yourmoney.app.model.ExpenseSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

data class ScanResult(val messagesExamined: Int, val candidatesFound: Int, val errors: Int)

/**
 * Scans the SMS inbox over a date range, runs every message through the shared
 * [TransactionParser] + [CandidateProcessor], and records a [ScanHistoryRepository] entry.
 * Repeatable/idempotent: re-scanning the same range only adds genuinely new candidates,
 * since [CandidateProcessor] skips anything whose fingerprint already exists as pending,
 * approved or rejected.
 */
class HistoricalScanner(
    private val smsMessageReader: SmsMessageReader,
    private val parser: TransactionParser,
    private val candidateProcessor: CandidateProcessor,
    private val scanHistoryRepository: ScanHistoryRepository,
) {
    suspend fun scan(
        startDate: LocalDate,
        endDate: LocalDate,
        onProgress: (examined: Int, candidates: Int) -> Unit = { _, _ -> },
    ): ScanResult = withContext(Dispatchers.IO) {
        val zone = ZoneId.systemDefault()
        val startMillis = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val messages = smsMessageReader.readInbox(startMillis, endMillis)

        var examined = 0
        var candidates = 0
        var errors = 0

        for (sms in messages) {
            examined++
            try {
                val outcome = parser.parse(sms)
                if (candidateProcessor.process(outcome, ExpenseSource.SMS, sms.body) != null) {
                    candidates++
                }
            } catch (e: Exception) {
                errors++
            }
            onProgress(examined, candidates)
        }

        scanHistoryRepository.record(startDate, endDate, examined, candidates, errors)

        ScanResult(messagesExamined = examined, candidatesFound = candidates, errors = errors)
    }
}
