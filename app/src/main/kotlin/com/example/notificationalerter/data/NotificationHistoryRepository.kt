// data/NotificationHistoryRepository.kt
package com.example.notificationalerter.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object NotificationHistoryRepository {
    private val _historyItems = mutableListOf<NotificationHistoryItem>()
    private val _historyLiveData = MutableLiveData<List<NotificationHistoryItem>>()
    val historyLiveData: LiveData<List<NotificationHistoryItem>> = _historyLiveData

    fun addHistoryItem(item: NotificationHistoryItem) {
        _historyItems.add(0, item) // Add to beginning for reverse chronological order
        _historyLiveData.value = _historyItems.toList()
    }

    fun clearHistory() {
        _historyItems.clear()
        _historyLiveData.value = emptyList()
    }

    fun deleteSelectedItems() {
        _historyItems.removeAll { it.isChecked }
        _historyLiveData.value = _historyItems.toList()
    }

    fun getHistory(): List<NotificationHistoryItem> = _historyItems.toList()
}