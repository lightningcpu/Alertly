// data/NotificationHistoryRepository.kt
package com.example.notificationalerter.data

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationHistoryRepository private constructor(context: Context) {
    private val notificationHistoryDao: NotificationHistoryDao
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val historyLiveData: LiveData<List<NotificationHistoryItem>>

    init {
        val database = AppDatabase.getDatabase(context)
        notificationHistoryDao = database.notificationHistoryDao()
        historyLiveData = notificationHistoryDao.getAllByDateDesc()
    }

    fun addHistoryItem(item: NotificationHistoryItem) {
        coroutineScope.launch {
            // Copy the item without the transient properties for storage
            val itemForDb = item
            notificationHistoryDao.insert(itemForDb)
        }
    }

    fun clearHistory() {
        coroutineScope.launch {
            notificationHistoryDao.clearAll()
        }
    }

    fun deleteSelectedItems(selectedIds: List<Long>) {
        coroutineScope.launch {
            notificationHistoryDao.deleteByIds(selectedIds)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: NotificationHistoryRepository? = null

        fun getInstance(context: Context): NotificationHistoryRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = NotificationHistoryRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}