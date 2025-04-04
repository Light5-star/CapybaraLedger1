package com.xuhh.capybaraledger.utils

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
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

    // 添加振动模式常量
    private val VIBRATE_PATTERN_FOREGROUND = longArrayOf(0, 500) // 单次振动500ms
    private val VIBRATE_PATTERN_BACKGROUND = longArrayOf(0, 500, 200, 500, 200, 500) // 三次振动

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
        title: String,
        notifyType: ReminderNotifyType,
        id: Int
    ) {
        // 检查应用是否在前台
        val isAppForeground = isAppForeground(context)
        
        if (isAppForeground) {
            // 应用在前台，显示Toast并振动一次
            when (notifyType) {
                ReminderNotifyType.VIBRATE, ReminderNotifyType.RING_VIBRATE -> {
                    vibrate(context, VIBRATE_PATTERN_FOREGROUND)
                }
                else -> {}
            }
            Toast.makeText(context, title, Toast.LENGTH_LONG).show()
        } else {
            // 应用不在前台，显示通知
            showNotification(context, title, notifyType, id)
        }
    }

    private fun showNotification(
        context: Context,
        title: String,
        notifyType: ReminderNotifyType,
        id: Int
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.capybara)
            .setContentTitle(title)
            .setContentText("点击进入记账")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // 根据提醒方式设置振动
        when (notifyType) {
            ReminderNotifyType.VIBRATE, ReminderNotifyType.RING_VIBRATE -> {
                builder.setVibrate(VIBRATE_PATTERN_BACKGROUND)
            }
            else -> {}
        }

        // 根据提醒方式设置声音
        when (notifyType) {
            ReminderNotifyType.RING, ReminderNotifyType.RING_VIBRATE -> {
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            }
            else -> {}
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
    }

    private fun vibrate(context: Context, pattern: LongArray) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun isAppForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName

        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && appProcess.processName == packageName) {
                return true
            }
        }
        return false
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