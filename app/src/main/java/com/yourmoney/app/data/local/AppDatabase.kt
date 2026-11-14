package com.yourmoney.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ExpenseEntity::class,
        PendingExpenseEntity::class,
        ScanHistoryEntity::class,
        CategoryEntity::class,
        MerchantEntity::class,
        MerchantAliasEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun pendingExpenseDao(): PendingExpenseDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun categoryDao(): CategoryDao
    abstract fun merchantDao(): MerchantDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yourmoney.db",
                )
                    .addMigrations(
                        Migrations.MIGRATION_1_2,
                        Migrations.MIGRATION_2_3,
                        Migrations.MIGRATION_3_4,
                        Migrations.MIGRATION_4_5,
                    )
                    .build()
                    .also { instance = it }
            }
    }
}
