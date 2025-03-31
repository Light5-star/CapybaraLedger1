package com.xuhh.capybaraledger.application

import android.app.Application
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.repository.BillRepository
import com.xuhh.capybaraledger.data.repository.LedgerRepository
import com.xuhh.capybaraledger.data.repository.UserRepository

class App : Application() {
    // 数据库实例（单例）
    val database by lazy { AppDatabase.getInstance(this) }

    // 全局共享的 Repository
    val ledgerRepository by lazy { LedgerRepository(database.ledgerDao()) }
    val billRepository by lazy { BillRepository(database.billDao()) }
}