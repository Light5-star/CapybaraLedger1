package com.xuhh.capybaraledger.application

import android.app.Application
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.repository.BillRepository
import com.xuhh.capybaraledger.data.repository.LedgerRepository
import com.xuhh.capybaraledger.data.repository.UserRepository

class App : Application() {
    private val database by lazy { AppDatabase.getInstance(this) }

    val billRepository by lazy {
        BillRepository(database.billDao())
    }

    val ledgerRepository by lazy {
        LedgerRepository(database.ledgerDao())
    }

    val userRepository by lazy {
        UserRepository(database.userDao())
    }
}