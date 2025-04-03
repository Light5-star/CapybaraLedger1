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
        ReminderViewModel.Factory((requireActivity().application as App).reminderRepository)
    }
    
    private val reminderAdapter = ReminderListAdapter()
    private val PERMISSION_REQUEST_CODE = 123

    override fun initBinding(): FragmentReminderListBinding {
        return FragmentReminderListBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeReminders()
        setupTestButton()
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

    private fun setupTestButton() {
        // 创建通知渠道
        NotificationHelper.createNotificationChannel(requireContext())
        
        // 设置测试按钮点击事件
        mBinding.btnTestNotification.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!NotificationHelper.hasNotificationPermission(requireContext())) {
                    // 申请权限
                    requestPermissions(
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        PERMISSION_REQUEST_CODE
                    )
                } else {
                    showTestNotification()
                }
            } else {
                showTestNotification()
            }
        }
    }

    private fun showTestNotification() {
        NotificationHelper.showReminder(
            context = requireContext(),
            notifyType = ReminderNotifyType.RING_VIBRATE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && 
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showTestNotification()
                } else {
                    Toast.makeText(requireContext(), 
                        "需要通知权限才能发送提醒", 
                        Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
} 