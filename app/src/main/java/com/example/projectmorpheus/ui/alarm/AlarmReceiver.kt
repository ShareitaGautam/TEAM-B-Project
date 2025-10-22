package com.example.projectmorpheus.ui.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.projectmorpheus.R

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "alarm_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("ALARM_ID", -1)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Wake up"
        val shouldVibrate = intent.getBooleanExtra("ALARM_VIBRATE", true)
        val ringtoneUri = intent.getStringExtra("ALARM_RINGTONE_URI")

        Log.d("AlarmReceiver", "✓ ALARM TRIGGERED! ID=$alarmId, Label=$alarmLabel")

        // Create notification channel for Android 8+
        createNotificationChannel(context)

        // Vibrate if enabled
        if (shouldVibrate) {
            triggerVibration(context)
        }

        // Full-screen alarm activity
        val fullScreenIntent = Intent(context, AlarmDismissActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
            putExtra("ALARM_VIBRATE", shouldVibrate)
            putExtra("ALARM_RINGTONE_URI", ringtoneUri)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            (if (alarmId >= 0) alarmId else 0L).toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // NOTE COMPOSE screen (JournalActivity)
        val noteIntent = Intent(context, com.example.projectmorpheus.ui.journal.JournalActivity::class.java).apply {
            putExtra("ALARM_LABEL", "Dream — $alarmLabel")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val notePending = PendingIntent.getActivity(
            context,
            (if (alarmId >= 0) alarmId else 0L).toInt(),
            noteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm: $alarmLabel")
            .setContentText("Time to wake up!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            // >>> NEW: make the notification open the note screen
            .setContentIntent(notePending)
            .addAction(R.drawable.ic_launcher_foreground, "Capture Dream", notePending)
            // <<< NEW
            .build()

        // >>> NEW: Android 13+ permission check to avoid SecurityException
        val canNotify = Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (canNotify) {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } else {
            Log.w("AlarmReceiver", "Notification permission not granted on Android 13+.")
        }
        // <<< NEW
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Notifications"
            val descriptionText = "Notifications for alarm reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // Set alarm sound
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                setSound(
                    alarmSound,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun triggerVibration(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // Vibrate pattern: wait 0ms, vibrate 1000ms, wait 1000ms, repeat
        val pattern = longArrayOf(0, 1000, 1000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, 0)
        }
    }
}
