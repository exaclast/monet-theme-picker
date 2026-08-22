package com.example.monetthemedesigner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monetthemedesigner.ui.MonetThemeDesignerScreen
import com.materialkolor.DynamicMaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ThemeViewModel = viewModel()
            val seedColor by viewModel.seedColor.collectAsState()
            val themeStyle by viewModel.themeStyle.collectAsState()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            
            DynamicMaterialTheme(
                seedColor = seedColor,
                useDarkTheme = isDarkTheme,
                style = themeStyle
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MonetThemeDesignerScreen(viewModel = viewModel)
                }
            }
        }
    }
}
