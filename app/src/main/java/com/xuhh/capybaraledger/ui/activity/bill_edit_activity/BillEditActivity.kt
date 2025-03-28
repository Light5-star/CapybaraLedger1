package com.xuhh.capybaraledger.ui.activity.bill_edit_activity

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.lifecycleScope
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Category
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.ActivityBillEditBinding
import com.xuhh.capybaraledger.ui.base.BaseActivity
import com.xuhh.capybaraledger.ui.view.ledgerselect.LedgerSelectorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BillEditActivity : BaseActivity<ActivityBillEditBinding>() {
    private lateinit var database: AppDatabase
    private var currentLedger: Ledger? = null
    private var currentCategory: Category? = null
    private var isExpense = true // true为支出，false为收入

    override fun initBinding(): ActivityBillEditBinding {
        return ActivityBillEditBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        database = AppDatabase.getInstance(this)
        setupToolbar()
        setupLedgerSelector()
        setupAmountInput()
        setupDatePicker()
        setupTimePicker()
        loadDefaultLedger()
    }

    private fun setupLedgerSelector() {
    }

    private fun loadDefaultLedger() {
    }

    private fun setupToolbar() {
        mBinding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }


    private fun setupAmountInput() {
        mBinding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 可以在这里添加金额格式化的逻辑
            }
        })
    }

    private fun setupDatePicker() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Date()
        mBinding.tvDate.text = dateFormat.format(currentDate)
        mBinding.tvDate.setOnClickListener {
            // TODO: 显示日期选择器
        }
    }

    private fun setupTimePicker() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = Date()
        mBinding.tvTime.text = timeFormat.format(currentTime)
        mBinding.tvTime.setOnClickListener {
        }
    }

    private fun saveBill() {
        val amount = mBinding.etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val note = mBinding.etNote.text.toString()
        val payee = mBinding.etPayee.text.toString()

        if (amount <= 0) {
            return
        }

        if (currentCategory == null) {
            return
        }

        if (currentLedger == null) {
            return
        }

        val bill = Bill(
            ledgerId = currentLedger!!.id,
            category = currentCategory!!.name,
            amount = amount,
            type = if (isExpense) Bill.TYPE_EXPENSE else Bill.TYPE_INCOME,
            date = System.currentTimeMillis(),
            time = System.currentTimeMillis(),
            note = note.takeIf { it.isNotBlank() },
            payee = payee.takeIf { it.isNotBlank() }
        )

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    database.billDao().insert(bill)
                }
                finish()
            } catch (e: Exception) {
            }
        }
    }
}