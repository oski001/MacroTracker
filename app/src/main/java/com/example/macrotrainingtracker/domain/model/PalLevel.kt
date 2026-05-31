package com.example.macrotrainingtracker.domain.model

enum class PalLevel(
    val multiplier: Double,
    val label: String,
    val shortDesc: String,
    val detailedDesc: String
) {
    SEDENTARY(
        1.2,
        "Brak aktywności (1.2)",
        "Praca siedząca, poniżej 5 000 kroków/dzień",
        "Treningi: 0 razy w tygodniu (0 godzin).\n\nŻycie codzienne: praca siedząca (biurko, auto, kanapa), poniżej 5 000 kroków dziennie."
    ),
    LIGHTLY_ACTIVE(
        1.375,
        "Niska aktywność (1.375)",
        "1–3 treningi/tydzień, łącznie 1–3 godziny",
        "Treningi: 1–3 razy w tygodniu.\nCzas: łącznie 1–3 godziny aktywności w tygodniu (sesje 45–60 minut).\n\nJak zaliczyć trening siłowy: Chodzisz na siłownię 2–3 razy w tygodniu na około godzinę, ale pozostałą część dnia spędzasz całkowicie na siedząco (biuro/dom) i robisz mało kroków (poniżej 7 000 dziennie)."
    ),
    MODERATELY_ACTIVE(
        1.55,
        "Średnia aktywność (1.55)",
        "3–5 treningów/tydzień, łącznie 3–7.5 godziny",
        "Treningi: 3–5 razy w tygodniu.\nCzas: łącznie 3–7.5 godziny aktywności w tygodniu (sesje 60–90 minut).\n\nOpcja A: Trenujesz siłowo 4–5× po 60–75 min, masz pracę siedzącą, ale dbasz o spacery (ok. 8 000–10 000 kroków/dzień).\nOpcja B: Trenujesz siłowo tylko 3× w tygodniu, ale masz pracę stojącą/chodzącą (fryzjer, fizjoterapeuta, sprzedawca)."
    ),
    VERY_ACTIVE(
        1.725,
        "Wysoka aktywność (1.725)",
        "6–7 treningów/tydzień, praktycznie codziennie",
        "Treningi: 6–7 razy w tygodniu (praktycznie codziennie).\nCzas: łącznie 6–10.5 godziny ciężkich treningów w tygodniu (sesje 60–90 minut).\n\nOpcja A: Plan Push/Pull/Legs 2× (6 dni × 75–90 min) + wysoka aktywność codzienna (ponad 10 000 kroków).\nOpcja B: 4× siłownia po 70 min + praca fizyczna (budowlanka, magazyn, kurier)."
    ),
    EXTREMELY_ACTIVE(
        1.9,
        "Bardzo wysoka aktywność (1.9)",
        "2× dziennie lub 12–20+ godzin/tydzień ekstremalnego wysiłku",
        "Treningi: Często 2× dziennie lub min. 7×/tydzień na ekstremalnej intensywności.\nCzas: łącznie 12–20+ godzin wysiłku w tygodniu (sesje 90–120 min lub dwie krótsze dziennie).\n\nSamo dźwiganie rzadko pozwala wejść na ten poziom. Osiągają go osoby łączące codzienny ciężki trening siłowy (90 min) z wycieńczającym cardio CODZIENNIE (zawodowi sportowcy MMA, crossfiterzy w przygotowawczym) LUB pracujące w kopalni/przy wyrębie lasu i po 8 h takiej pracy idące jeszcze na siłownię."
    )
}

