package com.example.monetthemedesigner.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.PaletteStyle

object MonetCommandGenerator {

    /**
     * Generates the JSON payload required for the secure setting.
     */
    fun generateJsonPayload(color: Color, style: PaletteStyle, contrastLevel: Double = 0.0): String {
        val hexColor = String.format("%06X", 0xFFFFFF and color.toArgb())
        
        // Map PaletteStyle to Android system style strings
        val styleString = when (style) {
            PaletteStyle.TonalSpot -> "TONAL_SPOT"
            PaletteStyle.Vibrant -> "VIBRANT"
            PaletteStyle.Expressive -> "EXPRESSIVE"
            PaletteStyle.Rainbow -> "RAINBOW"
            PaletteStyle.FruitSalad -> "FRUIT_SALAD"
            PaletteStyle.Monochrome -> "MONOCHROMATIC"
            else -> "TONAL_SPOT" // Default
        }

        return "{\"android.theme.customization.system_palette\":\"$hexColor\"," +
               "\"android.theme.customization.theme_style\":\"$styleString\"," +
               "\"android.theme.customization.color_source\":\"preset\"," +
               "\"android.theme.customization.contrast\":\"$contrastLevel\"}"
    }

    /**
     * Generates the shell command to apply a Monet theme via Android's internal overlay settings.
     */
    fun generateCommand(color: Color, style: PaletteStyle, contrastLevel: Double = 0.0): String {
        val payload = generateJsonPayload(color, style, contrastLevel)
        return "settings put secure theme_customization_overlay_packages '$payload'"
    }
}
