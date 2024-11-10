package com.example.notificationalerter

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.notificationalerter.MainActivity
import java.util.Locale

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class MyNotificationListenerService : NotificationListenerService() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NotificationListenerService onCreate")
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "NotificationListenerService onDestroy")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListenerService onListenerConnected")

        // Service is connected, start listening to notifications
        startListening()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "NotificationListenerService onListenerDisconnected")

        // Service is disconnected, stop listening to notifications
        stopListening()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        Log.d(TAG, "Notification posted: " + sbn.packageName)

        val packageName = sbn.packageName
        if (isAppSelected(packageName)) {
            val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
            val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT)
            Log.d(TAG, "Title: $title")
            Log.d(TAG, "Text: $text")

            if (containsSearchWord(text)) {
                if (customSoundUri != null) {
                    playCustomNotificationSound(customSoundUri)
                } else {
                    playDefaultNotificationSound()
                }

                sendNotificationBroadcast(packageName, title, text)
            }
        }
    }

    private fun containsSearchWord(text: String?): Boolean {
        if (searchWord == null || searchWord!!.isEmpty()) {
            return false
        }

        val words = text!!.lowercase(Locale.getDefault()).split("\\W+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val lowercaseSearchWord = searchWord!!.lowercase(Locale.getDefault())
        for (word in words) {
            if (word == lowercaseSearchWord) {
                return true
            }
        }

        return false
    }


    private fun isAppSelected(packageName: String): Boolean {
        return selectedApps.contains(packageName)
    }

    private fun containsSearchWord(sbn: StatusBarNotification): Boolean {
        if (searchWord == null || searchWord!!.isEmpty()) {
            return true
        }

        val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
        val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT)

        return ((title != null && title.lowercase(Locale.getDefault()).contains(searchWord!!.lowercase(Locale.getDefault())))
                || (text != null && text.lowercase(Locale.getDefault()).contains(searchWord!!.lowercase(Locale.getDefault()))))
    }

    private fun playDefaultNotificationSound() {
        try {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            playNotificationSound(defaultSoundUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing default notification sound: " + e.message)
        }
    }

    private fun playCustomNotificationSound(soundUri: Uri?) {
        if (soundUri == null) {
            return
        }

        try {
            playNotificationSound(soundUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing custom notification sound: " + e.message)
        }
    }

    private fun playNotificationSound(soundUri: Uri) {
        try {
            val context = applicationContext
            val ringtone = RingtoneManager.getRingtone(context, soundUri)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
            } else {
                ringtone.streamType = AudioManager.STREAM_NOTIFICATION
            }

            ringtone.play()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing notification sound: " + e.message)
        }
    }

    private fun sendNotificationBroadcast(packageName: String, title: String?, text: String?) {
        // Create an explicit intent for MainActivity
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        // Create a PendingIntent for the notification
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Build the notification
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_lock_idle_low_battery)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        // Get the notification manager
        val notificationManager = NotificationManagerCompat.from(applicationContext)

        // Post the notification
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun startListening() {
        setSearchWord(searchWord)
    }

    private fun stopListening() {
        setSearchWord(null)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Alerter Channel"
            val description = "Channel for alerter notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "NotificationListener"
        private const val ACTION_NOTIFICATION_POSTED = "com.example.notificationalerter.NOTIFICATION_POSTED"
        private const val EXTRA_SOUND_URI = "sound_uri"

        private val selectedApps: MutableSet<String> = HashSet()
        private var searchWord: String? = null
        var customSoundUri: Uri? = null

        private const val CHANNEL_ID = "alerter"
        private const val NOTIFICATION_ID = 175

        fun addSelectedApp(packageName: String) {
            selectedApps.add(packageName)
        }

        fun removeSelectedApp(packageName: String) {
            selectedApps.remove(packageName)
        }

        fun setSearchWord(word: String?) {
            searchWord = word
        }
    }
}
