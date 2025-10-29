package com.example.projectmorpheus.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text


    val secondDegrees = MutableLiveData<Float>().apply {
        value = (6.0*Calendar.getInstance().get(Calendar.SECOND)).toFloat() }
    val minuteDegrees = MutableLiveData<Float>().apply {
        value = (6.0*Calendar.getInstance().get(Calendar.MINUTE)).toFloat() + secondDegrees.value!!/60 }
    val hourDegrees = MutableLiveData<Float>().apply {
        value = (15.0*Calendar.getInstance().get(Calendar.HOUR)).toFloat() + minuteDegrees.value!!/60 }

    fun updatetime() {
        val currentTime = Calendar.getInstance()
        secondDegrees.value = (6.0*currentTime.get(Calendar.SECOND)).toFloat()
        minuteDegrees.value = (6.0*currentTime.get(Calendar.MINUTE)).toFloat() + secondDegrees.value!!/60
        hourDegrees.value = (15.0*currentTime.get(Calendar.HOUR)).toFloat() + minuteDegrees.value!!/60

    }

}