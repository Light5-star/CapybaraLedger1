package com.xuhh.capybaraledger.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.xuhh.capybaraledger.data.model.Reminder
import com.xuhh.capybaraledger.receiver.ReminderReceiver
import java.util.Calendar

object AlarmHelper {
    private const val ACTION_REMINDER = "com.xuhh.capybaraledger.action.REMINDER"

    fun scheduleReminder(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_name", reminder.name)
            putExtra("notify_type", reminder.notifyType.name)
            data = createUniqueUri(reminder.id)
            addCategory(reminder.id.toString())  // 添加额外的分类标识
        }

        val requestCode = generateUniqueRequestCode(reminder.id)
        
        // 先取消可能存在的旧闹钟
        cancelReminder(context, reminder.id)
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (hours, minutes) = reminder.time.split(":").map { it.toInt() }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val info = AlarmManager.AlarmClockInfo(calendar.timeInMillis, null)
        alarmManager.setAlarmClock(info, pendingIntent)
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra("reminder_id", reminderId)
            data = createUniqueUri(reminderId)
            addCategory(reminderId.toString())  // 添加相同的分类标识
        }
        
        val requestCode = generateUniqueRequestCode(reminderId)
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun generateUniqueRequestCode(reminderId: Long): Int {
        // 使用更复杂的哈希算法
        return (reminderId.toString() + System.currentTimeMillis()).hashCode()
    }

    private fun createUniqueUri(reminderId: Long): android.net.Uri {
        return android.net.Uri.parse("reminder://${reminderId}/${System.currentTimeMillis()}")
    }
} 