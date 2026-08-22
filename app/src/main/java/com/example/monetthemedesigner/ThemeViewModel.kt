package com.example.monetthemedesigner

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ThemeViewModel : ViewModel() {

    private val _seedColor = MutableStateFlow(Color(0xFF6750A4)) // Default seed color
    val seedColor: StateFlow<Color> = _seedColor.asStateFlow()

    private val _themeStyle = MutableStateFlow(PaletteStyle.TonalSpot)
    val themeStyle: StateFlow<PaletteStyle> = _themeStyle.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _contrastLevel = MutableStateFlow(0.0) // 0.0 is standard
    val contrastLevel: StateFlow<Double> = _contrastLevel.asStateFlow()

    fun updateSeedColor(color: Color) {
        _seedColor.update { color }
    }

    fun updateThemeStyle(style: PaletteStyle) {
        _themeStyle.update { style }
    }

    fun toggleDarkTheme(isDark: Boolean) {
        _isDarkTheme.update { isDark }
    }

    fun updateContrastLevel(level: Double) {
        _contrastLevel.update { level }
    }
}
