package de.challenge3.questapp.ui.achievements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val mainCategory: String,
    val category: String,
    val unlocked: Boolean = false
)

class AchievmentsViewModel : ViewModel() {

    private val _achievements = MutableLiveData<List<Achievement>>()
    val achievements: LiveData<List<Achievement>> = _achievements

    init {
        val externalStats = mapOf(
            "arm" to 22,
            "brust" to 16,
            "ruecken" to 12,
            "bauch" to 10,
            "bein" to 25,
            "ausdauer" to 35,
            "quests" to 12,
            "progress" to 55
        )

        val priority = listOf("Completion", "Allgemein", "Stärke", "Ausdauer")

        val allAchievements = listOf(
            // Completion
            Achievement("progress25", "25% Fortschritt erreicht", "Ein Viertel ist geschafft!", "Completion", "Fortschritt"),
            Achievement("progress50", "50% Fortschritt erreicht", "Halbzeit!", "Completion", "Fortschritt"),
            Achievement("progress75", "75% Fortschritt erreicht", "Fast geschafft!", "Completion", "Fortschritt"),
            Achievement("progress100", "100% Fortschritt erreicht", "Du hast alles abgeschlossen!", "Completion", "Fortschritt"),

            // Allgemein
            Achievement("firstQuest", "Erste Quest abgeschlossen", "Der Anfang deiner Reise!", "Allgemein", "Allgemein"),
            Achievement("tenQuests", "10 Quests abgeschlossen", "Starke Leistung!", "Allgemein", "Allgemein"),
            Achievement("fiftyQuests", "50 Quests abgeschlossen", "Du bist ein Quest-Veteran!", "Allgemein", "Allgemein"),

            // Stärke
            Achievement("arm10", "Armstärke 10 erreicht", "Starke Arme – weiter so!", "Stärke", "Arm"),
            Achievement("arm20", "Armstärke 20 erreicht", "Du kannst jetzt ordentlich drücken!", "Stärke", "Arm"),
            Achievement("arm50", "Armstärke 50 erreicht", "Du bist ein Arm-Titan!", "Stärke", "Arm"),

            Achievement("brust5", "Bruststärke 5 erreicht", "Erste Fortschritte im Brustbereich", "Stärke", "Brust"),
            Achievement("brust15", "Bruststärke 15 erreicht", "Solide Brustleistung!", "Stärke", "Brust"),
            Achievement("brust30", "Bruststärke 30 erreicht", "Du bist ein Benchpress-Boss!", "Stärke", "Brust"),

            Achievement("ruecken5", "Rückenstärke 5 erreicht", "Guter Rücken ist Gold wert", "Stärke", "Rücken"),
            Achievement("ruecken15", "Rückenstärke 15 erreicht", "Kräftiger Rücken!", "Stärke", "Rücken"),
            Achievement("ruecken25", "Rückenstärke 25 erreicht", "Unaufhaltbarer Rückenpanzer!", "Stärke", "Rücken"),

            Achievement("bauch5", "Bauchstärke 5 erreicht", "Core-Training zahlt sich aus!", "Stärke", "Bauch"),
            Achievement("bauch10", "Bauchstärke 10 erreicht", "Der Sixpack kommt!", "Stärke", "Bauch"),
            Achievement("bauch25", "Bauchstärke 25 erreicht", "Bauch aus Granit!", "Stärke", "Bauch"),

            Achievement("bein5", "Beinstärke 5 erreicht", "Nicht das Beintraining skippen!", "Stärke", "Bein"),
            Achievement("bein15", "Beinstärke 15 erreicht", "Du trittst ordentlich zu!", "Stärke", "Bein"),
            Achievement("bein30", "Beinstärke 30 erreicht", "Bein wie Baumstämme!", "Stärke", "Bein"),

            // Ausdauer
            Achievement("ausdauer10", "Ausdauer 10 erreicht", "Erste Ausdauer-Level freigeschaltet", "Ausdauer", "Ausdauer"),
            Achievement("ausdauer20", "Ausdauer 20 erreicht", "Halbmarathonfähig!", "Ausdauer", "Ausdauer"),
            Achievement("ausdauer40", "Ausdauer 40 erreicht", "Läuft bei dir – im wahrsten Sinne!", "Ausdauer", "Ausdauer"),
            Achievement("ausdauer60", "Ausdauer 60 erreicht", "Unendliche Energie!", "Ausdauer", "Ausdauer")
        )

        val statCheck = { key: String, value: Int -> (externalStats[key] ?: 0) >= value }

        val unlocked = allAchievements.map { achievement ->
            when (achievement.id) {
                "arm10" -> achievement.copy(unlocked = statCheck("arm", 10))
                "arm20" -> achievement.copy(unlocked = statCheck("arm", 20))
                "arm50" -> achievement.copy(unlocked = statCheck("arm", 50))

                "brust5" -> achievement.copy(unlocked = statCheck("brust", 5))
                "brust15" -> achievement.copy(unlocked = statCheck("brust", 15))
                "brust30" -> achievement.copy(unlocked = statCheck("brust", 30))

                "ruecken5" -> achievement.copy(unlocked = statCheck("ruecken", 5))
                "ruecken15" -> achievement.copy(unlocked = statCheck("ruecken", 15))
                "ruecken25" -> achievement.copy(unlocked = statCheck("ruecken", 25))

                "bauch5" -> achievement.copy(unlocked = statCheck("bauch", 5))
                "bauch10" -> achievement.copy(unlocked = statCheck("bauch", 10))
                "bauch25" -> achievement.copy(unlocked = statCheck("bauch", 25))

                "bein5" -> achievement.copy(unlocked = statCheck("bein", 5))
                "bein15" -> achievement.copy(unlocked = statCheck("bein", 15))
                "bein30" -> achievement.copy(unlocked = statCheck("bein", 30))

                "ausdauer10" -> achievement.copy(unlocked = statCheck("ausdauer", 10))
                "ausdauer20" -> achievement.copy(unlocked = statCheck("ausdauer", 20))
                "ausdauer40" -> achievement.copy(unlocked = statCheck("ausdauer", 40))
                "ausdauer60" -> achievement.copy(unlocked = statCheck("ausdauer", 60))

                "tenQuests" -> achievement.copy(unlocked = statCheck("quests", 10))
                "fiftyQuests" -> achievement.copy(unlocked = statCheck("quests", 50))
                "firstQuest" -> achievement.copy(unlocked = true)

                "progress25" -> achievement.copy(unlocked = statCheck("progress", 25))
                "progress50" -> achievement.copy(unlocked = statCheck("progress", 50))
                "progress75" -> achievement.copy(unlocked = statCheck("progress", 75))
                "progress100" -> achievement.copy(unlocked = statCheck("progress", 100))

                else -> achievement
            }
        }

        // Sortiere nach gewünschter Reihenfolge
        _achievements.value = unlocked.sortedWith(
            compareBy(
                { priority.indexOf(it.mainCategory) }, // 1. Sortierung: nach Kategorie-Reihenfolge
                { it.category },                        // 2. Sortierung: alphabetisch innerhalb
                { it.title }                            // 3. Sortierung: nach Titel
            )
        )
    }
}
<<<<<<< HEAD
=======

>>>>>>> feature/achievements
