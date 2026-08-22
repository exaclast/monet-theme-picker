package com.example.monetthemedesigner.data

import android.content.ContentValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FavoriteThemeDao(private val dbHelper: AppDatabase) {
    private val _favoritesFlow = MutableStateFlow<List<FavoriteTheme>>(emptyList())
    
    init {
        refreshFavorites()
    }

    fun getAllFavorites(): Flow<List<FavoriteTheme>> {
        return _favoritesFlow.asStateFlow()
    }

    private fun refreshFavorites() {
        val list = mutableListOf<FavoriteTheme>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AppDatabase.TABLE_FAVORITES,
            null, null, null, null, null, 
            "${AppDatabase.COLUMN_ID} DESC"
        )
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(AppDatabase.COLUMN_ID))
                val seedColor = getInt(getColumnIndexOrThrow(AppDatabase.COLUMN_SEED_COLOR))
                val styleName = getString(getColumnIndexOrThrow(AppDatabase.COLUMN_STYLE_NAME))
                val contrastLevel = getDouble(getColumnIndexOrThrow(AppDatabase.COLUMN_CONTRAST_LEVEL))
                val isDarkTheme = getInt(getColumnIndexOrThrow(AppDatabase.COLUMN_IS_DARK_THEME)) == 1
                list.add(FavoriteTheme(id, seedColor, styleName, contrastLevel, isDarkTheme))
            }
        }
        cursor.close()
        _favoritesFlow.update { list }
    }

    suspend fun insertFavorite(theme: FavoriteTheme) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_SEED_COLOR, theme.seedColor)
            put(AppDatabase.COLUMN_STYLE_NAME, theme.styleName)
            put(AppDatabase.COLUMN_CONTRAST_LEVEL, theme.contrastLevel)
            put(AppDatabase.COLUMN_IS_DARK_THEME, if (theme.isDarkTheme) 1 else 0)
        }
        db.insert(AppDatabase.TABLE_FAVORITES, null, values)
        refreshFavorites()
    }

    suspend fun deleteFavorite(theme: FavoriteTheme) {
        val db = dbHelper.writableDatabase
        db.delete(
            AppDatabase.TABLE_FAVORITES, 
            "${AppDatabase.COLUMN_ID} = ?", 
            arrayOf(theme.id.toString())
        )
        refreshFavorites()
    }
}
