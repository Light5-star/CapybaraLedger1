package com.xuhh.capybaraledger.ui.activity.bill_edit_activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
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
import com.xuhh.capybaraledger.ui.view.billtypeselect.CategoryDialog
import com.xuhh.capybaraledger.ui.view.ledgerselect.LedgerSelectorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.ViewModelProvider
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory

class BillEditActivity : BaseActivity<ActivityBillEditBinding>() {
    private lateinit var billAdapter: BillAdapter
    private val TAG = "BillEditActivity"
    private lateinit var database: AppDatabase
    private lateinit var viewModel: BillViewModel
    private var currentLedger: Ledger? = null
    private var currentCategory: Category? = null
    private var isExpense = true // true为支出，false为收入


    override fun initBinding(): ActivityBillEditBinding {
        return ActivityBillEditBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        val app = application as App
        val factory = ViewModelFactory(app.ledgerRepository, app.billRepository)
        viewModel = ViewModelProvider(this, factory)[BillViewModel::class.java]

        database = AppDatabase.getInstance(this)
        setupToolbar()
        setupLedgerSelector()
        setupTypeSelector()
        setupAmountInput()
        setupDatePicker()
        setupTimePicker()
        loadDefaultLedger()
        setUpCategorySelector()
        mBinding.btnSave.setOnClickListener {
            saveBill()
        }
        mBinding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setUpCategorySelector() {
        mBinding.tvCategory.setOnClickListener {
            // 根据收支类型获取对应分类
            val categories = if (isExpense) {
                Categories.EXPENSE_CATEGORIES
            } else {
                Categories.INCOME_CATEGORIES
            }

            // 创建分类选择弹窗
            CategoryDialog(
                context = this,
                categories = categories,
                selectedCategory = currentCategory,
                onCategorySelected = { category ->
                    currentCategory = category
                    mBinding.tvCategory.text = category.name
                }
            ).show()
        }
    }

    private fun setupLedgerSelector() {
        mBinding.tvLedger.setOnClickListener {
            LedgerSelectorDialog(
                this,
                viewModel
            ) { ledger ->
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
    //日期选择器
    private fun setupDatePicker() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Date()
        mBinding.tvDate.text = dateFormat.format(currentDate)
        mBinding.tvDate.setOnClickListener {
            // 获取当前日期（用于初始化选择器）
            val calendar = Calendar.getInstance()
            val initialYear = calendar.get(Calendar.YEAR)
            val initialMonth = calendar.get(Calendar.MONTH)
            val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

            // 创建日期选择对话框
            DatePickerDialog(
                this, // Context
//                有BUG
//                R.style.MyDialogTheme,
                { _, year, month, dayOfMonth -> // 日期选择回调
                    // 构造选中的日期
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }.time

                    // 更新界面显示
                    mBinding.tvDate.text = dateFormat.format(selectedDate)
                },
                initialYear,   // 默认显示年份
                initialMonth,  // 默认显示月份（0-11）
                initialDay     // 默认显示日期
            ).apply {
                // 可选：设置最小/最大可选日期（示例）
                // datePicker.minDate = System.currentTimeMillis() // 限制只能选今天之后的日期
            }.show()
        }
    }
    //时间选择器
    private fun setupTimePicker() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = Date()
        mBinding.tvTime.text = timeFormat.format(currentTime)
        mBinding.tvTime.setOnClickListener {
            // 获取当前时间
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            // 创建时间选择对话框
            TimePickerDialog(
                this, // 上下文
//                有BUG
//                R.style.MyDialogTheme,
                { _, hourOfDay, minute -> // 时间选择回调
                    // 格式化用户选择的时间
                    val selectedTime = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                        set(Calendar.MINUTE, minute)
                    }.time

                    // 更新界面显示
                    mBinding.tvTime.text = timeFormat.format(selectedTime)
                },
                currentHour,  // 默认显示小时
                currentMinute, // 默认显示分钟
                true // 使用24小时制（false为12小时制）
            ).show()
        }
    }

    private fun saveBill() {
        // 1. 验证输入
        val amount = mBinding.etAmount.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            showToast("请输入正确的金额")
            return
        }

        if (currentCategory == null) {
            showToast("请选择分类")
            return
        }

        if (currentLedger == null) {
            showToast("请选择账本")
            return
        }

        val note = mBinding.etNote.text.toString()
        val payee = mBinding.etPayee.text.toString()

        lifecycleScope.launch {
            try {
                // 2. 获取日期和时间
                val dateStr = mBinding.tvDate.text.toString()
                val timeStr = mBinding.tvTime.text.toString()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                val date = dateFormat.parse(dateStr)?.time
                val time = timeFormat.parse(timeStr)?.time

                if (date == null || time == null) {
                    showToast("日期或时间格式错误")
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    // 打印当前选择的分类和账本信息
                    Log.d(TAG, "Current Category: ${currentCategory?.name}")
                    Log.d(TAG, "Current Ledger: ${currentLedger?.id}, ${currentLedger?.name}")

                    // 首先检查分类是否存在
                    var categoryToUse = database.categoryDao().getCategoryByName(currentCategory!!.name)
                    Log.d(TAG, "Existing Category: ${categoryToUse?.id}, ${categoryToUse?.name}")
                    
                    if (categoryToUse == null) {
                        // 如果分类不存在，先插入分类
                        Log.d(TAG, "Inserting new category: ${currentCategory!!.name}")
                        val categoryId = database.categoryDao().insert(currentCategory!!)
                        Log.d(TAG, "Category insert result: $categoryId")
                        
                        // 重新获取插入后的分类
                        categoryToUse = database.categoryDao().getCategoryByName(currentCategory!!.name)
                        Log.d(TAG, "Category after insert: ${categoryToUse?.id}, ${categoryToUse?.name}")
                        
                        if (categoryToUse == null) {
                            throw Exception("分类插入失败")
                        }
                    }

                    // 检查账本是否存在
                    val existingLedger = database.ledgerDao().getLedgerById(currentLedger!!.id)
                    Log.d(TAG, "Existing Ledger: ${existingLedger?.id}, ${existingLedger?.name}")
                    
                    if (existingLedger == null) {
                        // 如果账本不存在，先插入账本
                        Log.d(TAG, "Inserting new ledger: ${currentLedger!!.id}, ${currentLedger!!.name}")
                        database.ledgerDao().insert(currentLedger!!)
                    }

                    // 再次验证分类和账本是否存在
                    val finalCategoryCheck = database.categoryDao().getCategoryByName(currentCategory!!.name)
                    val finalLedgerCheck = database.ledgerDao().getLedgerById(currentLedger!!.id)

                    Log.d(TAG, "Final Category Check: ${finalCategoryCheck?.id}, ${finalCategoryCheck?.name}")
                    Log.d(TAG, "Final Ledger Check: ${finalLedgerCheck?.id}, ${finalLedgerCheck?.name}")

                    if (finalCategoryCheck == null || finalLedgerCheck == null) {
                        throw Exception("分类或账本创建失败")
                    }

                    // 创建并保存账单
                    val bill = Bill(
                        id = 0, // 自动生成
                        ledgerId = currentLedger!!.id,
                        categoryId = finalCategoryCheck.id, // 使用最终检查的分类ID
                        amount = amount,
                        type = if (isExpense) Bill.TYPE_EXPENSE else Bill.TYPE_INCOME,
                        date = date,
                        time = time,
                        note = note.takeIf { it.isNotBlank() },
                        payee = payee.takeIf { it.isNotBlank() }
                    )

                    Log.d(TAG, "Saving bill with ledgerId: ${bill.ledgerId}, categoryId: ${bill.categoryId}")
                    // 保存账单
                    val billId = database.billDao().insert(bill)
                    Log.d(TAG, "Bill insert result: $billId")

                    // 验证账单是否真的保存了
                    val savedBill = database.billDao().getBillById(billId)
                    Log.d(TAG, "Saved bill verification: ${savedBill?.id}, ${savedBill?.categoryId}, ${savedBill?.ledgerId}")
                }

                // 显示成功提示并返回
                withContext(Dispatchers.Main) {
                    showToast("保存成功")
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving bill", e)
                withContext(Dispatchers.Main) {
                    showToast("保存失败：${e.message}")
                }
            }
        }
    }
}