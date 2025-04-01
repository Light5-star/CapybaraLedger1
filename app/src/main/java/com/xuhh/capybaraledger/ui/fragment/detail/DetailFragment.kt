package com.xuhh.capybaraledger.ui.fragment.detail

import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.databinding.FragmentDetailsBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.ui.view.ledgerselect.LedgerSelectorDialog
import com.xuhh.capybaraledger.viewmodel.BillViewModel
import com.xuhh.capybaraledger.viewmodel.DetailViewModel
import com.xuhh.capybaraledger.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class DetailFragment: BaseFragment<FragmentDetailsBinding>() {
    private var currentMode = Mode.FLOW
    private val mDetailViewModel: DetailViewModel by viewModels()
    private lateinit var mViewModel: BillViewModel

    override fun initBinding(): FragmentDetailsBinding {
        return FragmentDetailsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        // 初始化 BillViewModel
        val app = requireActivity().application as App
        val factory = ViewModelFactory(app.ledgerRepository, app.billRepository)
        mViewModel = ViewModelProvider(this, factory)[BillViewModel::class.java]

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        updateModeUI()
        setupModeSwitch()
        setupViewPager()
        setupLedgerSelector()
        setupMonthSelector()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 观察当前账本
            mViewModel.currentLedger.collect { ledger ->
                ledger?.let {
                    mBinding.tvLedgerName.text = it.name
                    loadBillData()
                }
            }
        }
    }

    private fun setupLedgerSelector() {
        mBinding.layoutLedger.setOnClickListener {
            LedgerSelectorDialog(
                requireContext(),
                mViewModel
            ) { ledger ->
                // 选择账本的处理已经在 Dialog 中完成
            }.show()
        }
    }

    private fun loadBillData() {
        val adapter = mBinding.viewPager.adapter as? DetailPagerAdapter
        val currentFragment = adapter?.getFragment(mBinding.viewPager.currentItem)
        
        when (currentFragment) {
            is FlowModeFragment -> {
                Log.d("DetailFragment", "Loading flow mode data")
                currentFragment.loadBillData()
            }
            is CalendarModeFragment -> {
                Log.d("DetailFragment", "Loading calendar mode data")
                currentFragment.loadCalendarData()
            }
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
            adapter = DetailPagerAdapter(childFragmentManager, lifecycle)
            isUserInputEnabled = false
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentMode = if (position == 0) Mode.FLOW else Mode.CALENDAR
                    updateModeUI()
                }
            })
            // 设置初始页面
            post {
                currentItem = if (currentMode == Mode.FLOW) 0 else 1
            }
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
        mDetailViewModel.calendar.observe(viewLifecycleOwner) { calendar ->
            Log.d("DetailFragment", "Calendar changed: month=${calendar.get(Calendar.MONTH)}")
            updateMonthDisplay(calendar)
            loadBillData()
        }

        mBinding.btnPrevMonth.setOnClickListener {
            Log.d("DetailFragment", "Previous month clicked")
            mDetailViewModel.backMonth()
        }

        mBinding.btnNextMonth.setOnClickListener {
            Log.d("DetailFragment", "Next month clicked")
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