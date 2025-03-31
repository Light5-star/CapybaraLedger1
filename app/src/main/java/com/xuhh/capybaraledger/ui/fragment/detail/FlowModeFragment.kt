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

        val calendar = detailViewModel.calendar.value ?: Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        lifecycleScope.launch {
            try {
                val database = AppDatabase.getInstance(requireContext())
                val billDao = database.billDao()

                // 获取当前月份的开始和结束时间
                val startCalendar = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startTime = startCalendar.timeInMillis

                val endCalendar = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.MONTH, 1)
                    add(Calendar.MILLISECOND, -1)
                }
                val endTime = endCalendar.timeInMillis

                // 在后台线程加载账单数据
                val billsWithCategory = withContext(Dispatchers.IO) {
                    billDao.getDailyBills(startTime, endTime, currentLedgerId)
                }

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

                // 在主线程更新UI
                withContext(Dispatchers.Main) {
                    dateSectionAdapter.submitList(dateSections)
                    Log.d("FlowMode", "Bill data updated for $currentYear-$currentMonth")
                }
            } catch (e: Exception) {
                Log.e("FlowMode", "Error loading bill data", e)
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