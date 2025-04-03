package com.xuhh.capybaraledger.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.xuhh.capybaraledger.MainActivity
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.model.ReminderNotifyType

object NotificationHelper {
    private const val CHANNEL_ID = "reminder_channel"
    private const val CHANNEL_NAME = "记账提醒"
    private var notificationId = 0  // 改为变量，用于生成唯一ID

    // 添加提醒文本列表
    private val reminderTitles = listOf(
        "卡皮提醒你",
        "记账小助手",
        "该记账啦",
        "今日份的记账提醒",
        "别忘记记账哦"
    )

    private val reminderContents = listOf(
        "今天花了多少呢？快来记一下吧！",
        "记录一下今天的收支，让生活更有规划～",
        "水豚在等你来记录今天的账单呢",
        "养成记账习惯，从现在开始！",
        "不要让今天的账单溜走了，快来记录一下吧",
        "点击进入，轻松记录今天的收支",
        "今天也要好好记账哦，财务自由从记账开始"
    )

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "提醒用户记账"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminder(
        context: Context,
        title: String? = null,
        content: String? = null,
        notifyType: ReminderNotifyType = ReminderNotifyType.RING,
        id: Int = generateNotificationId()  // 添加id参数，默认生成新ID
    ) {
        if (!hasNotificationPermission(context)) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.capybara)
            .setContentTitle(title ?: reminderTitles.random())
            .setContentText(content ?: reminderContents.random())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // 根据提醒方式设置声音和振动
        when (notifyType) {
            ReminderNotifyType.NOTIFICATION -> {
                // 只发送通知，不需要额外设置
            }
            ReminderNotifyType.RING -> {
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
            }
            ReminderNotifyType.VIBRATE -> {
                builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            }
            ReminderNotifyType.RING_VIBRATE -> {
                builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())  // 使用传入的id
    }

    // 生成唯一的通知ID
    private fun generateNotificationId(): Int {
        return notificationId++
    }

    // 清除所有通知
    fun clearAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
} 