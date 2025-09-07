package com.example.notificationalerter.data

object AppSelectionRepository {
    private val _selectedApps = mutableSetOf<String>()
    val selectedApps: Set<String> get() = _selectedApps

    fun addApp(packageName: String) = _selectedApps.add(packageName)
    fun removeApp(packageName: String) = _selectedApps.remove(packageName)
    fun isSelected(packageName: String) = _selectedApps.contains(packageName)
}