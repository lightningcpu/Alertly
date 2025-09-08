// HistoryActivity.kt
package com.example.notificationalerter

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.notificationalerter.data.NotificationHistoryRepository

class HistoryActivity : AppCompatActivity() {
    private lateinit var historyListView: ListView
    private lateinit var clearButton: Button
    private lateinit var deleteSelectedButton: Button
    private lateinit var adapter: HistoryAdapter
    private lateinit var repository: NotificationHistoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        historyListView = findViewById(R.id.history_list_view)
        clearButton = findViewById(R.id.clear_button)
        deleteSelectedButton = findViewById(R.id.delete_selected_button)

        repository = NotificationHistoryRepository.getInstance(applicationContext)
        setupHistoryList()
        setupButtons()
    }

    private fun setupHistoryList() {
        adapter = HistoryAdapter(this, ArrayList())
        historyListView.adapter = adapter

        repository.historyLiveData.observe(this) { items ->
            // Load app icons for each item
            items.forEach { item ->
                item.appIcon = loadAppIcon(item.packageName)
            }
            adapter.clear()
            adapter.addAll(items)
            adapter.notifyDataSetChanged()
        }
    }

    private fun loadAppIcon(packageName: String): Drawable? {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }

    private fun setupButtons() {
        clearButton.setOnClickListener {
            repository.clearHistory()
        }

        deleteSelectedButton.setOnClickListener {
            val selectedIds = adapter.getSelectedIds()
            repository.deleteSelectedItems(selectedIds)
        }
    }
}