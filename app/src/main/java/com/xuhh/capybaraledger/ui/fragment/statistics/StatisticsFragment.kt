package com.xuhh.capybaraledger.ui.fragment.statistics

import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.FragmentStatisticsBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.ui.view.ledgerselect.LedgerSelectorDialog
import com.xuhh.capybaraledger.viewmodel.StatisticsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatisticsFragment: BaseFragment<FragmentStatisticsBinding>() {
    private var currentMode = Mode.TREND
    private var currentLedger: Ledger? = null
    private val mStatisticsViewModel = StatisticsViewModel()


    override fun initBinding(): FragmentStatisticsBinding {
        return FragmentStatisticsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        updateModeUI()
        setupLedgerSelector()
        setupModeSwitch()
        setupViewPager()
        setupMonthSelector()
    }

    private fun setupViewPager() {
        mBinding.viewPager.apply {
            isUserInputEnabled = false // 禁用滑动切换
            // 添加缺失的适配器设置
            adapter = StatisticsPagerAdapter(requireActivity())
        }
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

    private fun loadBillData() {

    }

    private fun updateModeUI() {
        // 更新趋势模式按钮样式
        mBinding.btnTrendMode.apply {
            setTextColor(
                if (currentMode == Mode.TREND) {
                    ContextCompat.getColor(requireContext(), R.color.accent)
                } else {
                    ContextCompat.getColor(requireContext(), R.color.text_primary)
                }
            )
            setBackgroundResource(
                if (currentMode == Mode.TREND) {
                    R.drawable.bg_mode_switch_item_selected
                } else {
                    R.drawable.bg_mode_switch_item
                }
            )
        }

        // 更新排行模式按钮样式
        mBinding.btnRankMode.apply {
            setTextColor(
                if (currentMode == Mode.RANK) {
                    ContextCompat.getColor(requireContext(), R.color.accent)
                } else {
                    ContextCompat.getColor(requireContext(), R.color.text_primary)
                }
            )
            setBackgroundResource(
                if (currentMode == Mode.RANK) {
                    R.drawable.bg_mode_switch_item_selected
                } else {
                    R.drawable.bg_mode_switch_item
                }
            )
        }
    }

    private fun setupModeSwitch() {
        mBinding.btnTrendMode.setOnClickListener {
            if (currentMode != Mode.TREND) {
                currentMode = Mode.TREND
                updateModeUI()
                mBinding.viewPager.currentItem = 0
            }
        }

        mBinding.btnRankMode.setOnClickListener {
            if (currentMode != Mode.RANK) {
                currentMode = Mode.RANK
                updateModeUI()
                mBinding.viewPager.currentItem = 1
            }
        }
    }

    private fun setupMonthSelector() {
        // 观察 Calendar 变化
        mStatisticsViewModel.calendar.observe(viewLifecycleOwner) { calendar ->
            updateMonthDisplay(calendar)
            loadBillData()
        }

        mBinding.btnPrevMonth.setOnClickListener {
            mStatisticsViewModel.backMonth()
        }

        mBinding.btnNextMonth.setOnClickListener {
            mStatisticsViewModel.nextMonth()
        }
    }

    // 更新日期显示方法
    private fun updateMonthDisplay(calendar: Calendar) {
        val sdf = SimpleDateFormat("yyyy年M月", Locale.getDefault())
        mBinding.tvMonth.text = sdf.format(calendar.time)
    }


    private enum class Mode {
        RANK,TREND
    }

}