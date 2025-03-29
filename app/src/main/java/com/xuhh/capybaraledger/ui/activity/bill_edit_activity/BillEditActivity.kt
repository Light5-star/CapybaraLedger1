package com.xuhh.capybaraledger.ui.activity.bill_edit_activity

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.adapter.BillAdapter
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Category
import com.xuhh.capybaraledger.data.model.Categories
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.ActivityBillEditBinding
import com.xuhh.capybaraledger.ui.base.BaseActivity
import com.xuhh.capybaraledger.ui.view.billtypeselect.BillTypeSelectorDialog
import com.xuhh.capybaraledger.ui.view.ledgerselect.LedgerSelectorDialog
import kotlinx.coroutines.Dispatchers
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
        setupTypeSelector()
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

    private fun setupTypeSelector() {
        mBinding.rgType.setOnCheckedChangeListener { _, checkedId ->
            isExpense = checkedId == R.id.rb_expense
            setupCategorySpinner()
        }
    }

    private fun setupCategorySpinner() {
        val categories = if (isExpense) {
            Categories.EXPENSE_CATEGORIES
        } else {
            Categories.INCOME_CATEGORIES
        }
        
        // 更新UI显示分类
        if (categories.isNotEmpty()) {
            currentCategory = categories[0]
            mBinding.tvCategory.text = currentCategory?.name
        }
    }

    private fun loadBillData() {
        lifecycleScope.launch {
            try {
                val currentDate = getCurrentDate()
                val ledgerId = currentLedger?.id ?: 1L
                Log.d(TAG, "Loading bills for date: $currentDate, ledgerId: $ledgerId")

                // 在后台线程加载数据
                val billWithCategory = withContext(Dispatchers.IO) {
                }
                // 在主线程更新UI
                withContext(Dispatchers.Main) {
//                    updateBillList(bills)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading bills", e)
            }
        }
    }

    private fun updateBillList(bills: List<BillWithCategory>) {
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
        lifecycleScope.launch {
            try {
                val defaultLedger = withContext(Dispatchers.IO) {
                    database.ledgerDao().getDefaultLedger()
                }
                if (defaultLedger != null) {
                    currentLedger = defaultLedger
                    mBinding.tvLedger.text = defaultLedger.name
                    loadBillData()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading default ledger", e)
            }
        }
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

        lifecycleScope.launch {
            try {
                finish()
            } catch (e: Exception) {
            }
        }
    }
}