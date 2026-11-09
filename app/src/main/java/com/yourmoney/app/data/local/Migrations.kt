package com.yourmoney.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Explicit, non-destructive migrations. Never fall back to destructive migration in
 * production — existing expense history must survive every schema change.
 */
object Migrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE expenses ADD COLUMN accountRef TEXT")
            db.execSQL("ALTER TABLE expenses ADD COLUMN transactionRef TEXT")
            db.execSQL("ALTER TABLE expenses ADD COLUMN bankSender TEXT")
            db.execSQL("ALTER TABLE expenses ADD COLUMN fingerprint TEXT")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS pending_expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    merchant TEXT NOT NULL,
                    category TEXT NOT NULL,
                    amountPaise INTEGER NOT NULL,
                    transactionAt INTEGER NOT NULL,
                    accountRef TEXT,
                    transactionRef TEXT,
                    bankSender TEXT,
                    confidence TEXT NOT NULL,
                    status TEXT NOT NULL,
                    fingerprint TEXT NOT NULL,
                    source TEXT NOT NULL,
                    detectedAtMillis INTEGER NOT NULL
                )
                """.trimIndent(),
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS scan_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    rangeStartEpochDay INTEGER NOT NULL,
                    rangeEndEpochDay INTEGER NOT NULL,
                    scannedAtMillis INTEGER NOT NULL,
                    messagesExamined INTEGER NOT NULL,
                    candidatesFound INTEGER NOT NULL,
                    errorCount INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE pending_expenses ADD COLUMN bankName TEXT")
            db.execSQL("ALTER TABLE pending_expenses ADD COLUMN paymentMethod TEXT")
            db.execSQL("ALTER TABLE pending_expenses ADD COLUMN originalSmsBody TEXT")
        }
    }
}
