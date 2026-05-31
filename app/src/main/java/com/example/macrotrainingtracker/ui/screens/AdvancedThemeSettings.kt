package com.example.macrotrainingtracker.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macrotrainingtracker.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun AdvancedThemeSettingsDialog(context: Context, onDismiss: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Kolory", "Wygląd", "Nawigacja")

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp).padding(bottom = 0.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Ustawienia stylu", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Zamknij", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Tab row
                TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = AccentPrimary) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title, fontSize = 12.sp) })
                    }
                }

                // Content
                Box(modifier = Modifier.weight(1f).padding(20.dp).verticalScroll(rememberScrollState())) {
                    when (selectedTab) {
                        0 -> ColorSettingsTab(context)
                        1 -> AppearanceTab(context)
                        2 -> NavigationOrderTab(context)
                    }
                }

                // Bottom button
                Button(onClick = { ThemeManager.saveToPrefs(context); onDismiss() }, modifier = Modifier.fillMaxWidth().padding(20.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = Color.Black)
                ) { Text("GOTOWE", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

// ── Tab 1: Colors ─────────────────────────────────────────────────────────────

@Composable
private fun ColorSettingsTab(context: Context) {
    var showAddCustom  by remember { mutableStateOf(false) }
    var newPresetName  by remember { mutableStateOf("") }
    var newPresetHex   by remember { mutableStateOf("00E5FF") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("WBUDOWANE PRESETY", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), letterSpacing = 1.sp)
        val cols = 4
        ThemeManager.builtInPresets.chunked(cols).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { preset ->
                    ColorPresetChip(preset = preset, isSelected = ThemeManager.currentPreset.id == preset.id, modifier = Modifier.weight(1f)) {
                        ThemeManager.setPreset(preset.id)
                    }
                }
                repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }

        if (ThemeManager.customPresets.isNotEmpty()) {
            Text("WŁASNE PRESETY", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), letterSpacing = 1.sp)
            ThemeManager.customPresets.forEach { preset ->
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)).border(1.dp, if (ThemeManager.currentPreset.id == preset.id) preset.accentColor else MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)).clickable { ThemeManager.setPreset(preset.id) }.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(preset.accentColor))
                        Text(preset.name, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { ThemeManager.removeCustomPreset(preset.id) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        if (showAddCustom) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("NOWY PRESET", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), letterSpacing = 1.sp)
                OutlinedTextField(value = newPresetName, onValueChange = { newPresetName = it }, label = { Text("NAZWA", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("#", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = newPresetHex, onValueChange = { v -> if (v.length <= 6 && v.all { it.isLetterOrDigit() }) newPresetHex = v.uppercase() }, label = { Text("HEX", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface))
                    val previewColor = runCatching { Color(android.graphics.Color.parseColor("#$newPresetHex")) }.getOrElse { Color.Gray }
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(previewColor).border(2.dp, MaterialTheme.colorScheme.outline, CircleShape))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showAddCustom = false; newPresetName = ""; newPresetHex = "00E5FF" }, modifier = Modifier.weight(1f)) { Text("ANULUJ", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Button(onClick = {
                        if (newPresetName.isNotBlank() && newPresetHex.length == 6) {
                            val color = runCatching { Color(android.graphics.Color.parseColor("#$newPresetHex")) }.getOrElse { return@Button }
                            ThemeManager.addCustomPreset(newPresetName, color)
                            showAddCustom = false; newPresetName = ""; newPresetHex = "00E5FF"
                        }
                    }, enabled = newPresetName.isNotBlank() && newPresetHex.length == 6, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = Color.Black)
                    ) { Text("DODAJ", fontWeight = FontWeight.Bold) }
                }
            }
        } else {
            OutlinedButton(onClick = { showAddCustom = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPrimary)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("WŁASNY KOLOR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ColorPresetChip(preset: AppThemePreset, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.clickable { onClick() }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(10.dp)).background(preset.accentColor.copy(alpha = 0.15f))
            .border(2.dp, if (isSelected) preset.accentColor else MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(preset.accentColor))
        }
        Text(preset.name.split(" ").last(), fontSize = 8.sp, color = if (isSelected) preset.accentColor else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

// ── Tab 2: Appearance ─────────────────────────────────────────────────────────

@Composable
private fun AppearanceTab(context: Context) {
    var config by remember { mutableStateOf(ThemeManager.themeConfig) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Dark/Light mode
        Text("MOTYW", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ModeToggleButton(label = "Ciemny", icon = "🌙", isSelected = config.isDarkMode, modifier = Modifier.weight(1f)) {
                config = config.copy(isDarkMode = true)
                ThemeManager.updateThemeConfig(config)
            }
            ModeToggleButton(label = "Jasny", icon = "☀️", isSelected = !config.isDarkMode, modifier = Modifier.weight(1f)) {
                config = config.copy(isDarkMode = false)
                ThemeManager.updateThemeConfig(config)
            }
        }

        // Corner radius
        Text("ZAOKRĄGLENIE ELEMENTÓW", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), letterSpacing = 1.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${config.cornerRadius} dp", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentPrimary)
            Text("(0 = ostre, 30 = mocne)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
        Slider(value = config.cornerRadius.toFloat(), onValueChange = { val r = it.roundToInt(); config = config.copy(cornerRadius = r); ThemeManager.updateThemeConfig(config) },
            valueRange = 0f..30f, steps = 29, modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(thumbColor = AccentPrimary, activeTrackColor = AccentPrimary, inactiveTrackColor = MaterialTheme.colorScheme.outline)
        )
        // Preview
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(config.cornerRadius.dp)).background(AccentPrimary.copy(alpha = 0.3f)).border(1.dp, AccentPrimary, RoundedCornerShape(config.cornerRadius.dp)))
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(config.cornerRadius.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(config.cornerRadius.dp)))
        }
    }
}

@Composable
private fun ModeToggleButton(label: String, icon: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.background(if (isSelected) AccentPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
        .border(1.5.dp, if (isSelected) AccentPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
        .clickable { onClick() }.padding(14.dp), contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(icon, fontSize = 24.sp)
            Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) AccentPrimary else MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ── Tab 3: Navigation Order ──────────────────────────────────────────────────

@Composable
private fun NavigationOrderTab(context: Context) {
    var order by remember { mutableStateOf(ThemeManager.layoutConfig.navigationOrder.toMutableList()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("KOLEJNOŚĆ TABÓW", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), letterSpacing = 1.sp)
        Text("Przeciągnij aby zmienić kolejność", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        order.forEachIndexed { index, tabId ->
            val tab = NavigationTab.entries.find { it.id == tabId } ?: return@forEachIndexed
            ReorderableItem(
                label = tab.label,
                icon = tab.icon,
                canMoveUp = index > 0,
                canMoveDown = index < order.size - 1,
                onMoveUp = {
                    val new = order.toMutableList()
                    new[index] = new[index - 1].also { new[index - 1] = new[index] }
                    order = new
                    ThemeManager.updateLayoutConfig(ThemeManager.layoutConfig.copy(navigationOrder = new))
                },
                onMoveDown = {
                    val new = order.toMutableList()
                    new[index] = new[index + 1].also { new[index + 1] = new[index] }
                    order = new
                    ThemeManager.updateLayoutConfig(ThemeManager.layoutConfig.copy(navigationOrder = new))
                }
            )
        }
    }
}

// ── Reorderable item ──────────────────────────────────────────────────────────

@Composable
private fun ReorderableItem(label: String, icon: String, canMoveUp: Boolean, canMoveDown: Boolean, onMoveUp: () -> Unit, onMoveDown: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            Text(icon, fontSize = 18.sp)
            Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "W górę", tint = if (canMoveUp) AccentPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "W dół", tint = if (canMoveDown) AccentPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
            }
        }
    }
}


