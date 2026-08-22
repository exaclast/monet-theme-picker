package com.example.monetthemedesigner.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object ThemeApplier {

    fun copyToClipboard(context: Context, command: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Monet Command", command)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    /**
     * Attempts to apply the theme directly using the internal Android API.
     * Requires the WRITE_SECURE_SETTINGS permission granted via ADB.
     */
    fun applyThemeDirectly(context: Context, jsonPayload: String) {
        try {
            val success = android.provider.Settings.Secure.putString(
                context.contentResolver,
                "theme_customization_overlay_packages",
                jsonPayload
            )
            if (success) {
                Toast.makeText(context, "Theme applied successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to apply. Did you grant WRITE_SECURE_SETTINGS?", Toast.LENGTH_LONG).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(context, "Permission Denied! Run ADB command to grant WRITE_SECURE_SETTINGS.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Attempts to apply the theme using Shizuku's privileged shell via reflection,
     * as newProcess is hidden in newer Shizuku API versions.
     */
    fun applyThemeViaShizuku(context: Context, jsonPayload: String) {
        try {
            val cmd = arrayOf("settings", "put", "secure", "theme_customization_overlay_packages", "'$jsonPayload'")
            val method = rikka.shizuku.Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, cmd, null, null) as Process
            process.waitFor()
            if (process.exitValue() == 0) {
                Toast.makeText(context, "Theme applied successfully via Shizuku!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Shizuku failed to apply theme.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error using Shizuku: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
