package com.example.macrotrainingtracker.domain.model

enum class PhaseType(val label: String, val description: String, val emoji: String) {
    CUT("Redukcja", "Chcę schudnąć – deficyt kaloryczny", "✂️"),
    MAINTENANCE("Utrzymanie", "Chcę utrzymać wagę i kondycję", "⚖️"),
    BULK("Masa", "Chcę zyskać masę mięśniową – nadwyżka kaloryczna", "💪")
}

