package com.example.macrotrainingtracker.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

// ── Theme Configuration ───────────────────────────────────────────────────────

data class ThemeConfig(
    val isDarkMode: Boolean = true,
    val cornerRadius: Int = 12,        // dp: 0-30
    val fontFamily: String = "default" // "default", "serif", "monospace", "custom_*"
)

enum class NavigationTab(val id: String, val label: String, val icon: String) {
    FOOD  ("food",   "Żywienie", "search"),
    TODAY ("today",  "Dziś",     "home"),
    LOGS  ("logs",   "Historia", "calendar"),
    MENU  ("menu",   "Menu",     "menu")
}

enum class FoodScreenElement(val id: String, val label: String) {
    QUICK_ADD     ("quick_add",      "Quick Add"),
    NEW_PRODUCT   ("new_product",    "Nowy Produkt"),
    SEARCH_BAR    ("search_bar",     "Wyszukiwarka"),
    BARCODE_SCAN  ("barcode_scan",   "Skaner kodów"),
    CATEGORY_CHIPS("category_chips", "Kategorie"),
    PRODUCTS_GRID ("products_grid",  "Siatka produktów")
}

data class LayoutConfig(
    val navigationOrder: List<String> = listOf("food", "today", "logs", "menu"),
    val foodScreenOrder: List<String> = listOf("quick_add", "new_product", "search_bar", "category_chips", "products_grid")
)

// ── Theme Preset ──────────────────────────────────────────────────────────────

data class AppThemePreset(
    val id: String,
    val name: String,
    val accentColor: Color,
    val isBuiltIn: Boolean = false
)

// ── Theme Manager ─────────────────────────────────────────────────────────────

object ThemeManager {

    // ── Built-in presets ──────────────────────────────────────────────────────
    val builtInPresets = listOf(
        AppThemePreset("cyan",   "Cyber Cyan",      Color(0xFF00E5FF), true),
        AppThemePreset("green",  "Neon Green",      Color(0xFF00E676), true),
        AppThemePreset("purple", "Electric Purple", Color(0xFFAA00FF), true),
        AppThemePreset("orange", "Flame Orange",    Color(0xFFFF6D00), true),
        AppThemePreset("pink",   "Neon Pink",       Color(0xFFFF4081), true),
        AppThemePreset("yellow", "Solar Yellow",    Color(0xFFFFD600), true),
        AppThemePreset("red",    "Blood Red",       Color(0xFFFF1744), true),
        AppThemePreset("teal",   "Deep Teal",       Color(0xFF1DE9B6), true),
    )

    // ── Custom presets ────────────────────────────────────────────────────────
    val customPresets = mutableStateListOf<AppThemePreset>()

    val allPresets: List<AppThemePreset>
        get() = builtInPresets + customPresets

    // ── Current state ─────────────────────────────────────────────────────────
    private var _currentPresetId by mutableStateOf("cyan")
    var themeConfig by mutableStateOf(ThemeConfig())
        private set
    var layoutConfig by mutableStateOf(LayoutConfig())
        private set

    val currentPreset: AppThemePreset
        get() = allPresets.find { it.id == _currentPresetId } ?: builtInPresets[0]

    val accent: Color get() = currentPreset.accentColor
    val cornerRadiusDp get() = themeConfig.cornerRadius.dp
    val fontFamily: FontFamily get() = when (themeConfig.fontFamily) {
        "serif"     -> FontFamily.Serif
        "monospace" -> FontFamily.Monospace
        else        -> FontFamily.Default
    }

    // ── Navigation tabs (reorderable) ─────────────────────────────────────────
    val navigationTabs: List<NavigationTab>
        get() = layoutConfig.navigationOrder.mapNotNull { id ->
            NavigationTab.entries.find { it.id == id }
        }

    val foodScreenElements: List<FoodScreenElement>
        get() = layoutConfig.foodScreenOrder.mapNotNull { id ->
            FoodScreenElement.entries.find { it.id == id }
        }

    // ── Actions ───────────────────────────────────────────────────────────────
    fun setPreset(id: String) {
        _currentPresetId = id
    }

    fun updateThemeConfig(config: ThemeConfig) {
        themeConfig = config
    }

    fun updateLayoutConfig(config: LayoutConfig) {
        layoutConfig = config
    }

    fun addCustomPreset(name: String, color: Color) {
        val id = "custom_${System.currentTimeMillis()}"
        customPresets.add(AppThemePreset(id, name, color, isBuiltIn = false))
    }

    fun removeCustomPreset(id: String) {
        customPresets.removeAll { it.id == id }
    }

    // ── SharedPreferences persistence ─────────────────────────────────────────
    fun saveToPrefs(context: Context) {
        val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("preset_id", _currentPresetId)
            .putBoolean("dark_mode", themeConfig.isDarkMode)
            .putInt("corner_radius", themeConfig.cornerRadius)
            .putString("font_family", themeConfig.fontFamily)
            .putString("nav_order", layoutConfig.navigationOrder.joinToString(","))
            .putString("food_order", layoutConfig.foodScreenOrder.joinToString(","))
            .putInt("custom_count", customPresets.size)
            .also { ed ->
                customPresets.forEachIndexed { i, p ->
                    ed.putString("custom_${i}_id",    p.id)
                    ed.putString("custom_${i}_name",  p.name)
                    ed.putLong  ("custom_${i}_color", p.accentColor.value.toLong())
                }
            }
            .apply()
    }

    fun loadFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        _currentPresetId = prefs.getString("preset_id", "cyan") ?: "cyan"
        themeConfig = ThemeConfig(
            isDarkMode   = prefs.getBoolean("dark_mode", true),
            cornerRadius = prefs.getInt("corner_radius", 12),
            fontFamily   = prefs.getString("font_family", "default") ?: "default"
        )
        val navOrder = prefs.getString("nav_order", null)?.split(",")?.filter { it.isNotBlank() }
            ?: listOf("food", "today", "logs", "menu")
        val foodOrder = prefs.getString("food_order", null)?.split(",")?.filter { it.isNotBlank() }
            ?: listOf("quick_add", "new_product", "search_bar", "category_chips", "products_grid")
        layoutConfig = LayoutConfig(navigationOrder = navOrder, foodScreenOrder = foodOrder)

        val n = prefs.getInt("custom_count", 0)
        customPresets.clear()
        repeat(n) { i ->
            val id    = prefs.getString("custom_${i}_id",   null) ?: return@repeat
            val name  = prefs.getString("custom_${i}_name", "Custom") ?: "Custom"
            val colorVal = prefs.getLong("custom_${i}_color", 0xFF00E5FF)
            customPresets.add(AppThemePreset(id, name, Color(colorVal.toULong()), false))
        }
    }
}

