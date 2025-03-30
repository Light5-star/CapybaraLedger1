package com.xuhh.capybaraledger.ui.fragment.detail

import androidx.core.content.ContextCompat
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.databinding.FragmentDetailsBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment

class DetailFragment: BaseFragment<FragmentDetailsBinding>() {
    private var currentMode = Mode.FLOW

    override fun initBinding(): FragmentDetailsBinding {
        return FragmentDetailsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        updateModeUI()
        setupModeSwitch()
        setupViewPager()
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

    private enum class Mode {
        FLOW, CALENDAR
    }
}