package com.example.notificationalerter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var toggleListenerButton: Button? = null
    private var appListView: ListView? = null
    private var appAdapter: AppAdapter? = null
    private var isListenerEnabled = false
    private var searchInput: EditText? = null

    private var chooseNotificationButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleListenerButton = findViewById(R.id.toggle_listener_button)
        appListView = findViewById(R.id.app_list_view)
        searchInput = findViewById(R.id.search_input)
        chooseNotificationButton = findViewById(R.id.choose_notification_button)

        isListenerEnabled = false

        toggleListenerButton?.setOnClickListener(View.OnClickListener { toggleListener() })

        chooseNotificationButton?.setOnClickListener(View.OnClickListener { openNotificationSoundPicker() })

        appAdapter = AppAdapter(this, installedApps)
        appAdapter!!.setOnCheckedChangeListener(object : AppAdapter.OnCheckedChangeListener {
            override fun onCheckedChange(packageName: String?, isChecked: Boolean) {
                if (packageName != null) {
                    if (isChecked) {
                        MyNotificationListenerService.addSelectedApp(packageName)
                        Toast.makeText(this@MainActivity, "Selected: $packageName", Toast.LENGTH_SHORT).show()
                    } else {
                        MyNotificationListenerService.removeSelectedApp(packageName)
                        Toast.makeText(this@MainActivity, "Deselected: $packageName", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        appListView?.setAdapter(appAdapter)

        if (isNotificationAccessGranted) {
            toggleListenerButton?.setVisibility(View.VISIBLE)
        } else {
            toggleListenerButton?.setVisibility(View.GONE)
            requestNotificationAccessPermission()
        }

        // Retrieve the selected sound URI from SharedPreferences
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val selectedSoundUriString = preferences.getString(PREF_SELECTED_SOUND_URI, null)
        if (selectedSoundUriString != null) {
            val selectedSoundUri = Uri.parse(selectedSoundUriString)
            MyNotificationListenerService.customSoundUri = selectedSoundUri
        }

        createNotificationChannel()
    }

    private val installedApps: List<AppInfo>
        get() {
            val appList: MutableList<AppInfo> = ArrayList()
            val packageManager = packageManager
            val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val appPackageName = packageName

            for (applicationInfo in applications) {
                val packageName = applicationInfo.packageName

                // Exclude the app itself
                if (packageName != appPackageName && isLaunchable(packageManager, packageName)) {
                    val appInfo = AppInfo()
                    appInfo.packageName = packageName
                    appInfo.name = applicationInfo.loadLabel(packageManager).toString()
                    appInfo.icon = applicationInfo.loadIcon(packageManager)
                    appList.add(appInfo)
                }
            }

            return appList
        }

    private fun isLaunchable(packageManager: PackageManager, packageName: String): Boolean {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        return launchIntent != null
    }

    private fun requestNotificationAccessPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivityForResult(intent, REQUEST_NOTIFICATION_ACCESS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_NOTIFICATION_ACCESS) {
            if (isNotificationAccessGranted) {
                toggleListenerButton!!.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Notification access not granted.", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQUEST_SOUND_PICKER) {
            if (resultCode == RESULT_OK) {
                val selectedSoundUri = data!!.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                if (selectedSoundUri != null) {
                    // Save the selected sound URI to SharedPreferences
                    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                    val editor = preferences.edit()
                    editor.putString(PREF_SELECTED_SOUND_URI, selectedSoundUri.toString())
                    editor.apply()

                    // Handle the selected sound URI here
                    // You can save it or use it to set the notification sound for your app
                    MyNotificationListenerService.customSoundUri = selectedSoundUri
                    Toast.makeText(this, "Selected sound: $selectedSoundUri", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No sound selected.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val isNotificationAccessGranted: Boolean
        get() {
            val cn = ComponentName(this, MyNotificationListenerService::class.java)
            val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            return flat != null && flat.contains(cn.flattenToString())
        }

    private fun toggleListener() {
        if (isListenerEnabled) {
            stopListener()
        } else {
            startListener()
        }
    }

    private fun startListener() {
        toggleListenerButton!!.text = "Stop Listener"
        isListenerEnabled = true
        Toast.makeText(this, "Listener started", Toast.LENGTH_SHORT).show()

        // Get the search word from the input field
        val searchWord = searchInput!!.text.toString().trim { it <= ' ' }

        // Pass the search word to the notification listener service
        MyNotificationListenerService.setSearchWord(searchWord)

        // Start the notification listener service
        startService(Intent(this, MyNotificationListenerService::class.java))
    }

    private fun stopListener() {
        toggleListenerButton!!.text = "Start Listener"
        isListenerEnabled = false
        Toast.makeText(this, "Listener stopped", Toast.LENGTH_SHORT).show()

        // Stop the notification listener service
        stopService(Intent(this, MyNotificationListenerService::class.java))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Alerter Channel"
            val description = "Channel for alerter notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
            channel.description = description

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun openNotificationSoundPicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Notification Sound")
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, null as Uri?)
        startActivityForResult(intent, REQUEST_SOUND_PICKER)
    }

    companion object {
        private const val REQUEST_NOTIFICATION_ACCESS = 1
        private const val REQUEST_SOUND_PICKER = 2
        private const val NOTIFICATION_CHANNEL_ID = "alerter_channel_id"

        private const val PREF_SELECTED_SOUND_URI = "selected_sound_uri"
    }
}


