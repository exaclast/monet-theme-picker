package com.example.monetthemedesigner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var hexInput by remember(selectedColor) {
        mutableStateOf(String.format("%06X", 0xFFFFFF and selectedColor.toArgb()))
    }

    val presetColors = listOf(
        Color(0xFF6750A4), // Purple
        Color(0xFFB3261E), // Red
        Color(0xFF386A20), // Green
        Color(0xFF006874), // Cyan
        Color(0xFF005AC1), // Blue
        Color(0xFF7D5260), // Pink
        Color(0xFF825500)  // Orange
    )

    Column(modifier = modifier.padding(16.dp)) {
        Text("Seed Color", style = MaterialTheme.typography.titleMedium)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Hex Input
        OutlinedTextField(
            value = hexInput,
            onValueChange = {
                hexInput = it
                if (it.length == 6) {
                    try {
                        val parsedColor = Color(android.graphics.Color.parseColor("#$it"))
                        onColorSelected(parsedColor)
                    } catch (e: Exception) {
                        // Ignore invalid hex
                    }
                }
            },
            label = { Text("Hex Code") },
            prefix = { Text("#") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        // Preset Colors
        Text("Presets", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(presetColors) { color ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onColorSelected(color) }
                ) {
                    if (selectedColor == color) {
                        Box(modifier = Modifier.matchParentSize().background(Color.White.copy(alpha = 0.4f)))
                    }
                }
            }
        }
    }
}
