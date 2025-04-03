package com.xuhh.capybaraledger.ui.activity.reminder

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
    
    private val adapter = ReminderListAdapter()

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
            adapter = this@ReminderListFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }

        adapter.onSwitchChanged = { reminder, isEnabled ->
            viewModel.updateReminderEnabled(reminder.id, isEnabled)
        }
    }

    private fun observeReminders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reminders.collectLatest { reminders ->
                adapter.submitList(reminders)
                mBinding.tvEmpty.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
} 