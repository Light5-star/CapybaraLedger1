package com.xuhh.capybaraledger.ui.activity.ledger

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.ActivityLedgerManageBinding
import com.xuhh.capybaraledger.databinding.DialogDeleteConfirmBinding
import com.xuhh.capybaraledger.dialog.AddLedgerDialog
import com.xuhh.capybaraledger.ui.base.BaseActivity
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class LedgerManageActivity : BaseActivity<ActivityLedgerManageBinding>() {
    private val mViewModel: BillViewModel by viewModels {
        val app = application as App
        ViewModelFactory(app.ledgerRepository, app.billRepository)
    }
    private lateinit var adapter: LedgerManageAdapter

    override fun initBinding(): ActivityLedgerManageBinding {
        return ActivityLedgerManageBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initView() {
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        mBinding.btnBack.setOnClickListener {
            finish()
        }

        mBinding.btnAdd.setOnClickListener {
            AddLedgerDialog(this, mViewModel) {
                // 添加完成后会自动更新列表
            }.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRecyclerView() {
        adapter = LedgerManageAdapter(
            onSetDefault = { ledger ->
                mViewModel.setDefaultLedger(ledger.id)
            },
            onDelete = { ledger ->
                showDeleteConfirmDialog(ledger)
            }
        )

        mBinding.rvLedgers.apply {
            layoutManager = LinearLayoutManager(this@LedgerManageActivity)
            adapter = this@LedgerManageActivity.adapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeViewModel() {
        lifecycleScope.launch {
            mViewModel.ledgers.collect { ledgers ->
                adapter.submitList(ledgers)
            }
        }
    }

    private fun showDeleteConfirmDialog(ledger: Ledger) {
        val dialog = Dialog(this)
        val binding = DialogDeleteConfirmBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        // 设置对话框宽度为屏幕宽度的85%
        dialog.window?.apply {
            val params = attributes
            params.width = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            attributes = params
            setBackgroundDrawableResource(android.R.color.transparent)
        }

        // 设置消息
        binding.tvMessage.text = "确定要删除账本${ledger.name}吗？删除后该账本下的所有账单记录都将被删除，且无法恢复。"

        // 设置按钮点击事件
        binding.btnConfirm.setOnClickListener {
            mViewModel.deleteLedger(ledger.id)
            showToast("账本已删除")
            dialog.dismiss()
        }

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
} 