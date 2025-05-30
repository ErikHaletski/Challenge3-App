package de.challenge3.questapp.utils

import android.content.Context
import android.util.Log
import de.challenge3.questapp.repository.FirebaseQuestCompletionRepository
import de.challenge3.questapp.repository.FirebaseFriendRepository
import de.challenge3.questapp.ui.home.QuestCompletion
import de.challenge3.questapp.ui.home.QuestTag
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class DataMigration(private val context: Context) {
    private val TAG = "DataMigration"

    suspend fun migrateSampleDataToFirebase(): Boolean = withContext(Dispatchers.IO) {
        try {
            val questRepository = FirebaseQuestCompletionRepository()
            val friendRepository = FirebaseFriendRepository(context)
            val currentUserId = friendRepository.getCurrentUserId()
            val currentUsername = getCurrentUsername()
            val deviceId = getDeviceId()

            Log.d(TAG, "Starting migration for user: $currentUsername (ID: $currentUserId, Device: $deviceId)")

            val sampleQuests = getSampleQuests(currentUserId, currentUsername, deviceId)
            Log.d(TAG, "Generated ${sampleQuests.size} sample quests for device ending in ${deviceId.takeLast(6)}")

            if (sampleQuests.isEmpty()) {
                Log.d(TAG, "No sample quests generated for this device ID")
                return@withContext true
            }

            var successCount = 0
            for (quest in sampleQuests) {
                try {
                    questRepository.addCompletedQuest(quest)
                    Log.d(TAG, "Added quest: ${quest.questTitle} - ${quest.questText} at (${quest.lat}, ${quest.lng})")
                    successCount++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to add quest: ${quest.questTitle}", e)
                }
            }

            Log.d(TAG, "Migration completed. Added $successCount/${sampleQuests.size} quests")
            return@withContext successCount > 0
        } catch (e: Exception) {
            Log.e(TAG, "Migration failed with exception", e)
            return@withContext false
        }
    }

    private fun getSampleQuests(currentUserId: String, username: String, deviceId: String): List<QuestCompletion> {
        val deviceSuffix = deviceId.takeLast(6)

        Log.d(TAG, "Checking device suffix: $deviceSuffix for username: $username")

        return when (deviceSuffix) {
            "5072a1" -> createQuestSet1(currentUserId, username)
            "a5284e" -> createQuestSet2(currentUserId, username)
            "ac69ea" -> createQuestSet3(currentUserId, username)
            "49d22a" -> createQuestSet4(currentUserId, username)
            "000000" -> createEmulatorQuestSet(currentUserId, username)
            else -> {
                val hashBasedSet = (deviceSuffix.hashCode() % 4) + 1
                Log.d(TAG, "Unknown device ID, using hash-based set: $hashBasedSet")
                when (hashBasedSet) {
                    1 -> createQuestSet1(currentUserId, username)
                    2 -> createQuestSet2(currentUserId, username)
                    3 -> createQuestSet3(currentUserId, username)
                    else -> createQuestSet4(currentUserId, username)
                }
            }
        }
    }

    private fun createQuestSet1(userId: String, username: String): List<QuestCompletion> {
        Log.d(TAG, "Creating quest set 1 (Berlin/Paris theme)")
        return listOf(
            QuestCompletion(
                id = "",
                lat = 52.5200, lng = 13.4050,
                timestamp = System.currentTimeMillis() - 3600000,
                questTitle = "Berlin Workout", // Kurzer Titel für Marker
                questText = "Completed morning workout session in Berlin's Tiergarten park",
                tag = QuestTag.MIGHT,
                experiencePoints = 100,
                userId = userId,
                username = username
            ),
            QuestCompletion(
                id = "",
                lat = 48.8566, lng = 2.3522,
                timestamp = System.currentTimeMillis() - 7200000,
                questTitle = "Louvre Visit", // Kurzer Titel für Marker
                questText = "Explored the magnificent art collection at the Louvre Museum in Paris",
                tag = QuestTag.MIND,
                experiencePoints = 150,
                userId = userId,
                username = username
            )
        )
    }

    private fun createQuestSet2(userId: String, username: String): List<QuestCompletion> {
        Log.d(TAG, "Creating quest set 2 (London/Amsterdam theme)")
        return listOf(
            QuestCompletion(
                id = "",
                lat = 51.5074, lng = -0.1278,
                timestamp = System.currentTimeMillis() - 3600000,
                questTitle = "London Charity", // Kurzer Titel für Marker
                questText = "Volunteered at a local charity organization helping homeless people in London",
                tag = QuestTag.HEART,
                experiencePoints = 200,
                userId = userId,
                username = username
            ),
            QuestCompletion(
                id = "",
                lat = 52.3676, lng = 4.9041,
                timestamp = System.currentTimeMillis() - 7200000,
                questTitle = "Canal Meditation", // Kurzer Titel für Marker
                questText = "Peaceful meditation session by the beautiful canals of Amsterdam",
                tag = QuestTag.SPIRIT,
                experiencePoints = 80,
                userId = userId,
                username = username
            )
        )
    }

    private fun createQuestSet3(userId: String, username: String): List<QuestCompletion> {
        Log.d(TAG, "Creating quest set 3 (Rome/Barcelona theme)")
        return listOf(
            QuestCompletion(
                id = "",
                lat = 41.9028, lng = 12.4964,
                timestamp = System.currentTimeMillis() - 3600000,
                questTitle = "Roman History", // Kurzer Titel für Marker
                questText = "Deep dive into ancient Roman history at the Colosseum and Forum",
                tag = QuestTag.MIND,
                experiencePoints = 120,
                userId = userId,
                username = username
            ),
            QuestCompletion(
                id = "",
                lat = 41.3851, lng = 2.1734,
                timestamp = System.currentTimeMillis() - 7200000,
                questTitle = "Beach Volleyball", // Kurzer Titel für Marker
                questText = "Intensive beach volleyball training session at Barcelona's coastline",
                tag = QuestTag.MIGHT,
                experiencePoints = 90,
                userId = userId,
                username = username
            )
        )
    }

    private fun createQuestSet4(userId: String, username: String): List<QuestCompletion> {
        Log.d(TAG, "Creating quest set 4 (Vienna/Prague theme)")
        return listOf(
            QuestCompletion(
                id = "",
                lat = 48.2082, lng = 16.3738,
                timestamp = System.currentTimeMillis() - 3600000,
                questTitle = "Vienna Concert", // Kurzer Titel für Marker
                questText = "Attended a classical music concert at Vienna's famous Musikverein",
                tag = QuestTag.SPIRIT,
                experiencePoints = 110,
                userId = userId,
                username = username
            ),
            QuestCompletion(
                id = "",
                lat = 50.0755, lng = 14.4378,
                timestamp = System.currentTimeMillis() - 7200000,
                questTitle = "Prague Photography", // Kurzer Titel für Marker
                questText = "Photography walk through Prague's historic old town and castle district",
                tag = QuestTag.HEART,
                experiencePoints = 85,
                userId = userId,
                username = username
            )
        )
    }

    private fun createEmulatorQuestSet(userId: String, username: String): List<QuestCompletion> {
        Log.d(TAG, "Creating emulator quest set (Mixed European cities)")
        return listOf(
            QuestCompletion(
                id = "",
                lat = 52.5200, lng = 13.4050,
                timestamp = System.currentTimeMillis() - 3600000,
                questTitle = "Test Berlin", // Kurzer Titel für Marker
                questText = "Emulator test quest in Berlin - exploring the city center",
                tag = QuestTag.MIND,
                experiencePoints = 50,
                userId = userId,
                username = username
            ),
            QuestCompletion(
                id = "",
                lat = 48.8566, lng = 2.3522,
                timestamp = System.currentTimeMillis() - 7200000,
                questTitle = "Test Paris", // Kurzer Titel für Marker
                questText = "Emulator test quest in Paris - visiting famous landmarks",
                tag = QuestTag.HEART,
                experiencePoints = 75,
                userId = userId,
                username = username
            ),
            QuestCompletion(
                id = "",
                lat = 51.5074, lng = -0.1278,
                timestamp = System.currentTimeMillis() - 10800000,
                questTitle = "Test London", // Kurzer Titel für Marker
                questText = "Emulator test quest in London - exploring the Thames area",
                tag = QuestTag.MIGHT,
                experiencePoints = 60,
                userId = userId,
                username = username
            )
        )
    }

    private fun getCurrentUsername(): String {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val deviceId = getDeviceId()
        return "User_${deviceId.takeLast(6)}"
    }

    private fun getDeviceId(): String {
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
    }

    fun getDeviceInfo(): String {
        val deviceId = getDeviceId()
        val username = getCurrentUsername()
        val deviceSuffix = deviceId.takeLast(6)

        return """
            Device ID: $deviceId
            Username: $username
            Device Suffix: $deviceSuffix
            Quest Set: ${getQuestSetName(deviceSuffix)}
        """.trimIndent()
    }

    private fun getQuestSetName(deviceSuffix: String): String {
        return when (deviceSuffix) {
            "5072a1" -> "Set 1 (Berlin/Paris)"
            "a5284e" -> "Set 2 (London/Amsterdam)"
            "ac69ea" -> "Set 3 (Rome/Barcelona)"
            "49d22a" -> "Set 4 (Vienna/Prague)"
            "000000" -> "Emulator Set (Mixed)"
            else -> "Hash-based Set (${(deviceSuffix.hashCode() % 4) + 1})"
        }
    }
}
