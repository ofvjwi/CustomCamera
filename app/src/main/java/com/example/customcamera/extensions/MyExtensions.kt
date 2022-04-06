package com.example.customcamera.extensions

import android.app.Activity
import android.widget.Toast

object MyExtensions {
    fun Activity.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
