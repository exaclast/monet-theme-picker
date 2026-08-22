package com.exaclast.renoir.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import rikka.shizuku.Shizuku

object PermissionHelper {

    fun hasWriteSecureSettings(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_SECURE_SETTINGS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isShizukuRunning(): Boolean {
        return Shizuku.pingBinder()
    }

    fun hasShizukuPermission(): Boolean {
        return if (isShizukuRunning()) {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    fun requestShizukuPermission(requestCode: Int) {
        if (isShizukuRunning() && !hasShizukuPermission()) {
            Shizuku.requestPermission(requestCode)
        }
    }
}
