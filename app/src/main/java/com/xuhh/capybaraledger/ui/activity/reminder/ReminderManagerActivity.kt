package com.xuhh.capybaraledger.ui.activity.reminder

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.databinding.ActivityReminderManageBinding
import com.xuhh.capybaraledger.ui.base.BaseActivity
import com.xuhh.capybaraledger.viewmodel.ReminderViewModel

class ReminderManagerActivity : BaseActivity<ActivityReminderManageBinding>() {
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: ReminderPagerAdapter

    private val viewModel: ReminderViewModel by viewModels { 
        ReminderViewModel.Factory((application as App).reminderRepository)
    }

    override fun initBinding(): ActivityReminderManageBinding {
        return ActivityReminderManageBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar()
        setupViewPager()
    }

    private fun setupToolbar() {
        mBinding.btnBack.setOnClickListener {
            if (mBinding.viewPager.currentItem == 1) {
                navigateToList()
            } else {
                finish()
            }
        }

        mBinding.btnAdd.setOnClickListener {
            navigateToAddReminder()
        }
    }

    private fun setupViewPager() {
        pagerAdapter = ReminderPagerAdapter(this)
        mBinding.viewPager.apply {
            adapter = pagerAdapter
            isUserInputEnabled = false  // 禁止滑动切换
        }
    }

    fun navigateToAddReminder() {
        mBinding.viewPager.currentItem = 1
        mBinding.tvTitle.text = "添加提醒"
    }

    fun navigateToList() {
        mBinding.viewPager.currentItem = 0
        mBinding.tvTitle.text = "记账提醒"
    }

    override fun onBackPressed() {
        if (mBinding.viewPager.currentItem == 1) {
            navigateToList()
        } else {
            super.onBackPressed()
        }
    }
} 