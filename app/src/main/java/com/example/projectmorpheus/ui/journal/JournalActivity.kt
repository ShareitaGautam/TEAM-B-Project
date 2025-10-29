package com.example.projectmorpheus.ui.journal

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectmorpheus.R
import com.example.projectmorpheus.data.AlarmDatabase
import com.example.projectmorpheus.data.JournalEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JournalActivity : AppCompatActivity() {

    private var entryId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_journal_entry)
        title = "Write your dream"

        val titleInput = findViewById<EditText>(R.id.journal_title_input)
        val contentInput = findViewById<EditText>(R.id.journal_content_input)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        // make sure dialog_journal_entry.xml includes it
        val btnDelete = findViewById<Button?>(R.id.btnDelete)

        // Prefill title if opened from alarm
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Dream"
        titleInput.setText(alarmLabel)

        // Get DB + DAO
        val database = AlarmDatabase.getDatabase(applicationContext)
        val journalDao = database.journalDao()

        // Check if editing an existing dream
        entryId = intent.getLongExtra("ENTRY_ID", -1L).takeIf { it > 0 }

        if (entryId != null) {
            // Load dream content for editing if you implement it later
            btnDelete?.visibility = Button.VISIBLE
        } else {
            btnDelete?.visibility = Button.GONE
        }


        btnSave.setOnClickListener {
            val titleText = titleInput.text.toString().trim()
            val contentText = contentInput.text.toString().trim()

            if (titleText.isNotEmpty() && contentText.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val newEntry = JournalEntry(
                        title = titleText,
                        content = contentText,
                        timestamp = System.currentTimeMillis()
                    )
                    journalDao.insert(newEntry)
                }

                Toast.makeText(this, "Dream saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please enter your dream first", Toast.LENGTH_SHORT).show()
            }
        }


        btnDelete?.setOnClickListener {
            entryId?.let { id ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val entry = JournalEntry(id = id, title = "", content = "", timestamp = 0)
                    journalDao.delete(entry)
                }
                Toast.makeText(this, "Dream deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Cancel
        btnCancel.setOnClickListener { finish() }
    }
}
