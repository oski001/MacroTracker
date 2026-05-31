package com.example.macrotrainingtracker.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.macrotrainingtracker.ui.screens.FoodLogScreen
import com.example.macrotrainingtracker.ui.screens.FoodTrackerScreen
import com.example.macrotrainingtracker.ui.screens.OnboardingScreen
import com.example.macrotrainingtracker.ui.screens.SettingsScreen
import com.example.macrotrainingtracker.ui.screens.TodayScreen
import com.example.macrotrainingtracker.ui.theme.AccentPrimary
import com.example.macrotrainingtracker.ui.theme.ThemeManager
import com.example.macrotrainingtracker.ui.viewmodel.TrackerViewModel

@Composable
fun MacroTrainingApp(viewModel: TrackerViewModel, context: Context) {
    val navController = rememberNavController()
    val isOnboarded by viewModel.isOnboarded.collectAsState()

    if (!isOnboarded) {
        OnboardingScreen(onSave = viewModel::saveSetup)
        return
    }

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: MainTab.FOOD.route

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                ThemeManager.navigationTabs.forEach { tab ->
                    val route = tab.id
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(MainTab.FOOD.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (tab.id) {
                                    "food"  -> Icons.Default.Search
                                    "today" -> Icons.Default.Home
                                    "logs"  -> Icons.Default.DateRange
                                    "menu"  -> Icons.Default.Menu
                                    else    -> Icons.Default.Home
                                },
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.FOOD.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(MainTab.FOOD.route) {
                val state by viewModel.foodTrackerState.collectAsState()
                FoodTrackerScreen(
                    state = state,
                    onAddFromProduct = { product, grams -> viewModel.addFoodFromProduct(product, grams) },
                    onDeleteFood = viewModel::deleteFoodLog,
                    onQuickAdd = { name, p, f, c -> viewModel.quickAddFood(today = true, name = name, proteinG = p, fatsG = f, carbsG = c) },
                    onAddCustomProduct = viewModel::addCustomProduct,
                    onDeleteCustomProduct = viewModel::deleteCustomProduct,
                    onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) },
                    onSnoozeRecalcReminder = viewModel::snoozeRecalcReminder
                )
            }
            composable(MainTab.TODAY.route) {
                val state by viewModel.foodTrackerState.collectAsState()
                TodayScreen(
                    state = state,
                    onDeleteFood = viewModel::deleteFoodLog,
                    onUpdateFood = viewModel::updateFoodLog,
                    onQuickAdd = { name, p, f, c -> viewModel.quickAddFood(today = true, name = name, proteinG = p, fatsG = f, carbsG = c) },
                    onSnoozeRecalcReminder = viewModel::snoozeRecalcReminder
                )
            }
            composable(MainTab.LOGS.route) {
                val state by viewModel.logsUiState.collectAsState()
                val products by viewModel.foodTrackerState.collectAsState()
                FoodLogScreen(
                    state = state,
                    products = products.products,
                    onSelectDate = viewModel::selectLogDate,
                    onDeleteLog = viewModel::deleteFoodLog,
                    onUpdateLog = viewModel::updateFoodLog,
                    onQuickAdd = { date, name, p, f, c -> viewModel.quickAddFood(date, name, p, f, c) },
                    onAddFromProduct = { product, grams, date -> viewModel.addFoodFromProduct(product, grams, date) }
                )
            }
            composable(MainTab.MENU.route) {
                val state by viewModel.menuUiState.collectAsState()
                SettingsScreen(
                    state = state,
                    onSetActivePlan = viewModel::setActivePlan,
                    onUpdatePlan = viewModel::updatePlan,
                    onDeletePlan = viewModel::deletePlan,
                    onAddNewPlan = viewModel::addNewPlan,
                    onUpdateBody = viewModel::updateBody,
                    onRestartDayCounter = viewModel::restartDayCounter,
                    context = context
                )
            }
        }
    }
}

// helper used inline in navigation
private fun TrackerViewModel.quickAddFood(today: Boolean = false, date: String = "", name: String, proteinG: Int, fatsG: Int, carbsG: Int) {
    val d = if (today) java.time.LocalDate.now().toString() else date
    quickAddFood(d, name, proteinG, fatsG, carbsG)
}
