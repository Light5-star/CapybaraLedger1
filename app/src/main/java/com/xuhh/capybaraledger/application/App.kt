package com.xuhh.capybaraledger.application

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.model.ThemeType
import com.xuhh.capybaraledger.data.repository.BillRepository
import com.xuhh.capybaraledger.data.repository.LedgerRepository
import com.xuhh.capybaraledger.data.repository.ReminderRepository
import com.xuhh.capybaraledger.viewmodel.StatisticsViewModel

class App : Application() {
    // 数据库实例（单例）
    val database by lazy { AppDatabase.getInstance(this) }

    // 全局共享的 Repository
    val ledgerRepository by lazy { LedgerRepository(database.ledgerDao()) }
    val billRepository by lazy { BillRepository(database.billDao()) }
    val reminderRepository by lazy { ReminderRepository(database.reminderDao()) }

    override fun onCreate() {
        super.onCreate()
        applyTheme()
    }

    private fun applyTheme() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val themeName = prefs.getString("app_theme", ThemeType.DEFAULT.name)
        val theme = ThemeType.valueOf(themeName ?: ThemeType.DEFAULT.name)
        
        // 设置夜间模式
        if (theme == ThemeType.DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}