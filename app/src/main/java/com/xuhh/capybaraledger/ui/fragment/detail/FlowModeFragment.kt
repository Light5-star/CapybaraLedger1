package com.xuhh.capybaraledger.ui.fragment.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuhh.capybaraledger.adapter.DateSection
import com.xuhh.capybaraledger.adapter.DateSectionAdapter
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.databinding.FragmentDetailsListBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.viewmodel.DetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class FlowModeFragment : BaseFragment<FragmentDetailsListBinding>() {
    private val detailViewModel: DetailViewModel by activityViewModels()
    private lateinit var dateSectionAdapter: DateSectionAdapter
    private var currentLedgerId: Long = 1L
    private var isViewCreated = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewCreated = true
        setupRecyclerView()
        setupObservers()
    }

    private fun setupObservers() {
        detailViewModel.calendar.observe(viewLifecycleOwner) { calendar ->
            if (isViewCreated && isResumed) {
                loadBillData()
            }
        }
    }

    private fun setupRecyclerView() {
        dateSectionAdapter = DateSectionAdapter { billWithCategory ->
            // TODO: 处理账单点击事件
            Log.d("FlowMode", "Bill clicked: ${billWithCategory.bill.id}")
        }
        mBinding.rvBills.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dateSectionAdapter
        }
    }

    fun loadBillData() {
        if (!isAdded || isDetached) return

        lifecycleScope.launch {
            try {
                val database = AppDatabase.getInstance(requireContext())
                val billDao = database.billDao()

                // 使用 ViewModel 的时间范围
                val (startTime, endTime) = detailViewModel.getCurrentMonthRange()
                val currentMonth = detailViewModel.calendar.value?.get(Calendar.MONTH)
                Log.d("FlowMode", "Loading data for month: $currentMonth, range: $startTime to $endTime")

                val billsWithCategory = withContext(Dispatchers.IO) {
                    billDao.getDailyBills(startTime, endTime, currentLedgerId)
                }
                Log.d("FlowMode", "Loaded ${billsWithCategory.size} bills for month $currentMonth")

                // 按日期分组处理数据
                val dateSections = billsWithCategory
                    .groupBy { billWithCategory ->
                        // 将时间戳转换为日期（去掉时分秒）
                        Calendar.getInstance().apply {
                            timeInMillis = billWithCategory.bill.date
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.time
                    }
                    .map { (date, billsInDay) ->
                        // 计算当日收支
                        val dayExpense = billsInDay
                            .filter { it.bill.type == Bill.TYPE_EXPENSE }
                            .sumOf { it.bill.amount }
                        val dayIncome = billsInDay
                            .filter { it.bill.type == Bill.TYPE_INCOME }
                            .sumOf { it.bill.amount }
                        val balance = dayIncome - dayExpense

                        DateSection(
                            date = date,
                            bills = billsInDay,
                            balance = balance
                        )
                    }
                    .sortedByDescending { it.date }

                // 处理数据和更新UI
                withContext(Dispatchers.Main) {
                    dateSectionAdapter.submitList(dateSections)
                    Log.d("FlowMode", "UI updated for month $currentMonth")
                }
            } catch (e: Exception) {
                Log.e("FlowMode", "Error loading data", e)
                e.printStackTrace()
            }
        }
    }

    override fun initBinding(): FragmentDetailsListBinding {
        return FragmentDetailsListBinding.inflate(layoutInflater)
    }

    override fun onResume() {
        super.onResume()
        if (isViewCreated) {
            loadBillData()
        }
    }
} 