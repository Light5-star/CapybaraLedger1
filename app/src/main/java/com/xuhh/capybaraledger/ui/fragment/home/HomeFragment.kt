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

    override fun onResume() {
        super.onResume()
        loadBillData()
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
        // 回调，用于处理账单点击事件
        val billClickCallback: (BillWithCategory) -> Unit = { billWithCategory ->

        }

        // 创建 BillAdapter 实例
        billAdapter = BillAdapter(billClickCallback)

        // 配置 RecyclerView
        mBinding.rvBills.apply {
            adapter = billAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun loadBillData() {
        lifecycleScope.launch {
            try {
                val currentDate = getCurrentDate()
                val ledgerId = currentLedger?.id ?: 1L

                // 获取带分类信息的账单数据
                val billsWithCategory = withContext(Dispatchers.IO) {
                    database.billDao().getBillsByDate(
                        date = parseDateToTimestamp(currentDate),
                        ledgerId = ledgerId
                    )
                }
                // 传递 BillWithCategory 列表给 Adapter
                billAdapter.submitList(billsWithCategory)
                calculateDailyBalance(ledgerId,currentDate)
                mBinding.tvEmpty.visibility = if (billsWithCategory.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Log.e("HomeFragment", "加载账单失败", e)
            }
        }
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