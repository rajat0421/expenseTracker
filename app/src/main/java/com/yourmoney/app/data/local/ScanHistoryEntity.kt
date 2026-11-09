package com.yourmoney.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourmoney.app.model.ScanRecord

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rangeStartEpochDay: Long,
    val rangeEndEpochDay: Long,
    val scannedAtMillis: Long,
    val messagesExamined: Int,
    val candidatesFound: Int,
    val errorCount: Int,
)

fun ScanHistoryEntity.toDomain() = ScanRecord(
    id = id,
    rangeStartEpochDay = rangeStartEpochDay,
    rangeEndEpochDay = rangeEndEpochDay,
    scannedAtMillis = scannedAtMillis,
    messagesExamined = messagesExamined,
    candidatesFound = candidatesFound,
    errorCount = errorCount,
)
