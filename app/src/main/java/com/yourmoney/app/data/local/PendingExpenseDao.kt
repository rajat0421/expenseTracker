package com.yourmoney.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.yourmoney.app.model.ReviewStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingExpenseDao {

    @Query("SELECT * FROM pending_expenses WHERE status = :status ORDER BY transactionAt DESC")
    fun observeByStatus(status: ReviewStatus): Flow<List<PendingExpenseEntity>>

    @Query("SELECT COUNT(*) FROM pending_expenses WHERE status = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM pending_expenses WHERE fingerprint = :fingerprint LIMIT 1")
    suspend fun findByFingerprint(fingerprint: String): PendingExpenseEntity?

    @Query("SELECT * FROM pending_expenses WHERE id = :id")
    suspend fun getById(id: Long): PendingExpenseEntity?

    @Insert
    suspend fun insert(entity: PendingExpenseEntity): Long

    @Update
    suspend fun update(entity: PendingExpenseEntity)

    @Query("UPDATE pending_expenses SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: ReviewStatus)

    @Query("UPDATE pending_expenses SET status = :status WHERE id IN (:ids)")
    suspend fun updateStatusForIds(ids: List<Long>, status: ReviewStatus)
}
