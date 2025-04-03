package com.xuhh.capybaraledger.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.xuhh.capybaraledger.data.converter.ReminderTypeConverters
import com.xuhh.capybaraledger.data.dao.BillDao
import com.xuhh.capybaraledger.data.dao.CategoryDao
import com.xuhh.capybaraledger.data.dao.LedgerDao
import com.xuhh.capybaraledger.data.dao.ReminderDao
import com.xuhh.capybaraledger.data.dao.UserDao
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Categories
import com.xuhh.capybaraledger.data.model.Category
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.data.model.Reminder
import com.xuhh.capybaraledger.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Bill::class, Ledger::class, User::class, Category::class, Reminder::class],
    version = 2
)
@TypeConverters(ReminderTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        private const val TAG = "AppDatabase"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "BillDatabase.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration() // 如果数据库版本不匹配，重建数据库
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
                        userId = "defaultUser",
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

                    Categories.getAllCategories().forEach { category ->
                        database.categoryDao().insert(category)
                    }

                }
            }
        }
    }
}