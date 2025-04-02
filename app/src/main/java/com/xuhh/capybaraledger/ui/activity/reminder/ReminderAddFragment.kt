package com.xuhh.capybaraledger.ui.activity.reminder

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.xuhh.capybaraledger.data.model.ReminderNotifyType
import com.xuhh.capybaraledger.data.model.ReminderRepeatType
import com.xuhh.capybaraledger.databinding.FragmentReminderAddBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.viewmodel.ReminderViewModel

class ReminderAddFragment : BaseFragment<FragmentReminderAddBinding>() {
    private val viewModel: ReminderViewModel by activityViewModels()

    override fun initBinding(): FragmentReminderAddBinding {
        return FragmentReminderAddBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        // 返回按钮
        mBinding.btnBack.setOnClickListener {
            (activity as? ReminderManagerActivity)?.navigateToList()
        }

        // 保存按钮
        mBinding.btnSave.setOnClickListener {
            saveReminder()
        }

        // 设置默认选中项
        mBinding.rgRepeatType.check(mBinding.rbOnce.id)
        mBinding.rgNotifyType.check(mBinding.rbRing.id)
    }

    private fun saveReminder() {
        val name = mBinding.etName.text.toString()
        if (name.isBlank()) {
            Toast.makeText(requireContext(), "请输入提醒名称", Toast.LENGTH_SHORT).show()
            return
        }

        // 获取时间
        val hour = mBinding.timePicker.hour
        val minute = mBinding.timePicker.minute
        val time = String.format("%02d:%02d", hour, minute)

        // 获取重复类型
        val repeatType = when (mBinding.rgRepeatType.checkedRadioButtonId) {
            mBinding.rbOnce.id -> ReminderRepeatType.ONCE
            mBinding.rbDaily.id -> ReminderRepeatType.DAILY
            mBinding.rbCustom.id -> ReminderRepeatType.CUSTOM
            else -> ReminderRepeatType.ONCE
        }

        // 获取提醒方式
        val notifyType = when (mBinding.rgNotifyType.checkedRadioButtonId) {
            mBinding.rbRing.id -> ReminderNotifyType.RING
            mBinding.rbVibrate.id -> ReminderNotifyType.VIBRATE
            mBinding.rbRingVibrate.id -> ReminderNotifyType.RING_VIBRATE
            else -> ReminderNotifyType.RING
        }

        // 保存闹钟
        viewModel.createReminder(
            name = name,
            time = time,
            repeatType = repeatType,
            notifyType = notifyType
        )

        // 返回列表页面
        (activity as? ReminderManagerActivity)?.navigateToList()
    }
} 