// HistoryActivity.kt
package com.example.notificationalerter

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.notificationalerter.data.NotificationHistoryRepository

class HistoryActivity : AppCompatActivity() {
    private lateinit var historyListView: ListView
    private lateinit var clearButton: Button
    private lateinit var deleteSelectedButton: Button
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        historyListView = findViewById(R.id.history_list_view)
        clearButton = findViewById(R.id.clear_button)
        deleteSelectedButton = findViewById(R.id.delete_selected_button)

        setupHistoryList()
        setupButtons()
    }

    private fun setupHistoryList() {
        val historyItems = NotificationHistoryRepository.getHistory()
        adapter = HistoryAdapter(this, historyItems)
        historyListView.adapter = adapter
    }

    private fun setupButtons() {
        clearButton.setOnClickListener {
            NotificationHistoryRepository.clearHistory()
            adapter.notifyDataSetChanged()
        }

        deleteSelectedButton.setOnClickListener {
            NotificationHistoryRepository.deleteSelectedItems()
            setupHistoryList() // Refresh the list
        }
    }
}