package com.example.projectmorpheus.ui.alarm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.projectmorpheus.data.Alarm
import com.example.projectmorpheus.data.AlarmDao
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val alarms: LiveData<List<Alarm>> = alarmDao.getAllAlarms().asLiveData()

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun createAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {
                val id = alarmDao.insert(alarm)
                alarmScheduler.schedule(alarm.copy(id = id))
                Log.d("AlarmViewModel", "Alarm created successfully with ID=$id")
            } catch (e: SecurityException) {
                Log.e("AlarmViewModel", "Failed to schedule alarm: ${e.message}")
                _errorMessage.value = e.message
                // Remove alarm from database since it couldn't be scheduled
                alarmDao.delete(alarm)
            } catch (e: Exception) {
                Log.e("AlarmViewModel", "Unexpected error creating alarm", e)
                _errorMessage.value = "Failed to create alarm: ${e.message}"
                alarmDao.delete(alarm)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmDao.delete(alarm)
            alarmScheduler.cancel(alarm.id)
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            alarmDao.update(updated)
            if (updated.isEnabled) {
                alarmScheduler.schedule(updated)
            } else {
                alarmScheduler.cancel(updated.id)
            }
        }
    }

    fun canScheduleExactAlarms(): Boolean {
        return alarmScheduler.canScheduleExactAlarms()
    }
}
