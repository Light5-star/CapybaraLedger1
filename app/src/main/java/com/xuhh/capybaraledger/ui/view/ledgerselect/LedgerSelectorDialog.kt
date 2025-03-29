package com.xuhh.capybaraledger.ui.view.ledgerselect

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.DialogLedgerSelectorBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LedgerSelectorDialog(
    context: Context,
    private val onLedgerSelected: (Ledger) -> Unit
) : Dialog(context) {
    private lateinit var binding: DialogLedgerSelectorBinding
    private lateinit var adapter: LedgerSelectorAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var ledgerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogLedgerSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置对话框宽度为屏幕宽度的85%
        window?.apply {
            val params = attributes
            params.width = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes = params
            setBackgroundDrawableResource(android.R.color.transparent)
        }

        // 设置关闭按钮点击事件
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // 设置RecyclerView
        setupRecyclerView()
        
        // 加载账本数据
        loadLedgers()
    }

    private fun setupRecyclerView() {
        adapter = LedgerSelectorAdapter { ledger ->
            // 处理账本选择
            coroutineScope.launch(Dispatchers.IO) {
                val database = AppDatabase.getInstance(context)
                database.ledgerDao().safeSetDefaultLedger(ledger.id)
                withContext(Dispatchers.Main) {
                    onLedgerSelected(ledger)
                    dismiss()
                }
            }
        }

        binding.rvLedgers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@LedgerSelectorDialog.adapter
        }
    }

    private fun loadLedgers() {
        ledgerJob = coroutineScope.launch {
            val database = AppDatabase.getInstance(context)
            database.ledgerDao().getAllLedgersFlow().collectLatest { ledgers ->
                adapter.submitList(ledgers)
            }
        }
    }

    override fun dismiss() {
        ledgerJob?.cancel()
        super.dismiss()
    }
} 