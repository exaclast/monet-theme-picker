package com.exaclast.renoir

import androidx.core.graphics.ColorUtils

fun main() {
    val hsl1 = FloatArray(3)
    ColorUtils.colorToHSL(android.graphics.Color.parseColor("#808080"), hsl1)
    println("TestHsl output: #808080 -> H=${hsl1[0]}, S=${hsl1[1]}, L=${hsl1[2]}")
    
    val hsl2 = FloatArray(3)
    ColorUtils.colorToHSL(android.graphics.Color.parseColor("#000000"), hsl2)
    println("TestHsl output: #000000 -> H=${hsl2[0]}, S=${hsl2[1]}, L=${hsl2[2]}")
    
    val hsl3 = FloatArray(3)
    ColorUtils.colorToHSL(android.graphics.Color.parseColor("#FFFFFF"), hsl3)
    println("TestHsl output: #FFFFFF -> H=${hsl3[0]}, S=${hsl3[1]}, L=${hsl3[2]}")
}
