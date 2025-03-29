package com.xuhh.capybaraledger.ui.fragment.home

import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.adapter.BillAdapter
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.FragmentHomeBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.ui.view.ledgerselect.LedgerSelectorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class HomeFragment: BaseFragment<FragmentHomeBinding>() {
    private lateinit var billAdapter: BillAdapter
    private lateinit var database: AppDatabase
    private var currentLedger: Ledger? = null

    override fun initBinding(): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        database = AppDatabase.getInstance(requireContext())
        setDate()
        setQuote()
        loadDefaultLedger()
        setupLedgerSelector()
        setupRecyclerView()

    }

    private fun loadDefaultLedger() {
        lifecycleScope.launch {
            try {
                // 从数据库加载默认账本
                val defaultLedger = withContext(Dispatchers.IO) {
                    database.ledgerDao().getDefaultLedger()
                }
                defaultLedger?.let {
                    currentLedger = it
                    mBinding.tvLedgerName.text = it.name
                    loadBillData()
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "加载默认账本失败", e)
            }
        }
    }

    private fun setupLedgerSelector() {
        mBinding.layoutLedger.setOnClickListener {
            LedgerSelectorDialog(requireContext()) { ledger ->
                currentLedger = ledger
                mBinding.tvLedgerName.text = ledger.name
                loadBillData()
            }.show()
        }
    }

    private fun setupRecyclerView() {
        Log.d("HomeFragment", "开始设置 RecyclerView")
        
        // 回调，用于处理账单点击事件
        val billClickCallback: (BillWithCategory) -> Unit = { billWithCategory ->
            Log.d("HomeFragment", "账单被点击：${billWithCategory.bill.id}")
        }

        // 创建 BillAdapter 实例
        billAdapter = BillAdapter(billClickCallback)
        Log.d("HomeFragment", "BillAdapter 创建完成")

        // 配置 RecyclerView
        mBinding.rvBills.apply {
            adapter = billAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            // 添加分割线
            addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
        Log.d("HomeFragment", "RecyclerView 配置完成")
    }

    // HomeFragment.kt 中的 loadBillData 方法
    private fun loadBillData() {
        lifecycleScope.launch {
            try {
                val currentDate = getCurrentDate()
                val ledgerId = currentLedger?.id ?: 1L
                Log.d("HomeFragment", "开始加载账单数据，日期：$currentDate，账本ID：$ledgerId")

                // 获取带分类信息的账单数据
                val billsWithCategory = withContext(Dispatchers.IO) {
                    database.billDao().getBillsByDate(
                        date = parseDateToTimestamp(currentDate),
                        ledgerId = ledgerId
                    )
                }
                Log.d("HomeFragment", "获取到 ${billsWithCategory.size} 条账单数据")

                // 计算结余
                val balance = calculateBalance(billsWithCategory)
                Log.d("HomeFragment", "计算得到结余：$balance")

                // 在主线程更新 UI
                withContext(Dispatchers.Main) {
                    // 更新账单列表
                    billAdapter.submitSortedList(billsWithCategory)
                    Log.d("HomeFragment", "更新账单列表完成")

                    // 更新结余显示
                    mBinding.tvBalance.text = String.format(
                        if (balance >= 0) "+%.2f" else "%.2f",
                        balance
                    )
                    Log.d("HomeFragment", "更新结余显示完成")

                    // 根据是否有数据显示空状态
                    if (billsWithCategory.isEmpty()) {
                        mBinding.tvEmpty.visibility = View.VISIBLE
                        mBinding.rvBills.visibility = View.GONE
                        Log.d("HomeFragment", "显示空状态")
                    } else {
                        mBinding.tvEmpty.visibility = View.GONE
                        mBinding.rvBills.visibility = View.VISIBLE
                        Log.d("HomeFragment", "显示账单列表")
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "加载账单数据失败", e)
            }
        }
    }

    private fun calculateBalance(bills: List<BillWithCategory>): Double {
        var balance = 0.0
        bills.forEach { billWithCategory ->
            val amount = billWithCategory.bill.amount
            if (billWithCategory.bill.type == Bill.TYPE_INCOME) {
                balance += amount
            } else {
                balance -= amount
            }
        }
        return balance
    }

    // 日期字符串转时间戳（需实现）
    private fun parseDateToTimestamp(date: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.parse(date)?.time ?: 0L
    }

    //更新账单
    private fun updateBillList(bills: List<BillWithCategory>) {
        if (bills.isNotEmpty()) {
            billAdapter.submitList(bills)
        }
    }

    //设置每日一句
    private fun setQuote() {
        val quotes = resources.getStringArray(R.array.daily_quotes)
        val randomQuote = quotes[Random().nextInt(quotes.size)]
        mBinding.tvQuote.text = randomQuote
    }
    //获取日期
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    //设置日期
    private fun setDate() {
        val dateFormat = SimpleDateFormat("MM月dd日 E", Locale.CHINESE)
        mBinding.tvDate.text = dateFormat.format(Date())
    }

    // 新增结余计算方法
    private suspend fun calculateDailyBalance(ledgerId: Long, date: String) {
        withContext(Dispatchers.IO) {
            val startTimestamp = parseDateToTimestamp(date)
            val endTimestamp = startTimestamp + 86400000 // 加1天时间戳

            // 获取当日总收入
            val income = database.billDao().getExpenseAmount(
                type = Bill.TYPE_INCOME,
                startDate = startTimestamp,
                endDate = endTimestamp
            ) ?: 0.0

            // 获取当日总支出
            val expense = database.billDao().getExpenseAmount(
                type = Bill.TYPE_EXPENSE,
                startDate = startTimestamp,
                endDate = endTimestamp
            ) ?: 0.0

            // 更新UI
            withContext(Dispatchers.Main) {
                val balance = income - expense
                mBinding.tvBalance.text = "今日结余：${String.format("%.2f", balance)}"
            }
        }
    }
}