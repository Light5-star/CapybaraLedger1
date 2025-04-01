package com.xuhh.capybaraledger.ui.view.ledgerselect

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.DialogLedgerSelectorBinding
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LedgerSelectorDialog(
    context: Context,
    private val viewModel: BillViewModel,
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
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = LedgerSelectorAdapter { ledger ->
            coroutineScope.launch {
                viewModel.updateCurrentLedger(ledger)
                onLedgerSelected(ledger)
                dismiss()
            }
        }

        binding.rvLedgers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@LedgerSelectorDialog.adapter
        }
    }

    private fun observeViewModel() {
        coroutineScope.launch {
            viewModel.ledgers.collect { ledgers ->
                adapter.submitList(ledgers)
            }
        }
    }

    override fun dismiss() {
        coroutineScope.cancel()
        super.dismiss()
    }
} 