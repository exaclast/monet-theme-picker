package com.exaclast.renoir.data

data class FavoriteTheme(
    val id: Long = 0,
    val seedColor: Int,
    val styleName: String,
    val contrastLevel: Double,
    val isDarkTheme: Boolean
)
