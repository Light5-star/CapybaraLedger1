package com.xuhh.capybaraledger.ui.activity.reminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.databinding.FragmentReminderListBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.viewmodel.ReminderViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.xuhh.capybaraledger.utils.NotificationHelper
import com.xuhh.capybaraledger.data.model.ReminderNotifyType

class ReminderListFragment : BaseFragment<FragmentReminderListBinding>() {
    private val viewModel: ReminderViewModel by activityViewModels { 
        ReminderViewModel.Factory(
            (requireActivity().application as App).reminderRepository,
            requireContext()
        )
    }
    
    private val reminderAdapter = ReminderListAdapter()

    override fun initBinding(): FragmentReminderListBinding {
        return FragmentReminderListBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeReminders()
    }

    private fun setupRecyclerView() {
        mBinding.recyclerView.apply {
            adapter = reminderAdapter
            layoutManager = LinearLayoutManager(context)
        }

        reminderAdapter.onSwitchChanged = { reminder, isEnabled ->
            viewModel.updateReminderEnabled(reminder.id, isEnabled)
        }
    }

    private fun observeReminders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reminders.collectLatest { reminders ->
                reminderAdapter.submitList(reminders)
                mBinding.tvEmpty.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
} 