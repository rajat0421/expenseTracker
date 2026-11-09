package com.yourmoney.app.data

import com.yourmoney.app.data.local.ScanHistoryDao
import com.yourmoney.app.data.local.ScanHistoryEntity
import com.yourmoney.app.data.local.toDomain
import com.yourmoney.app.model.ScanRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ScanHistoryRepository(private val dao: ScanHistoryDao) {

    suspend fun record(start: LocalDate, end: LocalDate, examined: Int, candidates: Int, errors: Int) {
        dao.insert(
            ScanHistoryEntity(
                rangeStartEpochDay = start.toEpochDay(),
                rangeEndEpochDay = end.toEpochDay(),
                scannedAtMillis = System.currentTimeMillis(),
                messagesExamined = examined,
                candidatesFound = candidates,
                errorCount = errors,
            ),
        )
    }

    suspend fun lastScan(): ScanRecord? = dao.getLast()?.toDomain()

    fun observeHistory(): Flow<List<ScanRecord>> = dao.observeAll().map { entities -> entities.map { it.toDomain() } }
}
