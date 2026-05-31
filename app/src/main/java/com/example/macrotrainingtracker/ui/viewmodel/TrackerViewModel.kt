package com.example.macrotrainingtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.macrotrainingtracker.data.local.entity.FoodLogEntity
import com.example.macrotrainingtracker.data.local.entity.FoodProductEntity
import com.example.macrotrainingtracker.data.local.entity.NutritionPlanEntity
import com.example.macrotrainingtracker.data.local.entity.UserBodyEntity
import com.example.macrotrainingtracker.data.repository.TrackerRepository
import com.example.macrotrainingtracker.domain.model.Gender
import com.example.macrotrainingtracker.domain.model.PalLevel
import com.example.macrotrainingtracker.domain.model.PhaseType
import com.example.macrotrainingtracker.domain.usecase.CalculateNutritionTargetsUseCase
import com.example.macrotrainingtracker.ui.model.FoodTrackerUiState
import com.example.macrotrainingtracker.ui.model.LogsUiState
import com.example.macrotrainingtracker.ui.model.MenuUiState
import com.example.macrotrainingtracker.ui.model.OnboardingInput
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerViewModel(
    private val repository: TrackerRepository,
    private val calculateNutritionTargetsUseCase: CalculateNutritionTargetsUseCase
) : ViewModel() {

    private val todayFlow = MutableStateFlow(today())
    private val _selectedLogDate = MutableStateFlow<String?>(null)

    // ─── Core streams ────────────────────────────────────────────────────────
    private val bodyFlow = repository.observeBody()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val activePlanFlow = repository.observeActivePlan()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val plansFlow = repository.observeAllPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ─── Onboarded flag ──────────────────────────────────────────────────────
    val isOnboarded: StateFlow<Boolean> = combine(bodyFlow, activePlanFlow) { body, plan ->
        body != null && plan != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // ─── Food Tracker (today) ─────────────────────────────────────────────────
    val foodTrackerState: StateFlow<FoodTrackerUiState> = combine(
        repository.observeFoodProducts(),
        todayFlow.flatMapLatest { repository.observeDailyFoodLogs(it) },
        activePlanFlow,
        bodyFlow
    ) { products, logs, plan, body ->
        val daysSince = if (body != null) calculateDaysSinceStart(body.dayStartedAt) else 0
        val reminder = if (body != null) shouldRemind(body) else false
        FoodTrackerUiState(
            products = products,
            todayLogs = logs,
            activePlan = plan,
            daysSinceStart = daysSince,
            shouldShowRecalcReminder = reminder
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FoodTrackerUiState())

    // ─── Logs (calendar) ─────────────────────────────────────────────────────
    val logsUiState: StateFlow<LogsUiState> = combine(
        repository.observeAllLoggedDates(),
        _selectedLogDate.flatMapLatest { date ->
            if (date != null) repository.observeDailyFoodLogs(date) else flowOf(emptyList())
        }
    ) { dates, selectedLogs ->
        LogsUiState(allLoggedDates = dates.toSet(), selectedDateLogs = selectedLogs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LogsUiState())

    // ─── Menu ─────────────────────────────────────────────────────────────────
    val menuUiState: StateFlow<MenuUiState> = combine(bodyFlow, plansFlow, activePlanFlow) { body, plans, active ->
        MenuUiState(
            body = body,
            plans = plans,
            activePlan = active,
            daysSinceStart = if (body != null) calculateDaysSinceStart(body.dayStartedAt) else 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MenuUiState())

    // ─── Onboarding / Setup ───────────────────────────────────────────────────
    fun saveSetup(input: OnboardingInput) {
        val age    = input.age.toIntOrNull() ?: return
        val height = input.heightCm.toIntOrNull() ?: return
        val weight = input.weightKg.toDoubleOrNull() ?: return
        val target = input.targetWeightKg.toDoubleOrNull() ?: weight
        val modifier = input.modifierKcal.roundToInt()

        val result = calculateNutritionTargetsUseCase.execute(
            gender       = input.gender,
            age          = age,
            heightCm     = height,
            weightKg     = weight,
            targetWeightKg = target,
            palLevel     = input.palLevel,
            phase        = input.phase,
            modifierKcal = modifier
        )

        viewModelScope.launch {
            val existingBody = repository.getBody()
            repository.upsertBody(
                UserBodyEntity(
                    gender    = input.gender.name,
                    age       = age,
                    heightCm  = height,
                    weightKg  = weight,
                    palLevel  = input.palLevel.name,
                    dayStartedAt = existingBody?.dayStartedAt ?: System.currentTimeMillis(),
                    lastRecalcAt = System.currentTimeMillis(),
                    recalcReminderSnoozedUntil = 0L
                )
            )
            repository.insertPlan(
                NutritionPlanEntity(
                    name           = input.planName.ifBlank { "${input.phase.label} – ${input.weightKg}kg→${input.targetWeightKg}kg" },
                    phase          = input.phase.name,
                    targetWeightKg = target,
                    modifierKcal   = modifier,
                    targetCalories = result.targetCalories,
                    targetProtein  = result.proteinGrams,
                    targetFats     = result.fatsGrams,
                    targetCarbs    = result.carbsGrams,
                    daysToGoal     = result.daysToGoal ?: 0,
                    isActive       = true
                )
            )
        }
    }

    fun addNewPlan(input: OnboardingInput) {
        val age    = input.age.toIntOrNull() ?: return
        val height = input.heightCm.toIntOrNull() ?: return
        val weight = input.weightKg.toDoubleOrNull() ?: return
        val target = input.targetWeightKg.toDoubleOrNull() ?: weight
        val modifier = input.modifierKcal.roundToInt()

        val result = calculateNutritionTargetsUseCase.execute(
            gender = input.gender, age = age, heightCm = height,
            weightKg = weight, targetWeightKg = target,
            palLevel = input.palLevel, phase = input.phase,
            modifierKcal = modifier
        )
        viewModelScope.launch {
            repository.insertPlan(
                NutritionPlanEntity(
                    name           = input.planName.ifBlank { input.phase.label },
                    phase          = input.phase.name,
                    targetWeightKg = target,
                    modifierKcal   = modifier,
                    targetCalories = result.targetCalories,
                    targetProtein  = result.proteinGrams,
                    targetFats     = result.fatsGrams,
                    targetCarbs    = result.carbsGrams,
                    daysToGoal     = result.daysToGoal ?: 0,
                    isActive       = false
                )
            )
        }
    }

    fun setActivePlan(id: Long) {
        viewModelScope.launch { repository.setActivePlan(id) }
    }

    fun updatePlan(plan: NutritionPlanEntity) {
        viewModelScope.launch { repository.updatePlan(plan) }
    }

    fun deletePlan(id: Long) {
        viewModelScope.launch { repository.deletePlan(id) }
    }

    fun updateBody(input: OnboardingInput) {
        val age    = input.age.toIntOrNull() ?: return
        val height = input.heightCm.toIntOrNull() ?: return
        val weight = input.weightKg.toDoubleOrNull() ?: return
        viewModelScope.launch {
            val existing = repository.getBody() ?: return@launch
            repository.upsertBody(
                existing.copy(
                    gender   = input.gender.name,
                    age      = age,
                    heightCm = height,
                    weightKg = weight,
                    palLevel = input.palLevel.name
                )
            )
        }
    }

    // ─── Food logging ─────────────────────────────────────────────────────────
    fun addFoodFromProduct(product: FoodProductEntity, grams: Double, date: String = today()) {
        if (grams <= 0.0) return
        viewModelScope.launch {
            repository.addFoodLog(FoodLogEntity(
                date       = date,
                timestamp  = System.currentTimeMillis(),
                sourceName = product.name,
                grams      = grams,
                calories   = (product.caloriesPerGram * grams).roundToInt(),
                protein    = (product.proteinPerGram * grams).roundToInt(),
                carbs      = (product.carbsPerGram * grams).roundToInt(),
                fats       = (product.fatsPerGram * grams).roundToInt()
            ))
        }
    }

    fun quickAddFood(date: String, name: String, proteinG: Int, fatsG: Int, carbsG: Int) {
        val calories = proteinG * 4 + fatsG * 9 + carbsG * 4
        viewModelScope.launch {
            repository.addFoodLog(FoodLogEntity(
                date       = date,
                timestamp  = System.currentTimeMillis(),
                sourceName = name.ifBlank { "Quick Add" },
                grams      = 0.0,
                calories   = calories,
                protein    = proteinG,
                carbs      = carbsG,
                fats       = fatsG
            ))
        }
    }

    fun updateFoodLog(log: FoodLogEntity) {
        viewModelScope.launch { repository.updateFoodLog(log) }
    }

    fun deleteFoodLog(id: Long) {
        viewModelScope.launch { repository.deleteFoodLog(id) }
    }

    // ─── Custom products ──────────────────────────────────────────────────────
    fun addCustomProduct(product: FoodProductEntity) {
        viewModelScope.launch { repository.addFoodProduct(product.copy(isCustom = true)) }
    }

    fun deleteCustomProduct(id: Long) {
        viewModelScope.launch { repository.deleteCustomFoodProduct(id) }
    }

    fun toggleFavorite(id: Long, currentValue: Boolean) {
        viewModelScope.launch { repository.setFavorite(id, !currentValue) }
    }

    // ─── Logs calendar selection ──────────────────────────────────────────────
    fun selectLogDate(date: String?) { _selectedLogDate.value = date }

    // ─── Settings ────────────────────────────────────────────────────────────
    fun restartDayCounter() {
        viewModelScope.launch {
            val body = repository.getBody() ?: return@launch
            repository.upsertBody(body.copy(dayStartedAt = System.currentTimeMillis()))
        }
    }

    fun snoozeRecalcReminder() {
        viewModelScope.launch {
            val body = repository.getBody() ?: return@launch
            val sevenDays = 7L * 24 * 60 * 60 * 1000
            repository.upsertBody(body.copy(recalcReminderSnoozedUntil = System.currentTimeMillis() + sevenDays))
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    private fun calculateDaysSinceStart(startedAt: Long): Int {
        val startDate = Instant.ofEpochMilli(startedAt).atZone(ZoneId.systemDefault()).toLocalDate()
        return ChronoUnit.DAYS.between(startDate, LocalDate.now()).toInt() + 1
    }

    private fun shouldRemind(body: UserBodyEntity): Boolean {
        val daysSince = ChronoUnit.DAYS.between(
            Instant.ofEpochMilli(body.lastRecalcAt).atZone(ZoneId.systemDefault()).toLocalDate(),
            LocalDate.now()
        )
        return daysSince >= 30 && System.currentTimeMillis() >= body.recalcReminderSnoozedUntil
    }

    private fun today(): String = LocalDate.now().toString()

    // ─── Factory ─────────────────────────────────────────────────────────────
    class Factory(
        private val repository: TrackerRepository,
        private val calculateNutritionTargetsUseCase: CalculateNutritionTargetsUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TrackerViewModel(repository, calculateNutritionTargetsUseCase) as T
    }
}
