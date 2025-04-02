package com.xuhh.capybaraledger.ui.fragment.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.xuhh.capybaraledger.adapter.CalendarAdapter
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.databinding.FragmentDetailsCalendarBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.viewmodel.DetailViewModel
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import com.xuhh.capybaraledger.application.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class CalendarModeFragment : BaseFragment<FragmentDetailsCalendarBinding>() {
    private val detailViewModel: DetailViewModel by activityViewModels()
    private val mViewModel: BillViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.ledgerRepository, app.billRepository)
    }
    private lateinit var calendarAdapter: CalendarAdapter
    private var currentLedgerId: Long = 1L
    private var isViewCreated = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewCreated = true
        setupCalendar()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        if (isViewCreated) {
            loadCalendarData()
        }
    }

    private fun setupObservers() {
        // 观察日历变化
        detailViewModel.calendar.observe(viewLifecycleOwner) { calendar ->
            if (isResumed) {
                loadCalendarData()
            }
        }

        // 观察当前账本变化
        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.currentLedger.collect { ledger ->
                if (ledger != null && isResumed) {
                    loadCalendarData()
                }
            }
        }
    }

    override fun initBinding(): FragmentDetailsCalendarBinding {
        return FragmentDetailsCalendarBinding.inflate(layoutInflater)
    }

    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter()
        mBinding.rvCalendar.apply {
            layoutManager = GridLayoutManager(context, 7)
            adapter = calendarAdapter
        }
    }

    fun loadCalendarData() {
        if (!isAdded || isDetached) return

        lifecycleScope.launch {
            try {
                val database = AppDatabase.getInstance(requireContext())
                val billDao = database.billDao()
                val ledgerId = mViewModel.currentLedger.value?.id ?: return@launch

                // 使用 ViewModel 的时间范围
                val (startTime, endTime) = detailViewModel.getCurrentMonthRange()
                val currentMonth = detailViewModel.calendar.value?.get(Calendar.MONTH)
                Log.d("CalendarMode", "Loading data for month: $currentMonth, range: $startTime to $endTime")

                val bills = withContext(Dispatchers.IO) {
                    billDao.getBillsByLedgerIdAndTimeRange(ledgerId, startTime, endTime)
                }
                Log.d("CalendarMode", "Loaded ${bills.size} bills for month $currentMonth")

                // 处理数据和更新UI
                withContext(Dispatchers.Main) {
                    updateCalendarView(bills)
                }
            } catch (e: Exception) {
                Log.e("CalendarMode", "Error loading data", e)
                e.printStackTrace()
            }
        }
    }

    private fun processCalendarData(bills: List<Bill>, year: Int, month: Int): List<CalendarDay> {
        val calendar = Calendar.getInstance().apply {
            set(year, month, 1)
        }
        
        // 获取当月第一天是星期几
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val calendarDays = mutableListOf<CalendarDay>()
        
        // 添加上个月的空白日期
        for (i in 1 until firstDayOfWeek) {
            calendarDays.add(CalendarDay())
        }
        
        // 添加当月的日期
        for (day in 1..daysInMonth) {
            val dayBills = bills.filter { bill ->
                val billCalendar = Calendar.getInstance().apply {
                    timeInMillis = bill.date
                }
                billCalendar.get(Calendar.DAY_OF_MONTH) == day
            }
            
            val expense = dayBills.filter { it.type == Bill.TYPE_EXPENSE }.sumOf { it.amount }
            val income = dayBills.filter { it.type == Bill.TYPE_INCOME }.sumOf { it.amount }
            val balance = income - expense
            
            calendarDays.add(CalendarDay(day, expense, income, balance))
        }
        
        return calendarDays
    }

    private fun updateCalendarView(bills: List<Bill>) {
        val calendarData = processCalendarData(
            bills,
            detailViewModel.calendar.value?.get(Calendar.YEAR) ?: 0,
            detailViewModel.calendar.value?.get(Calendar.MONTH) ?: 0
        )
        calendarAdapter.submitList(calendarData)
    }
}



