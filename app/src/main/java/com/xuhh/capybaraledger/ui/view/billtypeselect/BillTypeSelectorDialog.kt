package com.xuhh.capybaraledger.ui.view.billtypeselect

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.model.Category

class BillTypeSelectorDialog(
    context: Context,
    private val onTypeSelected: (Boolean) -> Unit
) : BottomSheetDialog(context) {

    private lateinit var adapter: BillTypeSelectorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_bill_type_selector)

        // 设置对话框宽度为屏幕宽度
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        // 设置对话框位置在底部
        window?.setGravity(android.view.Gravity.BOTTOM)

        // 设置背景透明
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 初始化RecyclerView
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_types)
        recyclerView?.layoutManager = GridLayoutManager(context, 4)

    }
}