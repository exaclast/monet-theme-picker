package com.exaclast.renoir

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.exaclast.renoir.data.FavoriteTheme
import com.exaclast.renoir.data.FavoriteThemeDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.compose.ui.graphics.toArgb

class ThemeViewModel(private val favoriteDao: FavoriteThemeDao) : ViewModel() {

    private val _seedColor = MutableStateFlow(Color(0xFF6750A4)) // Default seed color
    val seedColor: StateFlow<Color> = _seedColor.asStateFlow()

    private val _themeStyle = MutableStateFlow(PaletteStyle.TonalSpot)
    val themeStyle: StateFlow<PaletteStyle> = _themeStyle.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _contrastLevel = MutableStateFlow(0.0) // 0.0 is standard
    val contrastLevel: StateFlow<Double> = _contrastLevel.asStateFlow()

    private val _systemSeedColor = MutableStateFlow<Color?>(null)
    val systemSeedColor: StateFlow<Color?> = _systemSeedColor.asStateFlow()

    private val _systemThemeStyle = MutableStateFlow<PaletteStyle?>(null)
    val systemThemeStyle: StateFlow<PaletteStyle?> = _systemThemeStyle.asStateFlow()

    private val _systemContrastLevel = MutableStateFlow<Double?>(null)
    val systemContrastLevel: StateFlow<Double?> = _systemContrastLevel.asStateFlow()

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

    fun initializeFromSettings(colorHex: String?, styleString: String?, contrastLevelString: String? = null) {
        val newSeedColor = colorHex?.let {
            try {
                Color(android.graphics.Color.parseColor("#$it"))
            } catch (e: Exception) { null }
        } ?: Color(0xFF6750A4)
        
        val newStyle = styleString?.let {
            when (it) {
                "TONAL_SPOT" -> PaletteStyle.TonalSpot
                "VIBRANT" -> PaletteStyle.Vibrant
                "EXPRESSIVE" -> PaletteStyle.Expressive
                "RAINBOW" -> PaletteStyle.Rainbow
                "FRUIT_SALAD" -> PaletteStyle.FruitSalad
                "MONOCHROMATIC" -> PaletteStyle.Monochrome
                else -> PaletteStyle.TonalSpot
            }
        } ?: PaletteStyle.TonalSpot
        
        val newContrast = contrastLevelString?.toDoubleOrNull() ?: 0.0

        _systemSeedColor.value = newSeedColor
        _systemThemeStyle.value = newStyle
        _systemContrastLevel.value = newContrast

        _seedColor.value = newSeedColor
        _themeStyle.value = newStyle
        _contrastLevel.value = newContrast
    }

    fun markCurrentAsSystem() {
        _systemSeedColor.value = _seedColor.value
        _systemThemeStyle.value = _themeStyle.value
        _systemContrastLevel.value = _contrastLevel.value
    }

    fun reloadFromSystem(context: android.content.Context) {
        var initialHex: String? = null
        var initialStyle: String? = null
        var initialContrast: String? = null
        try {
            val overlayJson = android.provider.Settings.Secure.getString(context.contentResolver, "theme_customization_overlay_packages")
            if (!overlayJson.isNullOrEmpty()) {
                val json = org.json.JSONObject(overlayJson)
                initialHex = if (json.has("android.theme.customization.system_palette")) json.getString("android.theme.customization.system_palette") else null
                initialStyle = if (json.has("android.theme.customization.theme_style")) json.getString("android.theme.customization.theme_style") else null
                initialContrast = if (json.has("android.theme.customization.contrast")) json.getString("android.theme.customization.contrast") else null
            }
        } catch (e: Exception) {}
        initializeFromSettings(initialHex, initialStyle, initialContrast)
    }

    val favorites: StateFlow<List<FavoriteTheme>?> = favoriteDao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveCurrentThemeAsFavorite() {
        viewModelScope.launch {
            favoriteDao.insertFavorite(
                FavoriteTheme(
                    seedColor = _seedColor.value.toArgb(),
                    styleName = _themeStyle.value.name,
                    contrastLevel = _contrastLevel.value,
                    isDarkTheme = _isDarkTheme.value
                )
            )
        }
    }

    fun deleteFavorite(favorite: FavoriteTheme) {
        viewModelScope.launch {
            favoriteDao.deleteFavorite(favorite)
        }
    }

    fun loadFavorite(favorite: FavoriteTheme) {
        _seedColor.update { Color(favorite.seedColor) }
        _contrastLevel.update { favorite.contrastLevel }
        // Match style by name
        val style = PaletteStyle.values().find { it.name == favorite.styleName } ?: PaletteStyle.TonalSpot
        _themeStyle.update { style }
    }

    companion object {
        fun provideFactory(favoriteDao: FavoriteThemeDao): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
                    return ThemeViewModel(favoriteDao) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
