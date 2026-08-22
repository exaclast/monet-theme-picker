package com.exaclast.renoir.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exaclast.renoir.ThemeViewModel
import com.exaclast.renoir.ui.components.ColorPicker
import com.exaclast.renoir.util.ThemeApplier
import com.exaclast.renoir.util.RenoirCommandGenerator
import com.exaclast.renoir.util.PermissionHelper
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenoirScreen(
    viewModel: ThemeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val seedColor by viewModel.seedColor.collectAsState()
    val themeStyle by viewModel.themeStyle.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val contrastLevel by viewModel.contrastLevel.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val scrollState = rememberScrollState()
    var showPermissionDialog by remember { mutableStateOf(false) }

    if (showPermissionDialog) {
        PermissionInstructionsDialog(onDismiss = { showPermissionDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Renoir") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    val context = LocalContext.current
                    IconButton(onClick = { expanded = true }) {
                        Text("⋮", style = MaterialTheme.typography.titleLarge)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Copy Shell Command") },
                            onClick = {
                                val command = RenoirCommandGenerator.generateCommand(seedColor, themeStyle, contrastLevel)
                                ThemeApplier.copyToClipboard(context, command)
                                expanded = false
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Live Preview Section
            SectionHeader("Live Preview")
            LivePreviewArea()

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Configuration Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                SectionHeader("Configuration")
                TextButton(onClick = { viewModel.saveCurrentThemeAsFavorite() }) {
                    Text("⭐ Save Theme")
                }
            }
            
            ColorPicker(
                selectedColor = seedColor,
                onColorSelected = { viewModel.updateSeedColor(it) },
                favorites = favorites,
                onFavoriteSelected = { viewModel.loadFavorite(it) },
                onFavoriteDeleted = { viewModel.deleteFavorite(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Style", style = MaterialTheme.typography.titleMedium)
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val styles = listOf(
                    PaletteStyle.TonalSpot, 
                    PaletteStyle.Vibrant, 
                    PaletteStyle.Expressive, 
                    PaletteStyle.FruitSalad, 
                    PaletteStyle.Rainbow,
                    PaletteStyle.Monochrome
                )
                items(styles) { style ->
                    StylePreviewChip(
                        style = style,
                        seedColor = seedColor,
                        isDarkTheme = isDarkTheme,
                        isSelected = themeStyle == style,
                        onClick = { viewModel.updateThemeStyle(style) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Contrast Level", style = MaterialTheme.typography.titleMedium)
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val contrasts = listOf(
                    "Reduced" to -1.0,
                    "Standard" to 0.0,
                    "Medium" to 0.5,
                    "High" to 1.0
                )
                items(contrasts) { (name, value) ->
                    ContrastPreviewChip(
                        name = name,
                        contrastLevel = value,
                        style = themeStyle,
                        seedColor = seedColor,
                        isDarkTheme = isDarkTheme,
                        isSelected = contrastLevel == value,
                        onClick = { viewModel.updateContrastLevel(value) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Dark Theme")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = isDarkTheme, onCheckedChange = { viewModel.toggleDarkTheme(it) })
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            

            // Apply and Reset Buttons
            val context = LocalContext.current
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                OutlinedButton(
                    onClick = { viewModel.reloadFromSystem(context) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { 
                        val payload = RenoirCommandGenerator.generateJsonPayload(seedColor, themeStyle, contrastLevel)
                        if (PermissionHelper.hasWriteSecureSettings(context)) {
                            ThemeApplier.applyThemeDirectly(context, payload) 
                        } else if (PermissionHelper.isShizukuRunning()) {
                            if (PermissionHelper.hasShizukuPermission()) {
                                ThemeApplier.applyThemeViaShizuku(context, payload)
                            } else {
                                PermissionHelper.requestShizukuPermission(1)
                                android.widget.Toast.makeText(context, "Please grant Shizuku permission and try again.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            showPermissionDialog = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply Theme")
                }
            }
        }
    }
}

@Composable
fun PermissionInstructionsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permissions Required") },
        text = {
            Column {
                Text("To apply themes, you must grant permission. Choose ONE of the following methods:")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Option 1: Shizuku (On-Device)", style = MaterialTheme.typography.titleSmall)
                Text("Install and start the Shizuku app, then come back and click Apply.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Option 2: ADB (Requires PC)", style = MaterialTheme.typography.titleSmall)
                Text("Run this command on your computer:\nadb shell pm grant com.exaclast.renoir android.permission.WRITE_SECURE_SETTINGS", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

@Composable
fun SectionHeader(title: String) {
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title, 
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivePreviewArea() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        // Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Button(onClick = { }) { Text("Primary") }
            FilledTonalButton(onClick = { }) { Text("Tonal") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedButton(onClick = { }) { Text("Outlined") }
            TextButton(onClick = { }) { Text("Text") }
            Spacer(modifier = Modifier.weight(1f))
            FloatingActionButton(onClick = {}) { Text("+") }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = { }, label = { Text("Filter") })
            SuggestionChip(onClick = { }, label = { Text("Suggestion") })
            AssistChip(onClick = { }, label = { Text("Assist") })
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Checkbox(checked = true, onCheckedChange = {})
            RadioButton(selected = true, onClick = {})
            Switch(checked = true, onCheckedChange = {})
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Slider(value = 0.5f, onValueChange = {})
    }
}

@Composable
fun StylePreviewChip(
    style: PaletteStyle,
    seedColor: androidx.compose.ui.graphics.Color,
    isDarkTheme: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scheme = dynamicColorScheme(seedColor = seedColor, isDark = isDarkTheme, isAmoled = false, style = style)
    
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
            .width(72.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .then(
                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape).padding(6.dp)
                    else Modifier.padding(6.dp)
                )
                .clip(CircleShape)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val c1 = scheme.primary
                val c2 = scheme.secondaryContainer
                val c3 = scheme.tertiary

                drawRect(color = c1, size = androidx.compose.ui.geometry.Size(w / 3, h))
                drawRect(
                    color = c2, 
                    topLeft = androidx.compose.ui.geometry.Offset(w / 3, 0f), 
                    size = androidx.compose.ui.geometry.Size(w / 3, h)
                )
                drawRect(
                    color = c3, 
                    topLeft = androidx.compose.ui.geometry.Offset(2 * w / 3, 0f), 
                    size = androidx.compose.ui.geometry.Size(w / 3, h)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(style.name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
fun ContrastPreviewChip(
    name: String,
    contrastLevel: Double,
    style: PaletteStyle,
    seedColor: androidx.compose.ui.graphics.Color,
    isDarkTheme: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scheme = dynamicColorScheme(
        seedColor = seedColor, 
        isDark = isDarkTheme, 
        isAmoled = false, 
        style = style, 
        contrastLevel = contrastLevel
    )
    
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
            .width(72.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .then(
                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape).padding(6.dp)
                    else Modifier.padding(6.dp)
                )
                .clip(CircleShape)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val c1 = scheme.primary
                val c2 = scheme.secondaryContainer
                val c3 = scheme.tertiary

                drawRect(color = c1, size = androidx.compose.ui.geometry.Size(w / 3, h))
                drawRect(
                    color = c2, 
                    topLeft = androidx.compose.ui.geometry.Offset(w / 3, 0f), 
                    size = androidx.compose.ui.geometry.Size(w / 3, h)
                )
                drawRect(
                    color = c3, 
                    topLeft = androidx.compose.ui.geometry.Offset(2 * w / 3, 0f), 
                    size = androidx.compose.ui.geometry.Size(w / 3, h)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}
