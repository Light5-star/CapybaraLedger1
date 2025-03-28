package com.xuhh.capybaraledger.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.xuhh.capybaraledger.data.dao.BillDao
import com.xuhh.capybaraledger.data.dao.LedgerDao
import com.xuhh.capybaraledger.data.dao.UserDao
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Bill::class, Ledger::class, User::class],
    version = 5
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun userDao(): UserDao

    companion object {
        private const val TAG = "AppDatabase"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 定义数据库迁移策略
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 从版本1迁移到版本2的SQL语句
                database.execSQL("ALTER TABLE bills ADD COLUMN payee TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 从版本2迁移到版本3的SQL语句
                database.execSQL("ALTER TABLE bills ADD COLUMN note TEXT")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 从版本3迁移到版本4的SQL语句
                database.execSQL("ALTER TABLE bills ADD COLUMN time TEXT")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建用户表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        nickname TEXT NOT NULL,
                        avatar TEXT,
                        gender INTEGER NOT NULL DEFAULT 0,
                        birthday INTEGER,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "BillDatabase.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration() // 如果迁移失败，重建数据库
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // 在数据库创建时初始化默认数据
                CoroutineScope(Dispatchers.IO).launch {
                    val database = INSTANCE ?: return@launch
                    
                    // 创建默认用户
                    val defaultUser = User(
                        userId = "guest",
                        nickname = "未登录用户",
                        gender = User.GENDER_UNKNOWN
                    )
                    database.userDao().insert(defaultUser)

                    // 创建默认账本
                    val defaultLedger = Ledger(
                        name = "默认账本",
                        description = "系统默认账本",
                        icon = 0,
                        color = 0,
                        isDefault = true,
                        sortOrder = 0
                    )
                    database.ledgerDao().insert(defaultLedger)
                }
            }
        }
    }
}