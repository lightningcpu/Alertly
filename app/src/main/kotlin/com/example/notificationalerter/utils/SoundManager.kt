package com.example.notificationalerter.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log

class SoundManager(private val context: Context) {
    companion object {
        private const val TAG = "SoundManager"
    }

    fun getDefaultSoundUri(): Uri =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    fun playSound(uri: Uri?) {
        uri ?: return

        try {
            val ringtone = RingtoneManager.getRingtone(context, uri)

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
            Log.e(TAG, "Error playing sound: ${e.message}")
        }
    }
}