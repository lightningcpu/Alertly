package com.example.notificationalerter

import android.graphics.drawable.Drawable

// This is the info of each listed app
class AppInfo {
    @JvmField
    var name: String? = null
    @JvmField
    var packageName: String? = null
    @JvmField
    var icon: Drawable? = null
    var isChecked: Boolean = false
}