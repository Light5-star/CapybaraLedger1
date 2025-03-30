package com.xuhh.capybaraledger.ui.fragment.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.databinding.FragmentDetailsCalendarBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CalendarModeFragment : Fragment() {
    private var _binding: FragmentDetailsCalendarBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var calendarAdapter: CalendarAdapter
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var currentLedgerId: Long = 1L
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCalendar()
        setupMonthNavigation()
        loadCalendarData()
    }

    override fun onResume() {
        super.onResume()
        loadCalendarData()
    }

    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter()
        binding.rvCalendar.apply {
            layoutManager = GridLayoutManager(context, 7)
            adapter = calendarAdapter
        }
    }

    private fun setupMonthNavigation() {
        binding.btnPrevMonth.setOnClickListener {
            if (currentMonth == Calendar.JANUARY) {
                currentMonth = Calendar.DECEMBER
                currentYear--
            } else {
                currentMonth--
            }
            updateYearMonthDisplay()
            loadCalendarData()
        }

        binding.btnNextMonth.setOnClickListener {
            if (currentMonth == Calendar.DECEMBER) {
                currentMonth = Calendar.JANUARY
                currentYear++
            } else {
                currentMonth++
            }
            updateYearMonthDisplay()
            loadCalendarData()
        }
    }

    private fun updateYearMonthDisplay() {
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        val dateFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
        binding.tvYearMonth.text = dateFormat.format(calendar.time)
    }

    fun loadCalendarData() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getInstance(requireContext())
                val billDao = database.billDao()
                
                // 获取当前月份的开始和结束时间
                val calendar = Calendar.getInstance()
                calendar.set(currentYear, currentMonth, 1)
                val startTime = calendar.timeInMillis
                
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                val endTime = calendar.timeInMillis

                // 在后台线程加载账单数据
                val bills = withContext(Dispatchers.IO) {
                    billDao.getBillsByLedgerIdAndTimeRange(currentLedgerId, startTime, endTime)
                }

                // 处理日历数据
                val calendarData = processCalendarData(bills)
                
                // 在主线程更新UI
                withContext(Dispatchers.Main) {
                    calendarAdapter.submitList(calendarData)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun processCalendarData(bills: List<Bill>): List<CalendarDay> {
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        
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
                val billCalendar = Calendar.getInstance()
                billCalendar.timeInMillis = bill.date
                billCalendar.get(Calendar.DAY_OF_MONTH) == day
            }
            
            val expense = dayBills.filter { it.type == Bill.TYPE_EXPENSE }.sumOf { it.amount }
            val income = dayBills.filter { it.type == Bill.TYPE_INCOME }.sumOf { it.amount }
            val balance = income - expense
            
            calendarDays.add(CalendarDay(day, expense, income, balance))
        }
        
        return calendarDays
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class CalendarDay(
    val day: Int = 0,
    val expense: Double = 0.0,
    val income: Double = 0.0,
    val balance: Double = 0.0
)

class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
    private var calendarDays = listOf<CalendarDay>()

    fun submitList(days: List<CalendarDay>) {
        calendarDays = days
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(calendarDays[position])
    }

    override fun getItemCount() = calendarDays.size

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        private val tvExpense: TextView = itemView.findViewById(R.id.tvExpense)
        private val tvIncome: TextView = itemView.findViewById(R.id.tvIncome)
        private val tvBalance: TextView = itemView.findViewById(R.id.tvBalance)

        fun bind(day: CalendarDay) {
            if (day.day > 0) {
                tvDay.text = day.day.toString()
                tvDay.visibility = View.VISIBLE
                
                if (day.expense > 0) {
                    tvExpense.text = "%.0f".format(day.expense)
                    tvExpense.visibility = View.VISIBLE
                } else {
                    tvExpense.visibility = View.GONE
                }
                
                if (day.income > 0) {
                    tvIncome.text = "%.0f".format(day.income)
                    tvIncome.visibility = View.VISIBLE
                } else {
                    tvIncome.visibility = View.GONE
                }
                
                if (day.balance != 0.0) {
                    tvBalance.text = "%.0f".format(day.balance)
                    tvBalance.visibility = View.VISIBLE
                } else {
                    tvBalance.visibility = View.GONE
                }
            } else {
                tvDay.visibility = View.INVISIBLE
                tvExpense.visibility = View.GONE
                tvIncome.visibility = View.GONE
                tvBalance.visibility = View.GONE
            }
        }
    }
} 