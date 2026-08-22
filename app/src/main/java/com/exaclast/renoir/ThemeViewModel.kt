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

    fun exportFavorites(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val currentFavorites = favorites.value ?: return@launch
                val jsonArray = org.json.JSONArray()
                for (fav in currentFavorites) {
                    val color = Color(fav.seedColor)
                    val style = PaletteStyle.values().find { it.name == fav.styleName } ?: PaletteStyle.TonalSpot
                    val jsonString = com.exaclast.renoir.util.RenoirCommandGenerator.generateJsonPayload(
                        color = color, 
                        style = style, 
                        contrastLevel = fav.contrastLevel
                    )
                    jsonArray.put(org.json.JSONObject(jsonString))
                }
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonArray.toString(2).toByteArray())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importFavorites(context: android.content.Context, uri: android.net.Uri, replace: Boolean) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (replace) {
                    favoriteDao.deleteAllFavorites()
                }
                
                val existingSignatures = if (replace) mutableSetOf() else {
                    favorites.value?.map { 
                        Triple(it.seedColor, it.styleName, it.contrastLevel) 
                    }?.toMutableSet() ?: mutableSetOf()
                }

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val jsonString = inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = org.json.JSONArray(jsonString)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        
                        val hex = if (obj.has("android.theme.customization.system_palette")) obj.getString("android.theme.customization.system_palette") else "6750A4"
                        val styleStr = if (obj.has("android.theme.customization.theme_style")) obj.getString("android.theme.customization.theme_style") else "TONAL_SPOT"
                        val contrastStr = if (obj.has("android.theme.customization.contrast")) obj.getString("android.theme.customization.contrast") else "0.0"
                        
                        val parsedColor = try {
                            android.graphics.Color.parseColor("#$hex")
                        } catch (e: Exception) {
                            android.graphics.Color.parseColor("#6750A4")
                        }
                        
                        val contrast = contrastStr.toDoubleOrNull() ?: 0.0
                        
                        val mappedStyleName = when (styleStr) {
                            "TONAL_SPOT" -> PaletteStyle.TonalSpot.name
                            "VIBRANT" -> PaletteStyle.Vibrant.name
                            "EXPRESSIVE" -> PaletteStyle.Expressive.name
                            "RAINBOW" -> PaletteStyle.Rainbow.name
                            "FRUIT_SALAD" -> PaletteStyle.FruitSalad.name
                            "MONOCHROMATIC" -> PaletteStyle.Monochrome.name
                            else -> PaletteStyle.TonalSpot.name
                        }
                        
                        val signature = Triple(parsedColor, mappedStyleName, contrast)
                        if (!existingSignatures.contains(signature)) {
                            existingSignatures.add(signature)
                            val favorite = FavoriteTheme(
                                seedColor = parsedColor,
                                styleName = mappedStyleName,
                                contrastLevel = contrast,
                                isDarkTheme = false // Standardized default
                            )
                            favoriteDao.insertFavorite(favorite)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
