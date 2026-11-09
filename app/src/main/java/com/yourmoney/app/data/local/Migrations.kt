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

    /**
     * Replaces the hardcoded Category enum (stored as a TEXT column) with a real,
     * user-editable `categories` table referenced by id. SQLite on minSdk 26 devices may
     * not support ALTER TABLE ... DROP COLUMN (added in SQLite 3.35, not guaranteed until
     * much newer Android versions), so both tables are rebuilt via the standard
     * create-new-table / copy / drop-old / rename pattern instead.
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `key` TEXT,
                    name TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_categories_key ON categories(`key`)")

            // Fixed ids 1..11, matching DefaultCategories.seed() order exactly.
            val seeded = listOf(
                1L to ("FOOD" to "Food & Dining"),
                2L to ("GROCERIES" to "Groceries"),
                3L to ("TRANSPORT" to "Transportation"),
                4L to ("SHOPPING" to "Shopping"),
                5L to ("ENTERTAINMENT" to "Entertainment"),
                6L to ("BILLS" to "Bills & Utilities"),
                7L to ("HEALTH" to "Healthcare"),
                8L to ("EDUCATION" to "Education"),
                9L to ("TRAVEL" to "Travel"),
                10L to ("OTHER" to "Other"),
                11L to ("UNCATEGORIZED" to "Uncategorized"),
            )
            seeded.forEach { (id, keyAndName) ->
                val (key, name) = keyAndName
                db.execSQL(
                    "INSERT OR IGNORE INTO categories (id, `key`, name) VALUES ($id, '$key', '${name.replace("'", "''")}')",
                )
            }

            val categoryCase = """
                CASE category
                    WHEN 'FOOD' THEN 1
                    WHEN 'GROCERIES' THEN 2
                    WHEN 'TRANSPORT' THEN 3
                    WHEN 'SHOPPING' THEN 4
                    WHEN 'ENTERTAINMENT' THEN 5
                    WHEN 'BILLS' THEN 6
                    WHEN 'HEALTH' THEN 7
                    WHEN 'EDUCATION' THEN 8
                    WHEN 'TRAVEL' THEN 9
                    WHEN 'OTHER' THEN 10
                    ELSE 11
                END
            """.trimIndent()

            db.execSQL(
                """
                CREATE TABLE expenses_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    merchant TEXT NOT NULL,
                    categoryId INTEGER NOT NULL,
                    amountPaise INTEGER NOT NULL,
                    date INTEGER NOT NULL,
                    note TEXT,
                    source TEXT NOT NULL,
                    accountRef TEXT,
                    transactionRef TEXT,
                    bankSender TEXT,
                    fingerprint TEXT
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO expenses_new
                    (id, merchant, categoryId, amountPaise, date, note, source, accountRef, transactionRef, bankSender, fingerprint)
                SELECT id, merchant, $categoryCase, amountPaise, date, note, source, accountRef, transactionRef, bankSender, fingerprint
                FROM expenses
                """.trimIndent(),
            )
            db.execSQL("DROP TABLE expenses")
            db.execSQL("ALTER TABLE expenses_new RENAME TO expenses")

            db.execSQL(
                """
                CREATE TABLE pending_expenses_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    merchant TEXT NOT NULL,
                    categoryId INTEGER NOT NULL,
                    amountPaise INTEGER NOT NULL,
                    transactionAt INTEGER NOT NULL,
                    accountRef TEXT,
                    transactionRef TEXT,
                    bankSender TEXT,
                    bankName TEXT,
                    paymentMethod TEXT,
                    confidence TEXT NOT NULL,
                    status TEXT NOT NULL,
                    fingerprint TEXT NOT NULL,
                    source TEXT NOT NULL,
                    detectedAtMillis INTEGER NOT NULL,
                    originalSmsBody TEXT
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO pending_expenses_new
                    (id, merchant, categoryId, amountPaise, transactionAt, accountRef, transactionRef, bankSender,
                     bankName, paymentMethod, confidence, status, fingerprint, source, detectedAtMillis, originalSmsBody)
                SELECT id, merchant, $categoryCase, amountPaise, transactionAt, accountRef, transactionRef, bankSender,
                       bankName, paymentMethod, confidence, status, fingerprint, source, detectedAtMillis, originalSmsBody
                FROM pending_expenses
                """.trimIndent(),
            )
            db.execSQL("DROP TABLE pending_expenses")
            db.execSQL("ALTER TABLE pending_expenses_new RENAME TO pending_expenses")
        }
    }
}
