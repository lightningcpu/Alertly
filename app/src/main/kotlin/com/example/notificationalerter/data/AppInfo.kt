package com.example.notificationalerter.data

import android.graphics.drawable.Drawable

data class AppInfo(
    var packageName: String = "",
    var name: String = "",
    var icon: Drawable? = null,
    var isChecked: Boolean = false
)