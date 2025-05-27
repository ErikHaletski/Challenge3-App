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
        // -------------------------
        // 🔧 TESTWERTE (später ersetzen)
        // -------------------------
        val stats = mapOf(
            "arm" to 22,
            "brust" to 16,
            "ruecken" to 12,
            "bauch" to 10,
            "bein" to 25,
            "ausdauer" to 35,
            "quests" to 12,
            "progress" to 55,
            "lesen" to 45,
            "artikel" to 6,
            "vokabeln" to 120
        )

        /*
        // ✅ BEREIT FÜR ECHTE STAT-KLASSE:
        val stats = StatProvider.getCurrentStats().let { stats ->
            mapOf(
                "arm" to stats.arm,
                "brust" to stats.brust,
                "ruecken" to stats.ruecken,
                "bauch" to stats.bauch,
                "bein" to stats.bein,
                "ausdauer" to stats.ausdauer,
                "quests" to stats.questCount,
                "progress" to stats.progressPercent,
                "lesen" to stats.pagesRead,
                "artikel" to stats.articlesRead,
                "vokabeln" to stats.vocabularyLearned
            )
        }
        */

        // -------------------------
        // 🔢 SORTIERUNG DER KATEGORIEN
        // -------------------------
        val priority = listOf("Completion", "Allgemein", "Stärke", "Ausdauer", "Intelligenz")

        // -------------------------
        // 🏆 ACHIEVEMENT-DEFINITIONEN
        // -------------------------
        val allAchievements = listOf(
            // --- Completion ---
            Achievement("progress25", "25% Fortschritt erreicht", "Ein Viertel ist geschafft!", "Completion", "Fortschritt"),
            Achievement("progress50", "50% Fortschritt erreicht", "Halbzeit!", "Completion", "Fortschritt"),
            Achievement("progress75", "75% Fortschritt erreicht", "Fast geschafft!", "Completion", "Fortschritt"),
            Achievement("progress100", "100% Fortschritt erreicht", "Du hast alles abgeschlossen!", "Completion", "Fortschritt"),

            // --- Allgemein ---
            Achievement("firstQuest", "Erste Quest abgeschlossen", "Der Anfang deiner Reise!", "Allgemein", "Allgemein"),
            Achievement("tenQuests", "10 Quests abgeschlossen", "Starke Leistung!", "Allgemein", "Allgemein"),
            Achievement("fiftyQuests", "50 Quests abgeschlossen", "Du bist ein Quest-Veteran!", "Allgemein", "Allgemein"),

            // --- Stärke: Arm, Brust, Rücken, Bauch, Bein ---
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

            // --- Ausdauer ---
            Achievement("ausdauer10", "Ausdauer 10 erreicht", "Erste Ausdauer-Level freigeschaltet", "Ausdauer", "Ausdauer"),
            Achievement("ausdauer20", "Ausdauer 20 erreicht", "Halbmarathonfähig!", "Ausdauer", "Ausdauer"),
            Achievement("ausdauer40", "Ausdauer 40 erreicht", "Läuft bei dir – im wahrsten Sinne!", "Ausdauer", "Ausdauer"),
            Achievement("ausdauer60", "Ausdauer 60 erreicht", "Unendliche Energie!", "Ausdauer", "Ausdauer"),

            // --- Intelligenz ---
            Achievement("lesen10", "10 Seiten gelesen", "Du hast 10 Seiten gelesen. Weiter so!", "Intelligenz", "Lesen"),
            Achievement("lesen50", "50 Seiten gelesen", "Wissenshungrig und motiviert!", "Intelligenz", "Lesen"),
            Achievement("lesen100", "100 Seiten gelesen", "Du verschlingst Bücher!", "Intelligenz", "Lesen"),

            Achievement("intelligenz10", "Intellignez 10 erreicht", "Du kannst schon weit denken!", "Intelligenz", "Intelligenz"),
            Achievement("intelligenz20", "Intellignez 20 erreicht", "Du bist informiert!", "Intelligenz", "Intelligenz"),
            Achievement("intelligenz30", "Intellignez 30 erreicht", "Du entwickelst dein Wissen weiter!", "Intelligenz", "Intelligenz"),

            Achievement("vokabel50", "50 Vokabeln gelernt", "Der Anfang deiner Sprachreise!", "Intelligenz", "Vokabeln"),
            Achievement("vokabel100", "100 Vokabeln gelernt", "Wortgewandt!", "Intelligenz", "Vokabeln"),
            Achievement("vokabel200", "200 Vokabeln gelernt", "Sprachgenie in Arbeit!", "Intelligenz", "Vokabeln")
        )

        // -------------------------
        // ✅ ACHIEVEMENT-FREISCHALTUNG
        // -------------------------
        val statCheck = { key: String, value: Int -> (stats[key] ?: 0) >= value }

        val unlocked = allAchievements.map { achievement ->
            when (achievement.id) {
                // Stärke
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

                // Ausdauer
                "ausdauer10" -> achievement.copy(unlocked = statCheck("ausdauer", 10))
                "ausdauer20" -> achievement.copy(unlocked = statCheck("ausdauer", 20))
                "ausdauer40" -> achievement.copy(unlocked = statCheck("ausdauer", 40))
                "ausdauer60" -> achievement.copy(unlocked = statCheck("ausdauer", 60))

                // Intelligenz – Lesen
                "lesen10" -> achievement.copy(unlocked = statCheck("lesen", 10))
                "lesen50" -> achievement.copy(unlocked = statCheck("lesen", 50))
                "lesen100" -> achievement.copy(unlocked = statCheck("lesen", 100))

                // Intelligenz – Artikel
                "intelligenz10" -> achievement.copy(unlocked = statCheck("intelligenz", 10))
                "intelligenz20" -> achievement.copy(unlocked = statCheck("intelligenz", 20))
                "intelligenz30" -> achievement.copy(unlocked = statCheck("intelligenz", 30))

                // Intelligenz – Vokabeln
                "vokabel50" -> achievement.copy(unlocked = statCheck("vokabeln", 50))
                "vokabel100" -> achievement.copy(unlocked = statCheck("vokabeln", 100))
                "vokabel200" -> achievement.copy(unlocked = statCheck("vokabeln", 200))

                // Allgemein
                "tenQuests" -> achievement.copy(unlocked = statCheck("quests", 10))
                "fiftyQuests" -> achievement.copy(unlocked = statCheck("quests", 50))
                "firstQuest" -> achievement.copy(unlocked = true)

                // Completion
                "progress25" -> achievement.copy(unlocked = statCheck("progress", 25))
                "progress50" -> achievement.copy(unlocked = statCheck("progress", 50))
                "progress75" -> achievement.copy(unlocked = statCheck("progress", 75))
                "progress100" -> achievement.copy(unlocked = statCheck("progress", 100))

                else -> achievement
            }
        }

        _achievements.value = unlocked.sortedWith(
            compareBy(
                { priority.indexOf(it.mainCategory) },
                { it.category },
                { it.title }
            )
        )
    }
}
