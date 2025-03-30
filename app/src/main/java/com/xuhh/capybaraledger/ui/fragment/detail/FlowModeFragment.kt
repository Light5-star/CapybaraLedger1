package com.xuhh.capybaraledger.ui.fragment.detail

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuhh.capybaraledger.adapter.DateSection
import com.xuhh.capybaraledger.adapter.DateSectionAdapter
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.databinding.FragmentDetailsListBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class FlowModeFragment : BaseFragment<FragmentDetailsListBinding>() {
    private lateinit var dateSectionAdapter: DateSectionAdapter
    private val database by lazy { AppDatabase.getInstance(requireContext()) }

    override fun initBinding(): FragmentDetailsListBinding {
        return FragmentDetailsListBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        setupRecyclerView()
        loadBillData()
    }

    override fun onResume() {
        super.onResume()
        loadBillData()
    }

    private fun setupRecyclerView() {
        dateSectionAdapter = DateSectionAdapter { bill ->
            // TODO: 处理账单点击事件
        }
        mBinding.rvBills.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dateSectionAdapter
        }
    }

    fun loadBillData() {
        if (!isAdded) return
        viewLifecycleOwner.lifecycleScope.launch {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                try {
                    // 获取当前账本
                    val currentLedger = database.ledgerDao().getLedgerById(1L) ?: return@launch

                    // 获取所有账单
                    val bills = database.billDao().getBillsByLedger(currentLedger.id)

                    // 按日期分组
                    val dateSections = bills.groupBy { billWithCategory: BillWithCategory ->
                        // 将时间戳转换为日期（去掉时分秒）
                        val calendar = Calendar.getInstance()
                        calendar.time = Date(billWithCategory.bill.time)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        calendar.time
                    }.map { (date: Date, billsList: List<BillWithCategory>) ->
                        // 计算当日结余
                        val balance = billsList.sumOf { billWithCategory: BillWithCategory -> billWithCategory.bill.amount }
                        DateSection(date, billsList, balance)
                    }.sortedByDescending { dateSection: DateSection -> dateSection.date }

                    // 更新UI
                    dateSectionAdapter.submitList(dateSections)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
} 