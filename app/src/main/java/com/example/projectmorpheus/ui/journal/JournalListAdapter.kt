package com.example.projectmorpheus.ui.journal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.projectmorpheus.R
import com.example.projectmorpheus.data.JournalEntry
import java.text.SimpleDateFormat
import java.util.*

class JournalListAdapter(
    private val onItemClick: (JournalEntry) -> Unit,
    private val onItemLongClick: (JournalEntry) -> Unit
) : BaseAdapter() {

    private var entries: List<JournalEntry> = emptyList()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())

    fun updateEntries(newEntries: List<JournalEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }

    override fun getCount(): Int = entries.size

    override fun getItem(position: Int): JournalEntry = entries[position]

    override fun getItemId(position: Int): Long = entries[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.journal_list_item, parent, false)

        val entry = entries[position]

        val titleView = view.findViewById<TextView>(R.id.journal_item_title)
        val timestampView = view.findViewById<TextView>(R.id.journal_item_timestamp)
        val contentPreviewView = view.findViewById<TextView>(R.id.journal_item_content_preview)

        titleView.text = entry.title
        timestampView.text = dateFormat.format(Date(entry.timestamp))
        contentPreviewView.text = entry.content

        view.setOnClickListener { onItemClick(entry) }
        view.setOnLongClickListener {
            onItemLongClick(entry)
            true
        }

        return view
    }
}
