package com.xuhh.capybaraledger.ui.fragment.statistics

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.databinding.FragmentStatisticsTrendBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.viewmodel.StatisticsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class StatisticsTrendFragment : BaseFragment<FragmentStatisticsTrendBinding>() {
    private val viewModel: StatisticsViewModel by activityViewModels()
    private var currentType = TYPE_EXPENSE
    private var isViewCreated = false

    companion object {
        private const val TYPE_EXPENSE = 0
        private const val TYPE_INCOME = 1
        private const val TYPE_BALANCE = 2
    }

    override fun initBinding(): FragmentStatisticsTrendBinding {
        return FragmentStatisticsTrendBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewCreated = true
        setupChart()
        setupTypeButtons()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.calendar.observe(viewLifecycleOwner) { calendar ->
            if (isViewCreated && isResumed) {
                loadData()
            }
        }
    }

    private fun setupTypeButtons() {
        mBinding.btnExpense.setOnClickListener {
            currentType = TYPE_EXPENSE
            updateTypeButtonsState()
            loadData()
        }
        mBinding.btnIncome.setOnClickListener {
            currentType = TYPE_INCOME
            updateTypeButtonsState()
            loadData()
        }
        mBinding.btnBalance.setOnClickListener {
            currentType = TYPE_BALANCE
            updateTypeButtonsState()
            loadData()
        }
        updateTypeButtonsState()
    }

    private fun updateTypeButtonsState() {
        mBinding.btnExpense.isSelected = currentType == TYPE_EXPENSE
        mBinding.btnIncome.isSelected = currentType == TYPE_INCOME
        mBinding.btnBalance.isSelected = currentType == TYPE_BALANCE
    }

    private fun setupChart() {
        mBinding.lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}日"
                    }
                }
            }

            axisLeft.apply {
                setDrawGridLines(true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%.2f", value)
                    }
                }
            }

            axisRight.isEnabled = false
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getInstance(requireContext())
                val billDao = database.billDao()
                val (startTime, endTime) = viewModel.getCurrentMonthRange()
                val ledgerId = viewModel.currentLedgerId.value ?: 1L

                val bills = withContext(Dispatchers.IO) {
                    billDao.getBillsByLedgerIdAndTimeRange(ledgerId, startTime, endTime)
                }

                // 按日期分组统计数据
                val dailyData = bills.groupBy { bill ->
                    Calendar.getInstance().apply {
                        timeInMillis = bill.date
                    }.get(Calendar.DAY_OF_MONTH)
                }.mapValues { (_, dayBills) ->
                    val expense = dayBills.filter { it.type == Bill.TYPE_EXPENSE }.sumOf { it.amount }
                    val income = dayBills.filter { it.type == Bill.TYPE_INCOME }.sumOf { it.amount }
                    Triple(expense, income, income - expense)
                }

                // 创建图表数据
                val entries = (1..31).map { day ->
                    val (expense, income, balance) = dailyData[day] ?: Triple(0.0, 0.0, 0.0)
                    val value = when (currentType) {
                        TYPE_EXPENSE -> expense
                        TYPE_INCOME -> income
                        else -> balance
                    }
                    Entry(day.toFloat(), value.toFloat())
                }

                val dataSet = LineDataSet(entries, "").apply {
                    color = when (currentType) {
                        TYPE_EXPENSE -> Color.RED
                        TYPE_INCOME -> Color.GREEN
                        else -> Color.BLUE
                    }
                    setDrawCircles(true)
                    setDrawValues(false)
                    lineWidth = 2f
                }

                withContext(Dispatchers.Main) {
                    mBinding.lineChart.data = LineData(dataSet)
                    mBinding.lineChart.invalidate()

                    // 更新月度统计
                    val monthTotal = when (currentType) {
                        TYPE_EXPENSE -> bills.filter { it.type == Bill.TYPE_EXPENSE }.sumOf { it.amount }
                        TYPE_INCOME -> bills.filter { it.type == Bill.TYPE_INCOME }.sumOf { it.amount }
                        else -> bills.filter { it.type == Bill.TYPE_INCOME }.sumOf { it.amount } -
                                bills.filter { it.type == Bill.TYPE_EXPENSE }.sumOf { it.amount }
                    }
                    mBinding.tvMonthAmount.text = String.format("%.2f", monthTotal)
                    mBinding.tvMonthAmount.setTextColor(when (currentType) {
                        TYPE_EXPENSE -> Color.RED
                        TYPE_INCOME -> Color.GREEN
                        else -> if (monthTotal >= 0) Color.GREEN else Color.RED
                    })
                }
            } catch (e: Exception) {
                Log.e("StatisticsTrend", "Error loading data", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isViewCreated) {
            loadData()
        }
    }
}