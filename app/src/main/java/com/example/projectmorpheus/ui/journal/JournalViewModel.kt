package com.example.projectmorpheus.ui.journal

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.projectmorpheus.data.JournalDao
import com.example.projectmorpheus.data.JournalEntry
import kotlinx.coroutines.launch

class JournalViewModel(private val journalDao: JournalDao) : ViewModel() {

    val entries: LiveData<List<JournalEntry>> = journalDao.getAllEntries().asLiveData()

    fun createEntry(title: String, content: String) {
        viewModelScope.launch {
            val entry = JournalEntry(
                title = title,
                content = content
            )
            journalDao.insert(entry)
        }
    }

    fun updateEntry(id: Long, title: String, content: String, timestamp: Long) {
        viewModelScope.launch {
            val entry = JournalEntry(
                id = id,
                title = title,
                content = content,
                timestamp = timestamp
            )
            journalDao.update(entry)
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            journalDao.delete(entry)
        }
    }
}
