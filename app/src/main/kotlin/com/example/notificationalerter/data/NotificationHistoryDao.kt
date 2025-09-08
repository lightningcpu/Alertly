// data/NotificationHistoryDao.kt
package com.example.notificationalerter.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotificationHistoryDao {
    @Insert
    suspend fun insert(item: NotificationHistoryItem): Long

    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC")
    fun getAllByDateDesc(): LiveData<List<NotificationHistoryItem>>

    @Query("DELETE FROM notification_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notification_history WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM notification_history")
    suspend fun clearAll()
}