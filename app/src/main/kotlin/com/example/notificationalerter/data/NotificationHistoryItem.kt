// data/NotificationHistoryItem.kt
package com.example.notificationalerter.data

import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "notification_history")
data class NotificationHistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val timestamp: Date,
) {
    // Transient property won't be persisted by Room
    @Transient
    var appIcon: Drawable? = null

    @Transient
    var isChecked: Boolean = false
}