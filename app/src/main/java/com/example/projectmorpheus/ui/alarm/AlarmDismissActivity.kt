package com.example.projectmorpheus.ui.alarm

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.projectmorpheus.MainActivity
import com.example.projectmorpheus.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AlarmDismissActivity : AppCompatActivity() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_dismiss)

        // Show activity over lock screen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // Get alarm data from intent
        val alarmId = intent.getLongExtra("ALARM_ID", -1)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Wake up"
        val shouldVibrate = intent.getBooleanExtra("ALARM_VIBRATE", true)
        val ringtoneUriString = intent.getStringExtra("ALARM_RINGTONE_URI")

        // Set up UI
        val currentTimeText = findViewById<TextView>(R.id.current_time_text)
        val alarmLabelText = findViewById<TextView>(R.id.alarm_label_text)
        val dismissButton = findViewById<Button>(R.id.dismiss_button)

        // Display current time
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        currentTimeText.text = timeFormat.format(calendar.time)

        // Display alarm label
        alarmLabelText.text = alarmLabel

        // Play alarm sound
        playAlarmSound(ringtoneUriString)

        // Continue vibration if enabled (started in AlarmReceiver)
        if (shouldVibrate) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        }

        // Dismiss button handler
        dismissButton.setOnClickListener {
            dismissAlarm()
        }
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        // Prevent dismissing with back button - user must press dismiss button
        // This ensures they're actually awake
        // Intentionally NOT calling super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSoundAndVibration()
    }

    private fun playAlarmSound(ringtoneUriString: String?) {
        try {
            val alarmUri = if (ringtoneUriString != null) {
                Uri.parse(ringtoneUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            ringtone = RingtoneManager.getRingtone(this, alarmUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarmSoundAndVibration() {
        // Stop ringtone
        ringtone?.stop()
        ringtone = null

        // Stop vibration
        vibrator?.cancel()
        vibrator = null
    }

    private fun dismissAlarm() {
        // Stop sound and vibration
        stopAlarmSoundAndVibration()

        // Cancel notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(AlarmReceiver.NOTIFICATION_ID)

        // Navigate to main activity and open journal entry
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_JOURNAL_ENTRY", true)
        }
        startActivity(intent)
        finish()
    }
}
