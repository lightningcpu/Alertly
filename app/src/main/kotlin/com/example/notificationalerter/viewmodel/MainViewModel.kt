package com.example.notificationalerter.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.notificationalerter.data.AppInfo

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _installedApps = MutableLiveData<List<AppInfo>>()
    val installedApps: LiveData<List<AppInfo>> = _installedApps

    fun loadInstalledApps() {
        val appList = mutableListOf<AppInfo>()
        val packageManager = getApplication<Application>().packageManager
        val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appPackageName = getApplication<Application>().packageName

        for (applicationInfo in applications) {
            val packageName = applicationInfo.packageName

            if (packageName != appPackageName && isLaunchable(packageManager, packageName)) {
                val appInfo = AppInfo().apply {
                    this.packageName = packageName
                    this.name = applicationInfo.loadLabel(packageManager).toString()
                    this.icon = applicationInfo.loadIcon(packageManager)
                }
                appList.add(appInfo)
            }
        }

        _installedApps.value = appList
    }

    private fun isLaunchable(packageManager: PackageManager, packageName: String): Boolean {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        return launchIntent != null
    }
}