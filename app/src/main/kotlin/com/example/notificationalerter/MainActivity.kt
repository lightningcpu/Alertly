package com.example.notificationalerter

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.notificationalerter.data.AppInfo
import com.example.notificationalerter.data.AppSelectionRepository
import com.example.notificationalerter.utils.Constants
import com.example.notificationalerter.utils.NotificationUtils
import com.example.notificationalerter.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private var toggleListenerButton: Button? = null
    private var appListView: ListView? = null
    private var appAdapter: AppAdapter? = null
    private var isListenerEnabled = false
    private var searchInput: EditText? = null
    private var chooseNotificationButton: Button? = null

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleListenerButton = findViewById(R.id.toggle_listener_button)
        appListView = findViewById(R.id.app_list_view)
        searchInput = findViewById(R.id.search_input)
        chooseNotificationButton = findViewById(R.id.choose_notification_button)

        isListenerEnabled = false

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        toggleListenerButton?.setOnClickListener { toggleListener() }
        chooseNotificationButton?.setOnClickListener { openNotificationSoundPicker() }

        setupAppList()
        setupNotificationAccess()

        // Retrieve the selected sound URI from SharedPreferences
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val selectedSoundUriString = preferences.getString(Constants.PREF_SELECTED_SOUND_URI, null)
        if (selectedSoundUriString != null) {
            val selectedSoundUri = Uri.parse(selectedSoundUriString)
            MyNotificationListenerService.customSoundUri = selectedSoundUri
        }

        NotificationUtils.createNotificationChannel(this)
    }

    private fun setupAppList() {
        viewModel.loadInstalledApps()
        viewModel.installedApps.observe(this) { apps ->
            // Initialize apps with their checked state from repository
            val appsWithSelectionState = apps.map { app ->
                app.copy(isChecked = AppSelectionRepository.isSelected(app.packageName))
            }

            appAdapter = AppAdapter(this, appsWithSelectionState)
            appAdapter!!.setOnCheckedChangeListener(object : AppAdapter.OnCheckedChangeListener {
                override fun onCheckedChange(packageName: String?, isChecked: Boolean) {
                    if (packageName != null) {
                        if (isChecked) {
                            AppSelectionRepository.addApp(packageName)
                        } else {
                            AppSelectionRepository.removeApp(packageName)
                        }
                    }
                }
            })
            appListView?.adapter = appAdapter
        }
    }

    private fun setupNotificationAccess() {
        if (isNotificationAccessGranted) {
            toggleListenerButton?.visibility = View.VISIBLE
        } else {
            toggleListenerButton?.visibility = View.GONE
            requestNotificationAccessPermission()
        }
    }

    private fun requestNotificationAccessPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivityForResult(intent, Constants.REQUEST_NOTIFICATION_ACCESS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Constants.REQUEST_NOTIFICATION_ACCESS -> {
                if (isNotificationAccessGranted) {
                    toggleListenerButton?.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, "Notification access not granted", Toast.LENGTH_SHORT).show()
                }
            }
            Constants.REQUEST_SOUND_PICKER -> {
                if (resultCode == RESULT_OK) {
                    val selectedSoundUri = data?.getParcelableExtra<Uri>(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                    if (selectedSoundUri != null) {
                        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                        val editor = preferences.edit()
                        editor.putString(Constants.PREF_SELECTED_SOUND_URI, selectedSoundUri.toString())
                        editor.apply()

                        MyNotificationListenerService.customSoundUri = selectedSoundUri
                        Toast.makeText(this, "Sound selected", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "No sound selected", Toast.LENGTH_SHORT).show()
                }
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
        toggleListenerButton?.text = "Stop Listener"
        isListenerEnabled = true
        Toast.makeText(this, "Listener started", Toast.LENGTH_SHORT).show()

        val searchWord = searchInput?.text?.toString()?.trim()
        MyNotificationListenerService.setSearchWord(searchWord)

        startService(Intent(this, MyNotificationListenerService::class.java))
    }

    private fun stopListener() {
        toggleListenerButton?.text = "Start Listener"
        isListenerEnabled = false
        Toast.makeText(this, "Listener stopped", Toast.LENGTH_SHORT).show()

        stopService(Intent(this, MyNotificationListenerService::class.java))
        MyNotificationListenerService.setSearchWord(null)
    }

    private fun openNotificationSoundPicker() {
        val intent = Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TYPE, android.media.RingtoneManager.TYPE_NOTIFICATION)
            putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Notification Sound")
            putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, null as Uri?)
        }
        startActivityForResult(intent, Constants.REQUEST_SOUND_PICKER)
    }
}