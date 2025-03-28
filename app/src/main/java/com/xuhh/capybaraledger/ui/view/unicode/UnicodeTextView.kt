package com.xuhh.capybaraledger.ui.view.unicode

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.xuhh.capybaraledger.R

/**
 * iconfont的使用类
 * 可以直接设置text
 */
class UnicodeTextView(
    context: Context,
    attrs: AttributeSet? = null
): AppCompatTextView(context, attrs) {

    init {
        // 设置字体
        typeface = ResourcesCompat.getFont(context, R.font.iconfont)
    }
}