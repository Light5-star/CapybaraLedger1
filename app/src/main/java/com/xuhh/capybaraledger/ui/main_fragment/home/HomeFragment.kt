package com.xuhh.capybaraledger.ui.main_fragment.home

import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.adapter.BillAdapter
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
    private val TAG = "HomeFragment"
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
        setupRecyclerView()
        loadDefaultLedger()
        setupLedgerSelector()
    }

    private fun loadDefaultLedger() {
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
        val billClickCallback: (Bill) -> Unit = { bill ->

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
                Log.d(TAG, "Loading bills for date: $currentDate, ledgerId: $ledgerId")
                
                // 在后台线程加载数据
                val bills = withContext(Dispatchers.IO) {
                }
                
                // 在主线程更新UI
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

    private fun setQuote() {
        val quotes = resources.getStringArray(R.array.daily_quotes)
        val randomQuote = quotes[Random().nextInt(quotes.size)]
        mBinding.tvQuote.text = randomQuote
    }

    private fun setDate() {
        val dateFormat = SimpleDateFormat("MM月dd日 E", Locale.CHINESE)
        mBinding.tvDate.text = dateFormat.format(Date())
    }
}