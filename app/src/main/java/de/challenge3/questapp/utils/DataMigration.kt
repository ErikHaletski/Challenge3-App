package de.challenge3.questapp.utils

import android.content.Context
import de.challenge3.questapp.repository.FirebaseQuestRepository
import de.challenge3.questapp.repository.FirebaseFriendRepository
import de.challenge3.questapp.ui.home.QuestCompletion
import de.challenge3.questapp.ui.home.QuestTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataMigration(private val context: Context) {

    fun migrateSampleDataToFirebase() {
        val questRepository = FirebaseQuestRepository()
        val friendRepository = FirebaseFriendRepository(context)
        val currentUserId = friendRepository.getCurrentUserId()

        val sampleQuests = getSampleQuests(currentUserId)

        CoroutineScope(Dispatchers.IO).launch {
            sampleQuests.forEach { quest ->
                questRepository.addCompletedQuest(quest)
            }
        }
    }

    private fun getSampleQuests(currentUserId: String) = listOf(
        // Current user's quests
        QuestCompletion(
            id = "",
            lat = 52.5200, lng = 13.4050,
            timestamp = System.currentTimeMillis(),
            questText = "Defeat the Goblin King",
            tag = QuestTag.MIGHT,
            experiencePoints = 150,
            userId = currentUserId,
            username = getUsernameForId(currentUserId)
        ),
        QuestCompletion(
            id = "",
            lat = 51.5074, lng = -0.1278,
            timestamp = System.currentTimeMillis() - 7200000,
            questText = "Heal the wounded traveler",
            tag = QuestTag.HEART,
            experiencePoints = 75,
            userId = currentUserId,
            username = getUsernameForId(currentUserId)
        ),
        // Add some quests for other users too (so friends can see them)
        QuestCompletion(
            id = "",
            lat = 48.8566, lng = 2.3522,
            timestamp = System.currentTimeMillis() - 3600000,
            questText = "Solve the ancient riddle",
            tag = QuestTag.MIND,
            experiencePoints = 100,
            userId = "user_041d41b3-465a-4d50-bff7-5b3ab78096fa",
            username = "User_5072a1"
        ),
        QuestCompletion(
            id = "",
            lat = 54.5074, lng = -0.7278,
            timestamp = System.currentTimeMillis() - 5000000,
            questText = "Meditate for 10 minutes",
            tag = QuestTag.SPIRIT,
            experiencePoints = 75,
            userId = "user_347a011a-fe8a-4f52-87a3-51bdd25b143c",
            username = "User_49d22a"
        )
    )

    private fun getUsernameForId(userId: String): String {
        return when (userId) {
            "user_9ac93079-2cc2-4ca2-b77e-3025211b8127" -> "User_ac69ea"
            "user_041d41b3-465a-4d50-bff7-5b3ab78096fa" -> "User_5072a1"
            "user_347a011a-fe8a-4f52-87a3-51bdd25b143c" -> "User_49d22a"
            else -> "Unknown User"
        }
    }
}
