package de.challenge3.questapp.utils

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import de.challenge3.questapp.models.Friend
import de.challenge3.questapp.models.FriendRequest
import de.challenge3.questapp.models.FriendshipStatus
import kotlinx.coroutines.tasks.await

class FriendDataMigration(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Migrates sample data to Firebase - THIS WRITES TO DATABASE
     * Creates sample users, friendships, and friend requests
     */
    suspend fun migrateSampleDataToFirebase() {
        try {
            createSampleUsers()
            createSampleFriendships()
            createSampleFriendRequests()
        } catch (e: Exception) {
            println("Friend migration failed: ${e.message}")
            throw e
        }
    }

    /**
     * Creates sample users in Firebase - THIS WRITES TO DATABASE
     */
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

            usersCollection.document(user.id).set(userData).await()
        }
    }

    /**
     * Creates sample friendships - THIS WRITES TO DATABASE
     */
    private suspend fun createSampleFriendships() {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUserId = prefs.getString("user_id", null) ?: return

        val friendsCollection = firestore.collection("friends")
        val sampleFriendIds = listOf(
            "user_questmaster_001",
            "user_adventure_002",
            "user_dragon_003"
        )

        sampleFriendIds.forEach { friendId ->
            // Create bidirectional friendship
            val friendship1Data = mapOf(
                "userId" to currentUserId,
                "friendId" to friendId,
                "timestamp" to System.currentTimeMillis()
            )
            friendsCollection.add(friendship1Data).await()

            val friendship2Data = mapOf(
                "userId" to friendId,
                "friendId" to currentUserId,
                "timestamp" to System.currentTimeMillis()
            )
            friendsCollection.add(friendship2Data).await()
        }
    }

    /**
     * Creates sample friend requests - THIS WRITES TO DATABASE
     */
    private suspend fun createSampleFriendRequests() {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUserId = prefs.getString("user_id", null) ?: return

        val requestsCollection = firestore.collection("friend_requests")
        val sampleRequests = getSampleFriendRequests(currentUserId)

        sampleRequests.forEach { request ->
            val requestData = mapOf(
                "fromUserId" to request.fromUserId,
                "toUserId" to request.toUserId,
                "status" to "PENDING_SENT", // Store as PENDING_SENT in database
                "timestamp" to request.timestamp
            )
            requestsCollection.add(requestData).await()
        }
    }

    private fun getSampleUsers() = listOf(
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
        )
    )

    private fun getSampleFriendRequests(currentUserId: String) = listOf(
        FriendRequest(
            id = "",
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
