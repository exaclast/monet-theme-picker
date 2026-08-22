package com.exaclast.renoir

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
import com.exaclast.renoir.ui.RenoirScreen
import com.materialkolor.DynamicMaterialTheme

import android.provider.Settings
import org.json.JSONObject

import com.exaclast.renoir.data.AppDatabase
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val viewModel: ThemeViewModel = viewModel(
                factory = ThemeViewModel.provideFactory(database.favoriteThemeDao())
            )
            androidx.compose.runtime.LaunchedEffect(Unit) {
                viewModel.reloadFromSystem(this@MainActivity)
            }
            val seedColor by viewModel.seedColor.collectAsState()
            val themeStyle by viewModel.themeStyle.collectAsState()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val contrastLevel by viewModel.contrastLevel.collectAsState()
            
            DynamicMaterialTheme(
                seedColor = seedColor,
                useDarkTheme = isDarkTheme,
                style = themeStyle,
                contrastLevel = contrastLevel
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RenoirScreen(viewModel = viewModel)
                }
            }
        }
    }
}
