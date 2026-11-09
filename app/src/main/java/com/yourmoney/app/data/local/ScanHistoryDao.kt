package com.yourmoney.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {

    @Insert
    suspend fun insert(entity: ScanHistoryEntity): Long

    @Query("SELECT * FROM scan_history ORDER BY scannedAtMillis DESC LIMIT 1")
    suspend fun getLast(): ScanHistoryEntity?

    @Query("SELECT * FROM scan_history ORDER BY scannedAtMillis DESC")
    fun observeAll(): Flow<List<ScanHistoryEntity>>
}
