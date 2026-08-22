package com.example.monetthemedesigner.ui

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
import com.example.monetthemedesigner.ThemeViewModel
import com.example.monetthemedesigner.ui.components.ColorPicker
import com.example.monetthemedesigner.ui.components.TermuxActionCard
import com.example.monetthemedesigner.ui.components.TonalPaletteSwatch
import com.example.monetthemedesigner.util.MonetCommandGenerator
import com.materialkolor.PaletteStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonetThemeDesignerScreen(
    viewModel: ThemeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val seedColor by viewModel.seedColor.collectAsState()
    val themeStyle by viewModel.themeStyle.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monet Theme Designer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Text("+")
            }
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
            Text("Live Preview", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            LivePreviewArea()

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Configuration Section
            Text("Configuration", style = MaterialTheme.typography.titleLarge)
            
            ColorPicker(
                selectedColor = seedColor,
                onColorSelected = { viewModel.updateSeedColor(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Style", style = MaterialTheme.typography.titleMedium)
            
            // Basic dropdown/buttons for style
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Simplified selection for space
                Button(onClick = { viewModel.updateThemeStyle(PaletteStyle.TonalSpot) }) { Text("Tonal Spot") }
                Button(onClick = { viewModel.updateThemeStyle(PaletteStyle.Vibrant) }) { Text("Vibrant") }
                Button(onClick = { viewModel.updateThemeStyle(PaletteStyle.Expressive) }) { Text("Expressive") }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Dark Theme")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = isDarkTheme, onCheckedChange = { viewModel.toggleDarkTheme(it) })
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Palettes preview
            Text("Tonal Palettes", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            TonalPaletteSwatch("Primary", listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary))
            TonalPaletteSwatch("Secondary", listOf(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary))
            TonalPaletteSwatch("Tertiary", listOf(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary))
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Shell Command
            val command = MonetCommandGenerator.generateCommand(seedColor, themeStyle)
            TermuxActionCard(command = command)
        }
    }
}

@Composable
fun LivePreviewArea() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Preview Components", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = { }) { Text("Primary") }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(onClick = { }) { Text("Tonal") }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = { }) { Text("Outlined") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = true, onCheckedChange = {})
                Switch(checked = true, onCheckedChange = {})
                Slider(value = 0.5f, onValueChange = {}, modifier = Modifier.weight(1f))
            }
        }
    }
}
