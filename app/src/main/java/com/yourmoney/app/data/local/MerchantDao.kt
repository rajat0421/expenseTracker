package com.yourmoney.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantDao {

    @Query("SELECT * FROM merchants ORDER BY displayName ASC")
    fun observeAll(): Flow<List<MerchantEntity>>

    @Query(
        """
        SELECT a.alias as alias, m.id as merchantId, m.displayName as displayName,
               m.categoryId as categoryId, a.isUserAdded as isUserAdded
        FROM merchant_aliases a
        INNER JOIN merchants m ON m.id = a.merchantId
        """,
    )
    fun observeAliasLookup(): Flow<List<MerchantAliasLookup>>

    @Query("SELECT COUNT(*) FROM merchants")
    suspend fun count(): Int

    @Query("SELECT * FROM merchants WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): MerchantEntity?

    @Query("SELECT * FROM merchants WHERE displayName = :displayName COLLATE NOCASE LIMIT 1")
    suspend fun findByDisplayName(displayName: String): MerchantEntity?

    @Query("SELECT * FROM merchant_aliases WHERE alias = :alias LIMIT 1")
    suspend fun findAliasExact(alias: String): MerchantAliasEntity?

    @Query("SELECT COUNT(*) FROM expenses WHERE merchant = (SELECT displayName FROM merchants WHERE id = :merchantId)")
    suspend fun countExpensesUsing(merchantId: Long): Int

    @Insert
    suspend fun insertMerchant(entity: MerchantEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMerchants(entities: List<MerchantEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlias(entity: MerchantAliasEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAliases(entities: List<MerchantAliasEntity>)

    @Update
    suspend fun updateMerchant(entity: MerchantEntity)

    @Delete
    suspend fun deleteMerchant(entity: MerchantEntity)

    @Query("DELETE FROM merchant_aliases WHERE merchantId = :merchantId")
    suspend fun deleteAliasesForMerchant(merchantId: Long)
}
