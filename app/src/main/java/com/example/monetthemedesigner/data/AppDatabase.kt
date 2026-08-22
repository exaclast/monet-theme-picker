package com.example.monetthemedesigner.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "monet_theme_designer.db"
        const val DATABASE_VERSION = 1
        const val TABLE_FAVORITES = "favorite_themes"
        
        const val COLUMN_ID = "id"
        const val COLUMN_SEED_COLOR = "seedColor"
        const val COLUMN_STYLE_NAME = "styleName"
        const val COLUMN_CONTRAST_LEVEL = "contrastLevel"
        const val COLUMN_IS_DARK_THEME = "isDarkTheme"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_FAVORITES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SEED_COLOR INTEGER NOT NULL,
                $COLUMN_STYLE_NAME TEXT NOT NULL,
                $COLUMN_CONTRAST_LEVEL REAL NOT NULL,
                $COLUMN_IS_DARK_THEME INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    private val favoriteDaoInstance by lazy { FavoriteThemeDao(this) }
    
    fun favoriteThemeDao(): FavoriteThemeDao = favoriteDaoInstance
}
