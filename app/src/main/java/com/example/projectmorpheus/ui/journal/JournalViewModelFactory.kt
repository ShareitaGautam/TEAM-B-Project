package com.example.projectmorpheus.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projectmorpheus.data.JournalDao

class JournalViewModelFactory(private val journalDao: JournalDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
            return JournalViewModel(journalDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
