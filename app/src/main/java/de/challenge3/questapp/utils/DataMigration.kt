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

    private fun getSampleQuests(currentUserId: String): List<QuestCompletion> {
        return when (getCurrentUsername()) {
            "User_5072a1" -> listOf(
                QuestCompletion(
                    id = "",
                    lat = 48.8566, lng = 2.3522,
                    timestamp = System.currentTimeMillis() - 3600000,
                    questText = "Q1",
                    tag = QuestTag.MIND,
                    experiencePoints = 100,
                    userId = currentUserId,
                    username = "User_5072a1"
                ),
                QuestCompletion(
                    id = "",
                    lat = 54.5074, lng = -0.7278,
                    timestamp = System.currentTimeMillis() - 5000000,
                    questText = "Q2",
                    tag = QuestTag.SPIRIT,
                    experiencePoints = 75,
                    userId = currentUserId,
                    username = "User_5072a1"
                )
            )
            "User_a9e82e" -> listOf(
                QuestCompletion(
                    id = "",
                    lat = 50.8566, lng = 2.3522,
                    timestamp = System.currentTimeMillis() - 3600000,
                    questText = "Q3",
                    tag = QuestTag.MIND,
                    experiencePoints = 100,
                    userId = currentUserId,
                    username = "User_a9e82e"
                ),
                QuestCompletion(
                    id = "",
                    lat = 53.5074, lng = -0.9278,
                    timestamp = System.currentTimeMillis() - 5000000,
                    questText = "Q4",
                    tag = QuestTag.SPIRIT,
                    experiencePoints = 75,
                    userId = currentUserId,
                    username = "User_a9e82e"
                )
            )
            "User_ac69ea" -> listOf(
                QuestCompletion(
                    id = "",
                    lat = 38.8566, lng = 2.3522,
                    timestamp = System.currentTimeMillis() - 3600000,
                    questText = "Q5",
                    tag = QuestTag.MIND,
                    experiencePoints = 100,
                    userId = currentUserId,
                    username = "User_ac69ea"
                ),
                QuestCompletion(
                    id = "",
                    lat = 44.5074, lng = -0.4278,
                    timestamp = System.currentTimeMillis() - 5000000,
                    questText = "Q6",
                    tag = QuestTag.SPIRIT,
                    experiencePoints = 75,
                    userId = currentUserId,
                    username = "User_ac69ea"
                )
            )
            "User_49d22a" -> listOf(
                QuestCompletion(
                    id = "",
                    lat = 68.8566, lng = 1.3522,
                    timestamp = System.currentTimeMillis() - 3600000,
                    questText = "Q7",
                    tag = QuestTag.MIND,
                    experiencePoints = 100,
                    userId = currentUserId,
                    username = "User_49d22a"
                ),
                QuestCompletion(
                    id = "",
                    lat = 74.5074, lng = -0.3278,
                    timestamp = System.currentTimeMillis() - 5000000,
                    questText = "Q8",
                    tag = QuestTag.SPIRIT,
                    experiencePoints = 75,
                    userId = currentUserId,
                    username = "User_49d22a"
                )
            )
            else -> emptyList()
        }
    }

    private fun getCurrentUsername(): String {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val deviceId = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        return "User_${deviceId.takeLast(6)}"
    }
}
