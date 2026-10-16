package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.SettingsManager
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.PhotoPreviewScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

sealed interface Screen {
    object Camera : Screen
    object Settings : Screen
    object Gallery : Screen
    data class PhotoPreview(val imagePath: String) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsManager = SettingsManager(this)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WatermarkApp(settingsManager = settingsManager)
                }
            }
        }
    }
}

@Composable
fun WatermarkApp(settingsManager: SettingsManager) {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Camera) }
    val currentScreen = backStack.lastOrNull() ?: Screen.Camera
    var activeConfig by remember { androidx.compose.runtime.mutableStateOf(settingsManager.getDefaultConfig()) }

    BackHandler(enabled = backStack.size > 1) {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
        }
    }

    when (val screen = currentScreen) {
        is Screen.Camera -> {
            CameraScreen(
                config = activeConfig,
                onConfigChange = { activeConfig = it },
                settingsManager = settingsManager,
                onNavigateToSettings = {
                    backStack.add(Screen.Settings)
                },
                onNavigateToGallery = {
                    backStack.add(Screen.Gallery)
                },
                onPhotoCaptured = { path ->
                    backStack.add(Screen.PhotoPreview(path))
                }
            )
        }

        is Screen.Settings -> {
            SettingsScreen(
                settingsManager = settingsManager,
                onBack = {
                    if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                },
                onSaveDefault = { newDefault ->
                    activeConfig = newDefault
                }
            )
        }

        is Screen.Gallery -> {
            GalleryScreen(
                onPhotoClick = { path ->
                    backStack.add(Screen.PhotoPreview(path))
                },
                onBack = {
                    if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                }
            )
        }

        is Screen.PhotoPreview -> {
            PhotoPreviewScreen(
                imagePath = screen.imagePath,
                onBack = {
                    if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                }
            )
        }
    }
}
