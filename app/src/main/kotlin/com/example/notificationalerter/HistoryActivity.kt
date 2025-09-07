// HistoryActivity.kt
package com.example.notificationalerter

import android.os.Bundle
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
        // Create adapter with an empty mutable list so we can update it later
        adapter = HistoryAdapter(this, ArrayList())
        historyListView.adapter = adapter

        // Observe repository LiveData to keep UI in sync automatically
        NotificationHistoryRepository.historyLiveData.observe(this) { items ->
            adapter.clear()
            adapter.addAll(items)
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupButtons() {
        clearButton.setOnClickListener {
            NotificationHistoryRepository.clearHistory()
            // No need to manually update adapter — LiveData observer will run
        }

        deleteSelectedButton.setOnClickListener {
            NotificationHistoryRepository.deleteSelectedItems()
            // LiveData observer will update the adapter
        }
    }
}
