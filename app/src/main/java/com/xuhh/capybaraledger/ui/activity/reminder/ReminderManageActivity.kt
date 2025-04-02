package com.xuhh.capybaraledger.ui.activity.reminder

import com.xuhh.capybaraledger.databinding.ActivityReminderManageBinding
import com.xuhh.capybaraledger.ui.base.BaseActivity

class ReminderManageActivity : BaseActivity<ActivityReminderManageBinding>() {

    override fun initBinding(): ActivityReminderManageBinding {
        return ActivityReminderManageBinding.inflate(layoutInflater)
    }

    override fun initView() {
        setupToolbar()
    }

    private fun setupToolbar() {
        mBinding.btnBack.setOnClickListener {
            finish()
        }

        mBinding.btnAdd.setOnClickListener {
            // TODO: 显示添加提醒对话框
        }
    }
} 