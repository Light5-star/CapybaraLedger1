package com.xuhh.capybaraledger.ui.activity.bill_edit_activity

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.adapter.BillAdapter
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
    private lateinit var billAdapter: BillAdapter
    private val TAG = "BillEditActivity"
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
        mBinding.tvLedger.setOnClickListener {
            LedgerSelectorDialog(this) { ledger ->
                currentLedger = ledger
                mBinding.tvLedger.text = ledger.name
                loadBillData()
            }.show()
        }
    }

    private fun loadBillData() {
        lifecycleScope.launch {
            try {
                val currentDate = getCurrentDate()
                val ledgerId = currentLedger?.id ?: 1L
                Log.d(TAG, "Loading bills for date: $currentDate, ledgerId: $ledgerId")

                // 在后台线程加载数据
                val bills = withContext(Dispatchers.IO) {
                    database.billDao().getBillsByDate(currentDate, ledgerId)
                }

                Log.d(TAG, "Loaded ${bills.size} bills")

                // 在主线程更新UI
                withContext(Dispatchers.Main) {
                    updateBillList(bills)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading bills", e)
            }
        }
    }

    private fun updateBillList(bills: List<Bill>) {
        Log.d(TAG, "Updating UI with ${bills.size} bills")
        if (bills.isEmpty()) {
            Log.d(TAG, "Showing empty state")
        } else {
            billAdapter.submitList(bills)
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
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