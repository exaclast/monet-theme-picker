package com.example.monetthemedesigner.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.PaletteStyle

object MonetCommandGenerator {

    /**
     * Generates the shell command to apply a Monet theme via Android's internal overlay settings.
     */
    fun generateCommand(color: Color, style: PaletteStyle): String {
        val hexColor = String.format("%06X", 0xFFFFFF and color.toArgb())
        
        // Map PaletteStyle to Android system style strings
        val styleString = when (style) {
            PaletteStyle.TonalSpot -> "TONAL_SPOT"
            PaletteStyle.Vibrant -> "VIBRANT"
            PaletteStyle.Expressive -> "EXPRESSIVE"
            else -> "TONAL_SPOT" // Default
        }

        return "settings put secure theme_customization_overlay_packages " +
                "'{\"android.theme.customization.system_palette\":\"$hexColor\"," +
                "\"android.theme.customization.theme_style\":\"$styleString\"," +
                "\"android.theme.customization.color_source\":\"home_wallpaper\"}'"
    }
}
