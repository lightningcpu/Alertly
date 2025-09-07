package com.example.notificationalerter

import android.app.Notification
import android.content.Intent
import android.net.Uri
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.notificationalerter.data.AppSelectionRepository
import com.example.notificationalerter.utils.NotificationUtils
import com.example.notificationalerter.utils.SoundManager
import java.util.Locale

class MyNotificationListenerService : NotificationListenerService() {
    private lateinit var soundManager: SoundManager
    private lateinit var notificationManager: NotificationManagerCompat

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NotificationListenerService onCreate")

        soundManager = SoundManager(this)
        notificationManager = NotificationManagerCompat.from(this)
        NotificationUtils.createNotificationChannel(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        searchWord = null
        Log.d(TAG, "NotificationListenerService onDestroy")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        Log.d(TAG, "Notification posted: ${sbn.packageName}")

        val packageName = sbn.packageName
        if (AppSelectionRepository.isSelected(packageName)) {
            val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
            val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT)
            Log.d(TAG, "Title: $title")
            Log.d(TAG, "Text: $text")

            if (containsSearchWord(text)) {
                soundManager.playSound(customSoundUri)
                sendNotificationBroadcast(packageName, title, text)
            }
        }
    }

    private fun containsSearchWord(text: String?): Boolean {
        if (searchWord == null || searchWord!!.isEmpty()) {
            return false
        }

        val words = text?.lowercase(Locale.getDefault())?.split("\\W+".toRegex())?.filter { it.isNotEmpty() }
        val lowercaseSearchWord = searchWord!!.lowercase(Locale.getDefault())

        return words?.any { it == lowercaseSearchWord } ?: false
    }

    private fun sendNotificationBroadcast(packageName: String, title: String?, text: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, NotificationUtils.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val TAG = "NotificationListener"
        private const val NOTIFICATION_ID = 175

        private var searchWord: String? = null
        var customSoundUri: Uri? = null

        fun setSearchWord(word: String?) {
            searchWord = word
        }
    }
}