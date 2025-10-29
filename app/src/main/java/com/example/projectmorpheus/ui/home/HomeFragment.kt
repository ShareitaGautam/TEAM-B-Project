package com.example.projectmorpheus.ui.home

import android.os.Bundle
import android.os.Handler
//import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.projectmorpheus.data.AlarmDatabase
import com.example.projectmorpheus.databinding.FragmentHomeBinding
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val hourHand: ImageView = binding.hourhand
        val minuteHand: ImageView = binding.minutehand
        val secondHand: ImageView = binding.secondhand
        val alarmProgress: ProgressBar = binding.alarmprogress
        val alarmDao = AlarmDatabase.getDatabase(requireContext()).alarmDao()
        //run whenever a second passes
        homeViewModel.secondDegrees.observe(viewLifecycleOwner) {
            secondHand.rotation = it.toFloat()
            minuteHand.rotation = homeViewModel.minuteDegrees.value!!
            hourHand.rotation = homeViewModel.hourDegrees.value!!

            val currentTime = Calendar.getInstance()
            alarmDao.getAllAlarms().asLiveData().observe(viewLifecycleOwner) { alarms ->
                if (alarms.isNotEmpty()) {
                    val targetMinute = alarms[0].hourOfDay * 60 + alarms[0].minute
                    val dayMinute = currentTime.get(Calendar.HOUR_OF_DAY) * 60 + currentTime.get(Calendar.MINUTE)
                    alarmProgress.progress = (dayMinute - targetMinute).mod(1440)
                } else {
                    alarmProgress.progress = 0
                }
            }
        }

        //https://stackoverflow.com/questions/55570990/kotlin-call-a-function-every-second
        val mainHandler = Handler()
        mainHandler.post(object : Runnable {
            override fun run() {
                mainHandler.postDelayed(this, 1000)
                homeViewModel.updatetime()
            }
        })


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}