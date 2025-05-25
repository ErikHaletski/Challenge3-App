package de.challenge3.questapp.utils

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import de.challenge3.questapp.models.Friend
import de.challenge3.questapp.models.FriendRequest
import de.challenge3.questapp.models.FriendshipStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FriendDataMigration(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()

    fun migrateSampleDataToFirebase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // First, create sample users
                createSampleUsers()

                // Then create sample friendships and requests for the current device
                createSampleFriendships()
                createSampleFriendRequests()
            } catch (e: Exception) {
                // Handle error - you might want to log this
                println("Friend migration failed: ${e.message}")
            }
        }
    }

    private suspend fun createSampleUsers() {
        val users = getSampleUsers()
        val usersCollection = firestore.collection("users")

        users.forEach { user ->
            val userData = mapOf(
                "userId" to user.id,
                "username" to user.username,
                "email" to user.email,
                "level" to user.level,
                "totalExperience" to user.totalExperience,
                "isOnline" to user.isOnline,
                "lastSeen" to (user.lastSeen ?: System.currentTimeMillis()),
                "completedQuestsCount" to user.completedQuestsCount,
                "createdAt" to System.currentTimeMillis()
            )

            // Use the user ID as document ID for easy lookup
            usersCollection.document(user.id).set(userData).await()
        }
    }

    private suspend fun createSampleFriendships() {
        // Get the current device's user ID
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUserId = prefs.getString("user_id", null) ?: return

        val friendsCollection = firestore.collection("friends")

        // Use the actual user IDs from sample data
        val sampleFriendIds = listOf(
            "user_questmaster_001",
            "user_adventure_002",
            "user_dragon_003"
        )

        sampleFriendIds.forEach { friendId ->
            // Create friendship for current user
            val friendship1Data = mapOf(
                "userId" to currentUserId,
                "friendId" to friendId,
                "timestamp" to System.currentTimeMillis()
            )
            friendsCollection.add(friendship1Data).await()

            // Create friendship for the friend (bidirectional)
            val friendship2Data = mapOf(
                "userId" to friendId,
                "friendId" to currentUserId,
                "timestamp" to System.currentTimeMillis()
            )
            friendsCollection.add(friendship2Data).await()
        }
    }

    private suspend fun createSampleFriendRequests() {
        // Get the current device's user ID
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUserId = prefs.getString("user_id", null) ?: return

        val requestsCollection = firestore.collection("friend_requests")

        val sampleRequests = getSampleFriendRequests(currentUserId)

        sampleRequests.forEach { request ->
            val requestData = mapOf(
                "fromUserId" to request.fromUserId,
                "toUserId" to request.toUserId,
                "status" to request.status.name,
                "timestamp" to request.timestamp
            )
            requestsCollection.add(requestData).await()
        }
    }

    private fun getSampleUsers() = listOf(
        // Friends (people you're already friends with)
        Friend(
            id = "user_questmaster_001",
            username = "QuestMaster",
            email = "questmaster@example.com",
            level = 15,
            totalExperience = 15750,
            isOnline = true,
            completedQuestsCount = 42
        ),
        Friend(
            id = "user_adventure_002",
            username = "AdventureSeeker",
            email = "adventure@example.com",
            level = 8,
            totalExperience = 8200,
            isOnline = false,
            lastSeen = System.currentTimeMillis() - 3600000,
            completedQuestsCount = 23
        ),
        Friend(
            id = "user_dragon_003",
            username = "DragonHunter",
            email = "dragon@example.com",
            level = 22,
            totalExperience = 22500,
            isOnline = true,
            completedQuestsCount = 67
        ),

        // Users who will send friend requests
        Friend(
            id = "user_explorer_004",
            username = "NewExplorer",
            email = "explorer@example.com",
            level = 3,
            totalExperience = 2100,
            isOnline = false,
            completedQuestsCount = 8
        ),
        Friend(
            id = "user_magic_005",
            username = "MagicUser",
            email = "magic@example.com",
            level = 12,
            totalExperience = 12500,
            isOnline = false,
            completedQuestsCount = 31
        ),

        // Users for search results
        Friend(
            id = "user_slayer_006",
            username = "DragonSlayer",
            email = "dragonslayer@example.com",
            level = 20,
            totalExperience = 25000,
            isOnline = true,
            completedQuestsCount = 55
        ),
        Friend(
            id = "user_wizard_007",
            username = "WiseWizard",
            email = "wizard@example.com",
            level = 18,
            totalExperience = 18750,
            isOnline = true,
            completedQuestsCount = 48
        )
    )

    private fun getSampleFriendRequests(currentUserId: String) = listOf(
        FriendRequest(
            id = "", // Firebase will generate
            fromUserId = "user_explorer_004",
            toUserId = currentUserId,
            status = FriendshipStatus.PENDING_RECEIVED,
            timestamp = System.currentTimeMillis() - 1800000
        ),
        FriendRequest(
            id = "",
            fromUserId = "user_magic_005",
            toUserId = currentUserId,
            status = FriendshipStatus.PENDING_RECEIVED,
            timestamp = System.currentTimeMillis() - 3600000
        )
    )
}
