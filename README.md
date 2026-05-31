# MacroTrainingTracker 🥗

Aplikacja Android do śledzenia makroskładników i planowania żywienia.
Zbudowana w **Kotlin + Jetpack Compose + Room**.

---

## Funkcje

### 3 zakładki
| Zakładka | Opis |
|---|---|
| **Żywienie** | Dzisiejsze spożycie, biblioteka produktów, Quick Add |
| **Historia** | Miesięczny kalendarz z zalogowanymi dniami |
| **Menu** | Zarządzanie planami, parametry ciała, licznik dni |

---

### Onboarding (3 kroki)
1. **Wybierz fazę** – Cut ✂️ / Utrzymanie ⚖️ / Masa 💪 + nazwa planu
2. **Dane ciała** – płeć, wiek, wzrost, waga, waga docelowa, poziom PAL (5 stopni z dialogiem informacyjnym), deficyt/nadwyżka 300–800 kcal (suwak)
3. **Podsumowanie** – BMI, BF%, LBM, BMR, TDEE, cel kaloryczny, makroskładniki/dzień, czas do celu

---

### Model kalkulacyjny
```
BMI = waga / (wzrost_m)²
BF% = 1.20 × BMI + 0.23 × wiek − (10.8 jeśli M) − 5.4
LBM = waga × (1 − BF%/100)
BMR = Mifflin-St Jeor
TDEE = BMR × PAL

CUT:         kcal = TDEE − deficyt   | białko = waga×2.2g | tłuszcze = waga×0.8g
MAINTENANCE: kcal = TDEE             | białko = waga×2.0g | tłuszcze = waga×1.0g
BULK:        kcal = TDEE + nadwyżka  | białko = waga×2.0g | tłuszcze = waga×1.1g
węglowodany = (kcal − białko×4 − tłuszcze×9) / 4
```

---

### Quick Add
Użytkownik podaje białko / tłuszcze / węglowodany → kalorie liczone automatycznie:
`kalorie = białko×4 + tłuszcze×9 + węgle×4`

---

### Historia
- Kalendarz miesięczny z nawigacją ◀ ▶
- Dni z logami – zielone; bez logów – szare
- Kliknięcie dnia → lista posiłków, edycja ilości, usuwanie, dodawanie

---

### Menu
- Lista planów, aktywacja/usunięcie, tworzenie nowego planu
- Edycja parametrów ciała
- Globalny licznik dni od startu (z możliwością restartu)
- Przypomnienie o przeliczeniu co 30 dni (odkładanie na 7 dni)

---

## Architektura

```
data/local/   → Room: FoodProductEntity, FoodLogEntity, UserBodyEntity, NutritionPlanEntity
data/repository/ → TrackerRepository + Impl
domain/       → PalLevel, PhaseType, NutritionResult, CalculateNutritionTargetsUseCase
ui/screens/   → OnboardingScreen, FoodTrackerScreen, FoodLogScreen, SettingsScreen
ui/viewmodel/ → TrackerViewModel (StateFlow + Coroutines)
```

## Technologie
Kotlin · Jetpack Compose (Material3) · Room · Coroutines · StateFlow · Navigation Compose
