package com.xuhh.capybaraledger.ui.activity.reminder

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.databinding.FragmentReminderListBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.viewmodel.ReminderViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReminderListFragment : BaseFragment<FragmentReminderListBinding>() {
    private val viewModel: ReminderViewModel by activityViewModels { 
        ReminderViewModel.Factory((requireActivity().application as App).reminderRepository)
    }

    override fun initBinding(): FragmentReminderListBinding {
        return FragmentReminderListBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeReminders()
    }

    private fun setupViews() {
        mBinding.btnAdd.setOnClickListener {
            (activity as? ReminderManagerActivity)?.navigateToAddReminder()
        }
    }

    private fun observeReminders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reminders.collectLatest { reminders ->
                // 更新UI显示闹钟列表
                if (reminders.isEmpty()) {
                    mBinding.tvEmpty.visibility = View.VISIBLE
                    mBinding.llReminders.visibility = View.GONE
                } else {
                    mBinding.tvEmpty.visibility = View.GONE
                    mBinding.llReminders.visibility = View.VISIBLE
                    mBinding.llReminders.removeAllViews()
                    // 添加闹钟项视图
                    reminders.forEach { reminder ->
                        // TODO: 添加闹钟项视图
                    }
                }
            }
        }
    }
} 