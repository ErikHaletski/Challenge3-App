package de.challenge3.questapp.utils

import de.challenge3.questapp.repository.FirebaseQuestRepository
import de.challenge3.questapp.ui.home.QuestCompletion
import de.challenge3.questapp.ui.home.QuestTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataMigration {

    fun migrateSampleDataToFirebase() {
        val repository = FirebaseQuestRepository()
        val sampleQuests = getSampleQuests()

        CoroutineScope(Dispatchers.IO).launch {
            sampleQuests.forEach { quest ->
                repository.addCompletedQuest(quest)
            }
        }
    }

    private fun getSampleQuests() = listOf(
        QuestCompletion(
            id = "", // Let Firebase generate the ID
            lat = 52.5200, lng = 13.4050,
            timestamp = System.currentTimeMillis(),
            questText = "Defeat the Goblin King",
            tag = QuestTag.MIGHT,
            experiencePoints = 150
        ),
        QuestCompletion(
            id = "",
            lat = 48.8566, lng = 2.3522,
            timestamp = System.currentTimeMillis() - 3600000,
            questText = "Solve the ancient riddle",
            tag = QuestTag.MIND,
            experiencePoints = 100
        ),
        QuestCompletion(
            id = "",
            lat = 51.5074, lng = -0.1278,
            timestamp = System.currentTimeMillis() - 7200000,
            questText = "Heal the wounded traveler",
            tag = QuestTag.HEART,
            experiencePoints = 75
        ),
        QuestCompletion(
            id = "",
            lat = 54.5074, lng = -0.7278,
            timestamp = System.currentTimeMillis() - 5000000,
            questText = "Meditate for 10 minutes",
            tag = QuestTag.SPIRIT,
            experiencePoints = 75
        )
    )
}