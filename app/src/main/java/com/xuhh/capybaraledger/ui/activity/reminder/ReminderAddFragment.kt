package com.xuhh.capybaraledger.ui.activity.reminder

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.application.App
import com.xuhh.capybaraledger.data.model.Reminder
import com.xuhh.capybaraledger.data.model.ReminderNotifyType
import com.xuhh.capybaraledger.data.model.ReminderRepeatType
import com.xuhh.capybaraledger.databinding.FragmentReminderAddBinding
import com.xuhh.capybaraledger.ui.base.BaseFragment
import com.xuhh.capybaraledger.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch

class ReminderAddFragment : BaseFragment<FragmentReminderAddBinding>() {
    private val viewModel: ReminderViewModel by activityViewModels { 
        ReminderViewModel.Factory(
            (requireActivity().application as App).reminderRepository,
            requireContext()
        )
    }
    
    private var currentRepeatType = ReminderRepeatType.ONCE
    private var customDays: List<Int>? = null
    private var isOddWeek: Boolean = true  // 单双休时是否为单周
    private var currentNotifyType = ReminderNotifyType.NOTIFICATION
    private var reminderName: String = ""
    private var editingReminder: Reminder? = null  // 当前正在编辑的提醒

    override fun initBinding(): FragmentReminderAddBinding {
        return FragmentReminderAddBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        
        // 获取要编辑的提醒
        arguments?.getLong("reminder_id", -1)?.let { id ->
            if (id != -1L) {
                loadReminder(id)
            }
        }
    }

    private fun setupViews() {
        // 重复选择点击事件
        mBinding.llRepeat.setOnClickListener {
            showRepeatTypeDialog()
        }

        // 提醒方式点击事件
        mBinding.llNotify.root.setOnClickListener {  // 修改这里，因为是include布局
            showNotifyTypeDialog()
        }

        // 提醒名称点击事件
        mBinding.llName.setOnClickListener {
            showNameDialog()
        }

        updateNotifyTypeText()
    }

    private fun showRepeatTypeDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_repeat_type, null)
        dialog.setContentView(dialogView)

        // 创建临时变量存储选择状态
        var tempRepeatType = currentRepeatType
        var tempCustomDays = customDays?.toList()
        var tempIsOddWeek = isOddWeek

        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.rg_repeat_type)
        val weekdaySelector = dialogView.findViewById<ViewGroup>(R.id.weekday_selector)
        val alternateRestSelector = dialogView.findViewById<ViewGroup>(R.id.alternate_rest_selector)
        val weekdayViews = listOf(
            weekdaySelector.findViewById<TextView>(R.id.tv_sunday),
            weekdaySelector.findViewById<TextView>(R.id.tv_monday),
            weekdaySelector.findViewById<TextView>(R.id.tv_tuesday),
            weekdaySelector.findViewById<TextView>(R.id.tv_wednesday),
            weekdaySelector.findViewById<TextView>(R.id.tv_thursday),
            weekdaySelector.findViewById<TextView>(R.id.tv_friday),
            weekdaySelector.findViewById<TextView>(R.id.tv_saturday)
        )

        // 设置单双休选择器
        val oddWeekView = alternateRestSelector.findViewById<TextView>(R.id.tv_odd_week)
        val evenWeekView = alternateRestSelector.findViewById<TextView>(R.id.tv_even_week)

        // 设置当前选中状态
        when (tempRepeatType) {
            ReminderRepeatType.CUSTOM -> {
                radioGroup.check(R.id.rb_custom)
                weekdaySelector.visibility = View.VISIBLE
                alternateRestSelector.visibility = View.GONE
                tempCustomDays?.forEach { day ->
                    weekdayViews[day % 7].isSelected = true
                }
            }
            ReminderRepeatType.ALTERNATE_REST -> {
                radioGroup.check(R.id.rb_alternate_rest)
                weekdaySelector.visibility = View.GONE
                alternateRestSelector.visibility = View.VISIBLE
                oddWeekView.isSelected = tempIsOddWeek
                evenWeekView.isSelected = !tempIsOddWeek
            }
            else -> {
                radioGroup.check(when (tempRepeatType) {
                    ReminderRepeatType.ONCE -> R.id.rb_once
                    ReminderRepeatType.DAILY -> R.id.rb_daily
                    ReminderRepeatType.DOUBLE_REST -> R.id.rb_double_rest
                    ReminderRepeatType.SINGLE_REST -> R.id.rb_single_rest
                    else -> R.id.rb_once
                })
                weekdaySelector.visibility = View.GONE
                alternateRestSelector.visibility = View.GONE
            }
        }

        // 设置单双休选择点击事件
        oddWeekView.setOnClickListener {
            tempIsOddWeek = true
            oddWeekView.isSelected = true
            evenWeekView.isSelected = false
        }

        evenWeekView.setOnClickListener {
            tempIsOddWeek = false
            oddWeekView.isSelected = false
            evenWeekView.isSelected = true
        }

        // 设置星期选择点击事件
        weekdayViews.forEachIndexed { index, view ->
            view.setOnClickListener {
                view.isSelected = !view.isSelected
                tempCustomDays = weekdayViews.mapIndexedNotNull { i, v ->
                    if (v.isSelected) (i + 1) % 7 else null
                }
            }
        }

        // 设置底部按钮点击事件
        dialogView.findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btn_confirm).setOnClickListener {
            // 确认时才更新实际的值
            currentRepeatType = tempRepeatType
            customDays = tempCustomDays
            isOddWeek = tempIsOddWeek
            updateRepeatTypeText()
            dialog.dismiss()
        }

        // 选择监听
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_custom -> {
                    weekdaySelector.visibility = View.VISIBLE
                    alternateRestSelector.visibility = View.GONE
                    if (tempCustomDays == null) {
                        tempCustomDays = listOf(1)
                        weekdayViews[1].isSelected = true
                    }
                    tempRepeatType = ReminderRepeatType.CUSTOM
                }
                R.id.rb_alternate_rest -> {
                    weekdaySelector.visibility = View.GONE
                    alternateRestSelector.visibility = View.VISIBLE
                    tempRepeatType = ReminderRepeatType.ALTERNATE_REST
                    oddWeekView.isSelected = tempIsOddWeek
                    evenWeekView.isSelected = !tempIsOddWeek
                }
                else -> {
                    weekdaySelector.visibility = View.GONE
                    alternateRestSelector.visibility = View.GONE
                    tempRepeatType = when (checkedId) {
                        R.id.rb_once -> ReminderRepeatType.ONCE
                        R.id.rb_daily -> ReminderRepeatType.DAILY
                        R.id.rb_double_rest -> ReminderRepeatType.DOUBLE_REST
                        R.id.rb_single_rest -> ReminderRepeatType.SINGLE_REST
                        else -> ReminderRepeatType.ONCE
                    }
                }
            }
        }

        dialog.show()
    }

    private fun updateRepeatTypeText() {
        val text = when (currentRepeatType) {
            ReminderRepeatType.ONCE -> "仅一次"
            ReminderRepeatType.DAILY -> "每天"
            ReminderRepeatType.DOUBLE_REST -> "双休制"
            ReminderRepeatType.SINGLE_REST -> "单休制"
            ReminderRepeatType.ALTERNATE_REST -> {
                if (isOddWeek) "单双休(本周为单周)" else "单双休(本周为双周)"
            }
            ReminderRepeatType.CUSTOM -> {
                val days = customDays ?: return
                // 检查是否选择了所有天数
                if (days.size == 7) {
                    "每天"
                } else {
                    val dayNames = days.map { 
                        when (it) {
                            1 -> "周一"
                            2 -> "周二"
                            3 -> "周三"
                            4 -> "周四"
                            5 -> "周五"
                            6 -> "周六"
                            7, 0 -> "周日"  // 处理 0 和 7 都表示周日的情况
                            else -> ""
                        }
                    }
                    "每${dayNames.joinToString("、")}"
                }
            }
        }
        mBinding.tvRepeatValue.text = text
    }

    private fun showNotifyTypeDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_notify_type, null)
        dialog.setContentView(dialogView)

        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.rg_notify_type)
        var tempNotifyType = currentNotifyType

        // 设置当前选中状态
        radioGroup.check(R.id.rb_notification)  // 默认选中通知

        // 选择监听
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            tempNotifyType = ReminderNotifyType.NOTIFICATION  // 始终使用通知
        }

        // 取消按钮
        dialogView.findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        // 确认按钮
        dialogView.findViewById<TextView>(R.id.btn_confirm).setOnClickListener {
            currentNotifyType = tempNotifyType
            updateNotifyTypeText()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateNotifyTypeText() {
        mBinding.llNotify.tvNotifyValue.text = "通知"  // 始终显示通知
    }

    private fun showNameDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_reminder_name, null)
        dialog.setContentView(dialogView)

        val etName = dialogView.findViewById<EditText>(R.id.et_name)
        etName.setText(reminderName)
        etName.setSelection(reminderName.length)

        dialogView.findViewById<TextView>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btn_confirm).setOnClickListener {
            val name = etName.text.toString().trim()
        if (name.isBlank()) {
                Toast.makeText(requireContext(), "请输入提醒名称", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            reminderName = name
            mBinding.tvNameValue.text = name
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadReminderData(reminder: Reminder) {
        // 加载提醒数据到界面
        reminderName = reminder.name
        mBinding.tvNameValue.text = reminderName

        // 设置时间
        val (hours, minutes) = reminder.time.split(":").map { it.toInt() }
        mBinding.timePicker.hour = hours
        mBinding.timePicker.minute = minutes

        // 设置重复类型
        currentRepeatType = reminder.repeatType
        customDays = reminder.customDays
        isOddWeek = reminder.isOddWeek ?: true
        updateRepeatTypeText()

        // 设置提醒方式
        currentNotifyType = reminder.notifyType
        updateNotifyTypeText()
    }

    fun saveReminder(): Boolean {
        if (reminderName.isBlank()) {
            Toast.makeText(requireContext(), "请输入提醒名称", Toast.LENGTH_SHORT).show()
            return false
        }

        // 获取时间
        val hour = mBinding.timePicker.hour
        val minute = mBinding.timePicker.minute
        val time = String.format("%02d:%02d", hour, minute)

        if (editingReminder != null) {
            // 更新提醒
            viewModel.updateReminder(
                editingReminder!!.copy(
                    name = reminderName,
                    time = time,
                    repeatType = currentRepeatType,
                    customDays = if (currentRepeatType == ReminderRepeatType.CUSTOM) customDays else null,
                    isOddWeek = if (currentRepeatType == ReminderRepeatType.ALTERNATE_REST) isOddWeek else null,
                    notifyType = currentNotifyType
                )
            )
        } else {
            // 创建新提醒
            viewModel.createReminder(
                name = reminderName,
                time = time,
                repeatType = currentRepeatType,
                customDays = if (currentRepeatType == ReminderRepeatType.CUSTOM) customDays else null,
                isOddWeek = if (currentRepeatType == ReminderRepeatType.ALTERNATE_REST) isOddWeek else null,
                notifyType = currentNotifyType
            )
        }

        // 返回列表页面
        (activity as? ReminderManagerActivity)?.navigateToList()
        return true
    }

    fun loadReminder(reminderId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            editingReminder = viewModel.getReminder(reminderId)
            editingReminder?.let { reminder ->
                loadReminderData(reminder)
            }
        }
    }

    companion object {
        fun newInstance(reminderId: Long? = null): ReminderAddFragment {
            return ReminderAddFragment().apply {
                arguments = Bundle().apply {
                    reminderId?.let { putLong("reminder_id", it) }
                }
            }
        }
    }
} 