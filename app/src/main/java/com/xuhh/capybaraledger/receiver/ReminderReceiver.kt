package com.xuhh.capybaraledger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xuhh.capybaraledger.data.model.ReminderNotifyType
import com.xuhh.capybaraledger.utils.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1)
        val reminderName = intent.getStringExtra("reminder_name") ?: "记账提醒"
        val notifyType = intent.getStringExtra("notify_type")?.let {
            ReminderNotifyType.valueOf(it)
        } ?: ReminderNotifyType.NOTIFICATION

        NotificationHelper.showReminder(
            context = context,
            title = reminderName,
            notifyType = notifyType,
            id = reminderId.toInt()
        )
    }
} 