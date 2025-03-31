package com.xuhh.capybaraledger.ui.fragment.detail

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.FragmentDetailsBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.ui.view.ledgerselect.LedgerSelectorDialog
import com.xuhh.capybaraledger.viewmodel.DetailViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetailFragment: BaseFragment<FragmentDetailsBinding>() {
    private var currentMode = Mode.FLOW
    private var currentLedger: Ledger? = null
    private val mDetailViewModel: DetailViewModel by viewModels()

    override fun initBinding(): FragmentDetailsBinding {
        return FragmentDetailsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        updateModeUI()
        setupModeSwitch()
        setupViewPager()
        setupLedgerSelector()
        setupMonthSelector()
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
        if (currentMode == Mode.FLOW) {
            FlowModeFragment().loadBillData()
        } else {
            CalendarModeFragment().loadCalendarData()
        }
    }

    private fun setupModeSwitch() {
        mBinding.btnListMode.setOnClickListener {
            if (currentMode != Mode.FLOW) {
                currentMode = Mode.FLOW
                updateModeUI()
                mBinding.viewPager.currentItem = 0
            }
        }

        mBinding.btnCalendarMode.setOnClickListener {
            if (currentMode != Mode.CALENDAR) {
                currentMode = Mode.CALENDAR
                updateModeUI()
                mBinding.viewPager.currentItem = 1
            }
        }
    }

    private fun setupViewPager() {
        mBinding.viewPager.apply {
            adapter = DetailPagerAdapter(requireActivity())
            isUserInputEnabled = false // 禁用滑动切换
        }
    }

    private fun updateModeUI() {
        // 更新流水模式按钮样式
        mBinding.btnListMode.apply {
            setTextColor(
                if (currentMode == Mode.FLOW) {
                    ContextCompat.getColor(requireContext(), R.color.accent)
                } else {
                    ContextCompat.getColor(requireContext(), R.color.text_primary)
                }
            )
            setBackgroundResource(
                if (currentMode == Mode.FLOW) {
                    R.drawable.bg_mode_switch_item_selected
                } else {
                    R.drawable.bg_mode_switch_item
                }
            )
        }

        // 更新日历模式按钮样式
        mBinding.btnCalendarMode.apply {
            setTextColor(
                if (currentMode == Mode.CALENDAR) {
                    ContextCompat.getColor(requireContext(), R.color.accent)
                } else {
                    ContextCompat.getColor(requireContext(), R.color.text_primary)
                }
            )
            setBackgroundResource(
                if (currentMode == Mode.CALENDAR) {
                    R.drawable.bg_mode_switch_item_selected
                } else {
                    R.drawable.bg_mode_switch_item
                }
            )
        }
    }

    private fun setupMonthSelector() {
        // 观察 Calendar 变化
        mDetailViewModel.calendar.observe(viewLifecycleOwner) { calendar ->
            updateMonthDisplay(calendar)
            loadBillData()
        }

        mBinding.btnPrevMonth.setOnClickListener {
            mDetailViewModel.backMonth()
        }

        mBinding.btnNextMonth.setOnClickListener {
            mDetailViewModel.nextMonth()
        }
    }

    // 更新日期显示方法
    private fun updateMonthDisplay(calendar: Calendar) {
        val sdf = SimpleDateFormat("yyyy年M月", Locale.getDefault())
        mBinding.tvMonth.text = sdf.format(calendar.time)
    }

    private enum class Mode {
        FLOW, CALENDAR
    }
}