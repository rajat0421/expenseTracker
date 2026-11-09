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

    /**
     * Used for approve/reject: the original SMS body is only ever needed while a candidate
     * is PENDING, so leaving this review state clears it in the same statement - it can
     * never be left behind by a bug in a separate "clear" step run at the wrong time.
     */
    @Query("UPDATE pending_expenses SET status = :status, originalSmsBody = NULL WHERE id = :id")
    suspend fun updateStatusAndClearSms(id: Long, status: ReviewStatus)

    @Query("UPDATE pending_expenses SET status = :status, originalSmsBody = NULL WHERE id IN (:ids)")
    suspend fun updateStatusAndClearSmsForIds(ids: List<Long>, status: ReviewStatus)
}
