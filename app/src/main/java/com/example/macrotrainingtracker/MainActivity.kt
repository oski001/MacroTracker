package com.example.macrotrainingtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.example.macrotrainingtracker.ui.navigation.MacroTrainingApp
import com.example.macrotrainingtracker.ui.theme.MacroTrainingTrackerTheme
import com.example.macrotrainingtracker.ui.viewmodel.TrackerViewModel

import com.example.macrotrainingtracker.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {

    private val container by lazy { AppContainer(applicationContext) }

    private val viewModel: TrackerViewModel by viewModels {
        TrackerViewModel.Factory(
            repository = container.repository,
            calculateNutritionTargetsUseCase = container.calculateNutritionTargetsUseCase
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.loadFromPrefs(this)
        enableEdgeToEdge()
        setContent {
            MacroTrainingTrackerTheme {
                val isDark = ThemeManager.themeConfig.isDarkMode
                SideEffect {
                    window.statusBarColor = if (isDark) 0xFF121212.toInt() else 0xFFFFFFFF.toInt()
                    window.navigationBarColor = if (isDark) 0xFF121212.toInt() else 0xFFFFFFFF.toInt()
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !isDark
                        isAppearanceLightNavigationBars = !isDark
                    }
                }
                MacroTrainingApp(viewModel = viewModel, context = this)
            }
        }
    }
}
