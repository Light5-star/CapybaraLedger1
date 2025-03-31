package com.xuhh.capybaraledger.ui.fragment.detail

import android.os.Bundle
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class CalendarModeFragment : BaseFragment<FragmentDetailsCalendarBinding>() {
    private val detailViewModel: DetailViewModel by activityViewModels()
    private lateinit var calendarAdapter: CalendarAdapter
    private var currentLedgerId: Long = 1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendar()
        loadCalendarData()
    }

    override fun initBinding(): FragmentDetailsCalendarBinding {
        return FragmentDetailsCalendarBinding.inflate(layoutInflater)
    }

    override fun onResume() {
        super.onResume()
        loadCalendarData()
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
                val bills = withContext(Dispatchers.IO) {
                    billDao.getBillsByLedgerIdAndTimeRange(currentLedgerId, startTime, endTime)
                }

                // 处理日历数据
                val calendarData = processCalendarData(bills, currentYear, currentMonth)
                
                // 在主线程更新UI
                withContext(Dispatchers.Main) {
                    calendarAdapter.submitList(calendarData)
                }
            } catch (e: Exception) {
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
}


