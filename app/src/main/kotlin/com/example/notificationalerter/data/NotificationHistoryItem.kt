// data/NotificationHistoryItem.kt
package com.example.notificationalerter.data

import android.graphics.drawable.Drawable
import java.util.Date

data class NotificationHistoryItem(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val appIcon: Drawable? = null,
    val title: String?,
    val text: String?,
    val timestamp: Date,
    var isChecked: Boolean = false
)