package com.example.projectmorpheus.ui.alarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.example.projectmorpheus.R
import com.example.projectmorpheus.data.Alarm
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AlarmListAdapter(
    private var alarms: List<Alarm> = emptyList(),
    private val onToggleAlarm: (Alarm) -> Unit,
    private val onDeleteAlarm: (Alarm) -> Unit
) : BaseAdapter() {

    fun updateAlarms(newAlarms: List<Alarm>) {
        alarms = newAlarms
        notifyDataSetChanged()
    }

    override fun getCount(): Int = alarms.size

    override fun getItem(position: Int): Alarm = alarms[position]

    override fun getItemId(position: Int): Long = alarms[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.alarm_list_item, parent, false)

        val alarm = getItem(position)

        // Format time as "7:30 AM"
        val timeText = view.findViewById<TextView>(R.id.alarm_time)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hourOfDay)
            set(Calendar.MINUTE, alarm.minute)
        }
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        timeText.text = timeFormat.format(calendar.time)

        // Set label
        val labelText = view.findViewById<TextView>(R.id.alarm_label)
        labelText.text = alarm.label

        // Set toggle state
        val toggle = view.findViewById<SwitchMaterial>(R.id.alarm_toggle)
        toggle.isChecked = alarm.isEnabled
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != alarm.isEnabled) {
                onToggleAlarm(alarm)
            }
        }

        // Set delete button
        val deleteButton = view.findViewById<ImageButton>(R.id.delete_button)
        deleteButton.setOnClickListener {
            onDeleteAlarm(alarm)
        }

        // Dim text if alarm is disabled
        val alpha = if (alarm.isEnabled) 1.0f else 0.5f
        timeText.alpha = alpha
        labelText.alpha = alpha

        return view
    }
}
