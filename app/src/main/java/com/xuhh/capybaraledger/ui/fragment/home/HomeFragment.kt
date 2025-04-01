package com.xuhh.capybaraledger.ui.fragment.home

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.adapter.BillAdapter
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.databinding.FragmentHomeBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.ui.view.ledgerselect.LedgerSelectorDialog
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class HomeFragment: BaseFragment<FragmentHomeBinding>() {
    private lateinit var mViewModel: BillViewModel
    private lateinit var billAdapter: BillAdapter
    private lateinit var database: AppDatabase

    override fun initBinding(): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }

    override fun onResume() {
        super.onResume()
        // 不再需要手动调用 loadBillData
    }

    override fun initView() {
        super.initView()
        // 获取 Application 实例
        val app = requireActivity().application as App

        // 创建 ViewModel
        val factory = ViewModelFactory(app.ledgerRepository, app.billRepository)
        mViewModel = ViewModelProvider(this, factory)[BillViewModel::class.java]

        database = AppDatabase.getInstance(requireContext())
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        setDate()
        setQuote()
        setupLedgerSelector()
        setupRecyclerView()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 观察当前账本
            mViewModel.currentLedger.collect { ledger ->
                ledger?.let {
                    mBinding.tvLedgerName.text = it.name
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 观察账单列表
            mViewModel.bills.collect { bills ->
                billAdapter.submitList(bills)
                mBinding.tvEmpty.visibility = if (bills.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 观察余额
            mViewModel.balance.collect { balance ->
                mBinding.tvBalance.text = "今日结余：${String.format("%.2f", balance)}"
            }
        }
    }

    private fun setupLedgerSelector() {
        mBinding.layoutLedger.setOnClickListener {
            LedgerSelectorDialog(
                requireContext(),
                mViewModel
            ) { ledger ->
                // 这里不需要手动调用 updateCurrentLedger，因为在 Dialog 中已经调用了
                // 这里可以添加其他需要的操作
            }.show()
        }
    }

    private fun setupRecyclerView() {
        billAdapter = BillAdapter { billWithCategory ->
            // 处理账单点击事件
        }

        mBinding.rvBills.apply {
            adapter = billAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
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