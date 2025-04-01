package com.xuhh.capybaraledger.ui.fragment.statistics

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.xuhh.capybaraledger.adapter.CategoryRankAdapter
import com.xuhh.capybaraledger.adapter.CategoryRankItem
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.databinding.FragmentStatisticsRankBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.viewmodel.StatisticsViewModel
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatisticsRankFragment : BaseFragment<FragmentStatisticsRankBinding>() {
    private val viewModel: StatisticsViewModel by activityViewModels()
    private lateinit var mViewModel: BillViewModel
    private var currentType = Bill.TYPE_EXPENSE
    private var isViewCreated = false
    private lateinit var rankAdapter: CategoryRankAdapter

    override fun initBinding(): FragmentStatisticsRankBinding {
        return FragmentStatisticsRankBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        val app = requireActivity().application as App
        val factory = ViewModelFactory(app.ledgerRepository, app.billRepository)
        mViewModel = ViewModelProvider(this, factory)[BillViewModel::class.java]

        setupPieChart()
        setupTypeRadioGroup()
        setupRecyclerView()
        setupObservers()
        
        // 初始加载数据
        loadData()
    }

    private fun setupObservers() {
        // 观察账本变化
        lifecycleScope.launch {
            mViewModel.currentLedger.collect { ledger ->
                ledger?.let {
                    loadData()
                }
            }
        }

        // 观察日期变化
        viewModel.calendar.observe(viewLifecycleOwner) { calendar ->
            if (isViewCreated && isResumed) {
                loadData()
            }
        }
    }

    private fun setupPieChart() {
        mBinding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            transparentCircleRadius = 60f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            setUsePercentValues(true)
            legend.isEnabled = true
        }
    }

    private fun setupTypeRadioGroup() {
        mBinding.rgType.setOnCheckedChangeListener { _, checkedId ->
            currentType = when (checkedId) {
                mBinding.rbExpense.id -> Bill.TYPE_EXPENSE
                mBinding.rbIncome.id -> Bill.TYPE_INCOME
                else -> Bill.TYPE_EXPENSE
            }
            loadData()
        }
    }

    private fun setupRecyclerView() {
        rankAdapter = CategoryRankAdapter()
        mBinding.rvRank.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rankAdapter
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val (startTime, endTime) = viewModel.getCurrentMonthRange()
                val ledgerId = mViewModel.currentLedger.value?.id ?: return@launch

                // 使用 BillViewModel 获取账单数据
                val billsWithCategory = mViewModel.getBillsWithCategoryByTimeRange(
                    ledgerId, startTime, endTime
                )

                // 按类别分组统计
                val categoryData = billsWithCategory
                    .filter { it.bill.type == currentType }
                    .groupBy { it.category }
                    .mapValues { (_, bills) -> bills.sumOf { it.bill.amount } }
                    .toList()
                    .sortedByDescending { (_, amount) -> amount }

                // 计算总金额
                val totalAmount = categoryData.sumOf { it.second }

                // 创建饼图数据
                val entries = categoryData.map { (category, amount) ->
                    PieEntry(amount.toFloat(), category.name)
                }

                // 创建排行榜数据
                val rankItems = categoryData.map { (category, amount) ->
                    CategoryRankItem(
                        category = category.name,
                        amount = amount,
                        percentage = (amount / totalAmount).toFloat(),
                        iconResId = getString(category.iconResId)
                    )
                }

                // 更新 UI
                withContext(Dispatchers.Main) {
                    if (entries.isEmpty()) {
                        mBinding.contentView.visibility = View.GONE
                        mBinding.llEmptyView.visibility = View.VISIBLE
                    } else {
                        mBinding.contentView.visibility = View.VISIBLE
                        mBinding.llEmptyView.visibility = View.GONE
                        
                        // 更新饼图
                        updatePieChart(entries)
                        
                        // 更新排行列表
                        rankAdapter.submitList(rankItems)
                    }
                }
            } catch (e: Exception) {
                Log.e("StatisticsRank", "Error loading data", e)
                withContext(Dispatchers.Main) {
                    mBinding.contentView.visibility = View.GONE
                    mBinding.llEmptyView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updatePieChart(entries: List<PieEntry>) {
        val colors = listOf(
            Color.rgb(255, 99, 71),
            Color.rgb(255, 165, 0),
            Color.rgb(255, 215, 0),
            Color.rgb(144, 238, 144),
            Color.rgb(135, 206, 235)
        )

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 14f
            valueTextColor = Color.WHITE
        }

        mBinding.pieChart.apply {
            data = PieData(dataSet).apply {
                setValueFormatter(PercentFormatter(mBinding.pieChart))
            }
            centerText = if (currentType == Bill.TYPE_EXPENSE) "支出分布" else "收入分布"
            invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isViewCreated) {
            loadData()
        }
    }
} 