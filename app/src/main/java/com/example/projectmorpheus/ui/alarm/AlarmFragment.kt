package com.example.projectmorpheus.ui.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.projectmorpheus.databinding.FragmentAlarmBinding

class AlarmFragment : Fragment() {

    private var _binding: FragmentAlarmBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val alarmViewModel =
            ViewModelProvider(this).get(AlarmViewModel::class.java)

        _binding = FragmentAlarmBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textAlarmTitle
        alarmViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
