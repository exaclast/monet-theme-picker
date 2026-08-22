package com.example.monetthemedesigner.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

object TermuxIntegration {

    /**
     * Executes the generated command in Termux, or copies to clipboard if Termux is unavailable.
     */
    fun runTermuxCommand(context: Context, command: String, useSu: Boolean = true) {
        val intent = Intent("com.termux.RUN_COMMAND").apply {
            setPackage("com.termux")
            val executable = if (useSu) "su" else "/system/bin/sh"
            val args = if (useSu) arrayOf("-c", command) else arrayOf("-c", command)
            
            putExtra("com.termux.RUN_COMMAND_PATH", executable)
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", args)
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", false) // false opens termux to show result/errors
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startService(intent)
            Toast.makeText(context, "Command sent to Termux", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Fallback: Copy to clipboard
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Monet Command", command)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Termux not found. Command copied to clipboard.", Toast.LENGTH_LONG).show()
        }
    }
    
    fun copyToClipboard(context: Context, command: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Monet Command", command)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
