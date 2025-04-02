package com.xuhh.capybaraledger.ui.fragment.profile

import android.content.Intent
import com.xuhh.capybaraledger.databinding.FragmentProfileBinding
import com.xuhh.capybaraledger.dialog.AboutDialog
import com.xuhh.capybaraledger.dialog.FeedbackDialog
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.ui.activity.ledger.LedgerManageActivity
import com.xuhh.capybaraledger.ui.activity.reminder.ReminderManageActivity

class ProfileFragment: BaseFragment<FragmentProfileBinding>() {
    override fun initBinding(): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        
        // 设置关于我们的点击事件
        mBinding.llAbout.setOnClickListener {
            AboutDialog(requireContext()).show()
        }
        
        // 设置反馈的点击事件
        mBinding.llFeedback.setOnClickListener {
            FeedbackDialog(requireContext()).show()
        }

        mBinding.llLedger.setOnClickListener {
            startActivity(Intent(requireContext(), LedgerManageActivity::class.java))
        }

        mBinding.llReminder.setOnClickListener {
            startActivity(Intent(requireContext(), ReminderManageActivity::class.java))
        }
    }
}