package com.yourmoney.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT * FROM expenses WHERE merchant LIKE '%' || :query || '%' " +
            "OR category LIKE '%' || :query || '%' ORDER BY date DESC, id DESC"
    )
    fun search(query: String): Flow<List<ExpenseEntity>>

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun count(): Int
}
