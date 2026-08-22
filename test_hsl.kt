import androidx.core.graphics.ColorUtils

fun main() {
    val color1 = ColorUtils.HSLToColor(floatArrayOf(120f, 0f, 0.5f))
    val color2 = ColorUtils.HSLToColor(floatArrayOf(0f, 0f, 0.5f))
    println("color1: $color1, color2: $color2")
}
