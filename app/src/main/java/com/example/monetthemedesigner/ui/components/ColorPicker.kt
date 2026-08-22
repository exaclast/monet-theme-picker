package com.example.monetthemedesigner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.example.monetthemedesigner.data.FavoriteTheme

enum class SliderMode { HSL, RGB, PRESETS, FAVORITES }

@Composable
fun GradientSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    backgroundBrush: Brush
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(24.dp), style = MaterialTheme.typography.labelLarge)
        Box(modifier = Modifier.weight(1f).height(48.dp), contentAlignment = Alignment.Center) {
            // Gradient Track Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(backgroundBrush)
            )
            // Invisible Slider Overlay
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    favorites: List<FavoriteTheme> = emptyList(),
    onFavoriteSelected: ((FavoriteTheme) -> Unit)? = null,
    onFavoriteDeleted: ((FavoriteTheme) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Seed Color", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Live Color Preview and Hex Input
        var hexInput by remember(selectedColor) {
            mutableStateOf(String.format("%06X", 0xFFFFFF and selectedColor.toArgb()))
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(selectedColor)
            )
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedTextField(
                value = hexInput,
                onValueChange = {
                    hexInput = it
                    if (it.length == 6) {
                        try {
                            val parsedColor = Color(android.graphics.Color.parseColor("#$it"))
                            onColorSelected(parsedColor)
                        } catch (e: Exception) {}
                    }
                },
                label = { Text("Hex") },
                prefix = { Text("#") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        var sliderMode by remember { mutableStateOf(SliderMode.HSL) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = sliderMode == SliderMode.HSL,
                onClick = { sliderMode = SliderMode.HSL },
                label = { Text("HSL") }
            )
            FilterChip(
                selected = sliderMode == SliderMode.RGB,
                onClick = { sliderMode = SliderMode.RGB },
                label = { Text("RGB") }
            )
            FilterChip(
                selected = sliderMode == SliderMode.PRESETS,
                onClick = { sliderMode = SliderMode.PRESETS },
                label = { Text("Presets") }
            )
            FilterChip(
                selected = sliderMode == SliderMode.FAVORITES,
                onClick = { sliderMode = SliderMode.FAVORITES },
                label = { Text("Favs") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (sliderMode) {
            SliderMode.HSL -> {
            // HSL State
            var hue by remember { mutableFloatStateOf(0f) }
            var saturation by remember { mutableFloatStateOf(0f) }
            var lightness by remember { mutableFloatStateOf(0f) }
            var isInitialized by remember { mutableStateOf(false) }

            if (!isInitialized) {
                val hsl = FloatArray(3)
                ColorUtils.colorToHSL(selectedColor.toArgb(), hsl)
                hue = hsl[0]
                saturation = hsl[1]
                lightness = hsl[2]
                isInitialized = true
            } else {
                val currentArgb = ColorUtils.HSLToColor(floatArrayOf(hue, saturation, lightness))
                if (selectedColor.toArgb() != currentArgb) {
                    val hsl = FloatArray(3)
                    ColorUtils.colorToHSL(selectedColor.toArgb(), hsl)
                    hue = hsl[0]
                    saturation = hsl[1]
                    lightness = hsl[2]
                }
            }

            val updateHslColor = { h: Float, s: Float, l: Float ->
                onColorSelected(Color(ColorUtils.HSLToColor(floatArrayOf(h, s, l))))
            }

            // Hue Gradient Brush
            val rainbowColors = listOf(
                Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
            )
            val hueBrush = Brush.horizontalGradient(rainbowColors)

            // Saturation Gradient Brush
            val satStartColor = Color(ColorUtils.HSLToColor(floatArrayOf(hue, 0f, lightness)))
            val satEndColor = Color(ColorUtils.HSLToColor(floatArrayOf(hue, 1f, lightness)))
            val satBrush = Brush.horizontalGradient(listOf(satStartColor, satEndColor))

            // Lightness Gradient Brush
            val litMidColor = Color(ColorUtils.HSLToColor(floatArrayOf(hue, saturation, 0.5f)))
            val litBrush = Brush.horizontalGradient(listOf(Color.Black, litMidColor, Color.White))

            GradientSliderRow(
                label = "H",
                value = hue,
                onValueChange = { hue = it; updateHslColor(hue, saturation, lightness) },
                valueRange = 0f..360f,
                backgroundBrush = hueBrush
            )
            GradientSliderRow(
                label = "S",
                value = saturation,
                onValueChange = { saturation = it; updateHslColor(hue, saturation, lightness) },
                valueRange = 0f..1f,
                backgroundBrush = satBrush
            )
            GradientSliderRow(
                label = "L",
                value = lightness,
                onValueChange = { lightness = it; updateHslColor(hue, saturation, lightness) },
                valueRange = 0f..1f,
                backgroundBrush = litBrush
            )
        }
        SliderMode.RGB -> {
            // RGB State
            val r = selectedColor.red
            val g = selectedColor.green
            val b = selectedColor.blue

            val updateRgbColor = { newR: Float, newG: Float, newB: Float ->
                onColorSelected(Color(red = newR, green = newG, blue = newB, alpha = 1f))
            }

            val redBrush = Brush.horizontalGradient(listOf(Color(0f, g, b), Color(1f, g, b)))
            val greenBrush = Brush.horizontalGradient(listOf(Color(r, 0f, b), Color(r, 1f, b)))
            val blueBrush = Brush.horizontalGradient(listOf(Color(r, g, 0f), Color(r, g, 1f)))

            GradientSliderRow(
                label = "R",
                value = r,
                onValueChange = { updateRgbColor(it, g, b) },
                valueRange = 0f..1f,
                backgroundBrush = redBrush
            )
            GradientSliderRow(
                label = "G",
                value = g,
                onValueChange = { updateRgbColor(r, it, b) },
                valueRange = 0f..1f,
                backgroundBrush = greenBrush
            )
            GradientSliderRow(
                label = "B",
                value = b,
                onValueChange = { updateRgbColor(r, g, it) },
                valueRange = 0f..1f,
                backgroundBrush = blueBrush
            )
        }
        SliderMode.PRESETS -> {
            val presets = listOf(
                Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
                Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4),
                Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
                Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722),
                Color(0xFF795548), Color(0xFF9E9E9E), Color(0xFF607D8B), Color(0xFF000000)
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(48.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(160.dp)
            ) {
                items(presets) { color ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onColorSelected(color) }
                    )
                }
                }
            }
            SliderMode.FAVORITES -> {
            if (favorites.isEmpty()) {
                Box(modifier = Modifier.height(160.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No favorites saved yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(160.dp)
                ) {
                    items(favorites) { fav ->
                        val favColor = Color(fav.seedColor)
                        ElevatedCard(
                            onClick = { onFavoriteSelected?.invoke(fav) },
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().background(favColor)) {
                                IconButton(
                                    onClick = { onFavoriteDeleted?.invoke(fav) },
                                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                                ) {
                                    Text("✕", color = Color.White)
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }
}
