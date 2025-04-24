package com.xuhh.capybaraledger.ui.fragment.statistics

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.data.*
import com.xuhh.capybaraledger.databinding.FragmentStatisticsAnalysisBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.util.PredictionModel
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import com.xuhh.capybaraledger.R
import kotlinx.coroutines.launch
import java.util.*

class StatisticsAnalysisFragment : BaseFragment<FragmentStatisticsAnalysisBinding>() {
    private val mViewModel: BillViewModel by activityViewModels {
        val app = requireActivity().application as App
        ViewModelFactory(app.ledgerRepository, app.billRepository)
    }
    private val predictionModel = PredictionModel()
    private var isViewCreated = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPredictionChart()
        isViewCreated = true
        loadData()
    }

    override fun initBinding(): FragmentStatisticsAnalysisBinding {
        return FragmentStatisticsAnalysisBinding.inflate(layoutInflater)
    }

    private fun setupPredictionChart() {
        mBinding.predictionChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return (value + 1).toInt().toString() + "日"
                    }
                }
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "¥${value.toInt()}"
                    }
                }
            }

            axisRight.isEnabled = false

            legend.apply {
                form = Legend.LegendForm.LINE
                textSize = 12f
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
            
            setNoDataText("加载中...")
            setNoDataTextColor(Color.BLACK)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadData() {
        if (!isViewCreated) return
        
        mBinding.predictionChart.clear()
        mBinding.predictionChart.setNoDataText("加载中...")
        mBinding.predictionChart.invalidate()
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 从BillViewModel获取当前账本ID和月份范围
                val ledgerId = mViewModel.currentLedger.value?.id ?: return@launch
                val (startTime, endTime) = mViewModel.getCurrentMonthRange()
                
                // 获取历史账单数据
                val bills = mViewModel.getBillsWithCategoryByTimeRange(ledgerId, startTime, endTime)
                
                if (bills.isEmpty()) {
                    mBinding.predictionChart.setNoDataText("本月暂无数据")
                    mBinding.predictionChart.invalidate()
                    mBinding.tvPredictionSummary.text = "本月暂无数据，无法进行预测分析"
                    return@launch
                }
                
                // 对账单按日期分组并计算每日金额
                val dailyAmounts = mutableListOf<Float>()
                val calendar = Calendar.getInstance()
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                
                // 创建每日支出金额的映射
                val dailyExpenseMap = mutableMapOf<Int, Float>()
                
                // 统计每日支出
                bills.forEach { bill ->
                    calendar.timeInMillis = bill.bill.date
                    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                    
                    if (bill.bill.type == 0) { // 支出类型
                        val currentAmount = dailyExpenseMap[dayOfMonth] ?: 0f
                        dailyExpenseMap[dayOfMonth] = currentAmount + bill.bill.amount.toFloat()
                    }
                }
                
                // 填充每日数据（只用当前已过天数的数据）
                for (day in 1..currentDay) {
                    dailyAmounts.add(dailyExpenseMap[day] ?: 0f)
                }
                
                if (dailyAmounts.all { it == 0f }) {
                    mBinding.predictionChart.setNoDataText("本月暂无支出数据")
                    mBinding.predictionChart.invalidate()
                    mBinding.tvPredictionSummary.text = "本月暂无支出数据，无法进行预测分析"
                    return@launch
                }
                
                // 预测未来天数（剩余天数 + 下个月7天）
                val calendar2 = Calendar.getInstance()
                val daysInMonth = calendar2.getActualMaximum(Calendar.DAY_OF_MONTH)
                val daysToPredict = (daysInMonth - currentDay) + 7
                
                // 预测未来数据
                val predictions = predictionModel.predict(dailyAmounts, daysToPredict)
                
                // 创建连接的数据集，确保历史数据和预测数据线连起来
                // 历史数据
                val historicalEntries = dailyAmounts.mapIndexed { index, value ->
                    Entry(index.toFloat(), value)
                }
                
                // 预测数据 - 为确保连续性，从最后一个历史数据点开始
                val predictionEntries = mutableListOf<Entry>()
                if (dailyAmounts.isNotEmpty()) {
                    // 添加最后一个历史点作为预测的第一个点
                    predictionEntries.add(Entry((dailyAmounts.size - 1).toFloat(), dailyAmounts.last()))
                }
                
                // 添加预测点
                predictions.forEachIndexed { index, value ->
                    predictionEntries.add(Entry((dailyAmounts.size + index).toFloat(), value))
                }
                
                // 创建历史数据集
                val historicalDataSet = LineDataSet(historicalEntries, "当前支出").apply {
                    val historicalColor = ContextCompat.getColor(requireContext(), R.color.chart_history)
                    color = historicalColor
                    setCircleColor(historicalColor)
                    lineWidth = 2f
                    circleRadius = 3f
                    setDrawCircleHole(false)  // 设置为实心点
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER  // 添加平滑曲线
                }
                
                // 创建预测数据集
                val predictionDataSet = LineDataSet(predictionEntries, "预测支出").apply {
                    val predictionColor = ContextCompat.getColor(requireContext(), R.color.chart_prediction)
                    color = predictionColor
                    setCircleColor(predictionColor)
                    lineWidth = 2.5f  // 稍微粗一点的线，更醒目
                    circleRadius = 3f
                    setDrawCircleHole(false)  // 设置为实心点
                    setDrawValues(false)
                    setDrawFilled(true)
                    fillColor = ContextCompat.getColor(requireContext(), R.color.chart_prediction_fill)
                    fillAlpha = 50  // 适当增加透明度
                    mode = LineDataSet.Mode.CUBIC_BEZIER  // 添加平滑曲线
                    enableDashedLine(10f, 5f, 0f)  // 添加虚线
                    
                    // 第一个点是从历史数据连接过来的，不显示圆点
                    if (predictionEntries.size > 1) {
                        val circleColors = ArrayList<Int>(predictionEntries.size)
                        for (i in predictionEntries.indices) {
                            if (i == 0) {
                                // 第一个点是重复的，设为透明
                                circleColors.add(Color.TRANSPARENT)
                            } else {
                                circleColors.add(predictionColor)
                            }
                        }
                        setCircleColors(circleColors)
                    }
                }
                
                // 添加水平参考线 - 平均支出
                val avgValue = dailyAmounts.average().toFloat()
                val horizontalLineEntries = listOf(
                    Entry(0f, avgValue),
                    Entry((dailyAmounts.size + predictions.size - 1).toFloat(), avgValue)
                )
                val horizontalLineDataSet = LineDataSet(horizontalLineEntries, "平均支出").apply {
                    color = ContextCompat.getColor(requireContext(), R.color.text_secondary)
                    lineWidth = 1f
                    setDrawCircles(false)
                    setDrawValues(false)
                    enableDashedLine(5f, 5f, 0f)  // 添加虚线
                }
                
                // 添加垂直分界线
                val lastDay = dailyAmounts.size - 1
                val verticalLineEntries = listOf(
                    Entry(lastDay.toFloat(), 0f),
                    Entry(lastDay.toFloat(), dailyAmounts.maxOrNull()?.times(1.2f) ?: 100f)
                )
                val verticalLineDataSet = LineDataSet(verticalLineEntries, "当前日期").apply {
                    color = ContextCompat.getColor(requireContext(), R.color.text_hint)
                    lineWidth = 1f
                    setDrawCircles(false)
                    setDrawValues(false)
                    enableDashedLine(5f, 5f, 0f)  // 添加虚线
                }
                
                // 设置图表数据
                val data = LineData(historicalDataSet, predictionDataSet, horizontalLineDataSet, verticalLineDataSet)
                mBinding.predictionChart.data = data
                mBinding.predictionChart.animateX(1000)
                mBinding.predictionChart.invalidate()
                
                // 更新预测总结
                updatePredictionSummary(predictions, dailyAmounts)
            } catch (e: Exception) {
                mBinding.predictionChart.setNoDataText("加载失败: ${e.message}")
                mBinding.predictionChart.invalidate()
                mBinding.tvPredictionSummary.text = "数据加载失败，请稍后再试"
            }
        }
    }

    private fun updatePredictionSummary(predictions: List<Float>, historicalData: List<Float>) {
        try {
            // 计算当月总支出
            val currentTotal = historicalData.sum()
            
            // 计算月底预计总支出（当前 + 预测剩余天数）
            val remainingDays = predictions.take(predictions.size - 7)  // 除去下月7天
            val predictedMonthTotal = currentTotal + remainingDays.sum()
            
            // 计算下月前7天预计支出
            val nextMonthPrediction = predictions.takeLast(7).sum()
            
            // 判断支出趋势
            val trend = when {
                predictions.last() > predictions.first() * 1.1 -> "上升"
                predictions.last() < predictions.first() * 0.9 -> "下降"
                else -> "平稳"
            }
            
            // 计算变化幅度（使用近5天的平均值与未来5天的平均值比较）
            val recentAvg = historicalData.takeLast(minOf(5, historicalData.size)).average()
            val futureAvg = predictions.take(5).average()
            
            val change = if (recentAvg > 0) {
                ((futureAvg - recentAvg) / recentAvg * 100).toInt()
            } else {
                0
            }
            
            // 生成分析文本
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            val nextMonth = if (currentMonth == 12) 1 else currentMonth + 1
            
            val summaryText = buildString {
                append("根据历史数据分析，近期支出趋势预计")
                append(trend)
                if (change != 0) {
                    append("，变化幅度约")
                    append(if (change >= 0) "+" else "")
                    append(change)
                    append("%")
                }
                append("。\n\n")
                
                append("本月累计支出：¥")
                append(String.format("%.2f", currentTotal))
                append("\n")
                
                append("预计本月总支出：¥")
                append(String.format("%.2f", predictedMonthTotal))
                append("\n")
                
                append("预计${nextMonth}月前7天支出：¥")
                append(String.format("%.2f", nextMonthPrediction))
            }
            
            mBinding.tvPredictionSummary.text = summaryText
        } catch (e: Exception) {
            mBinding.tvPredictionSummary.text = "数据分析异常，请稍后再试"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        if (isViewCreated) {
            loadData()
        }
    }
} 