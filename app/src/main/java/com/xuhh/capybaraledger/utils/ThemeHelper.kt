package com.xuhh.capybaraledger.utils

import android.app.Activity
import androidx.preference.PreferenceManager
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.model.ThemeType

object ThemeHelper {
    fun applyTheme(activity: Activity) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val themeName = prefs.getString("app_theme", ThemeType.DEFAULT.name)
        val theme = ThemeType.valueOf(themeName ?: ThemeType.DEFAULT.name)
        
        val themeResId = when (theme) {
            ThemeType.DEFAULT -> R.style.Theme_Capybaraledger_Default
            ThemeType.DARK -> R.style.Theme_Capybaraledger_Dark
            ThemeType.PINK -> R.style.Theme_Capybaraledger_Pink
            ThemeType.BLUE -> R.style.Theme_Capybaraledger_Blue
        }
        
        activity.setTheme(themeResId)
    }
} 