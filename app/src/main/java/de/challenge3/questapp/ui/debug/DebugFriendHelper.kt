package de.challenge3.questapp.utils

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import de.challenge3.questapp.models.FriendshipStatus
import kotlinx.coroutines.tasks.await

class DebugFriendHelper(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()

    // Get current device's user ID
    private val currentUserId: String
        get() {
            val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            return prefs.getString("user_id", "") ?: ""
        }

    // Simulate receiving a friend request from a sample user
    suspend fun simulateIncomingFriendRequest(fromUsername: String = "TestUser") {
        try {
            val requestData = mapOf(
                "fromUserId" to "debug_user_${System.currentTimeMillis()}",
                "toUserId" to currentUserId,
                "status" to FriendshipStatus.PENDING_RECEIVED.name,
                "timestamp" to System.currentTimeMillis()
            )

            // Create the sender user first
            val userData = mapOf(
                "userId" to requestData["fromUserId"],
                "username" to fromUsername,
                "email" to "${fromUsername.lowercase()}@test.com",
                "level" to (1..20).random(),
                "totalExperience" to (100..5000).random(),
                "isOnline" to listOf(true, false).random(),
                "lastSeen" to System.currentTimeMillis() - (0..86400000).random(),
                "completedQuestsCount" to (5..50).random(),
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users").document(requestData["fromUserId"] as String).set(userData).await()
            firestore.collection("friend_requests").add(requestData).await()

            println("Debug: Created friend request from $fromUsername")
        } catch (e: Exception) {
            println("Debug: Failed to create friend request: ${e.message}")
        }
    }

    // Create a test user that can be searched and friended
    suspend fun createSearchableTestUser(username: String = "SearchableUser") {
        try {
            val userId = "searchable_${username.lowercase()}_${System.currentTimeMillis()}"
            val userData = mapOf(
                "userId" to userId,
                "username" to username,
                "email" to "${username.lowercase()}@test.com",
                "level" to (1..25).random(),
                "totalExperience" to (100..10000).random(),
                "isOnline" to true,
                "lastSeen" to System.currentTimeMillis(),
                "completedQuestsCount" to (10..100).random(),
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users").document(userId).set(userData).await()
            println("Debug: Created searchable user: $username")
        } catch (e: Exception) {
            println("Debug: Failed to create test user: ${e.message}")
        }
    }

    // Get current user info for debugging
    suspend fun getCurrentUserInfo(): String {
        return try {
            val doc = firestore.collection("users").document(currentUserId).get().await()
            if (doc.exists()) {
                val username = doc.getString("username") ?: "Unknown"
                val email = doc.getString("email") ?: "Unknown"
                "Current User: $username ($email) - ID: $currentUserId"
            } else {
                "Current User ID: $currentUserId (No profile found)"
            }
        } catch (e: Exception) {
            "Error getting user info: ${e.message}"
        }
    }

    // Clear all debug data
    suspend fun clearDebugData() {
        try {
            // Remove debug users
            val debugUsers = firestore.collection("users")
                .whereGreaterThanOrEqualTo("userId", "debug_")
                .whereLessThanOrEqualTo("userId", "debug_\uf8ff")
                .get()
                .await()

            debugUsers.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            val searchableUsers = firestore.collection("users")
                .whereGreaterThanOrEqualTo("userId", "searchable_")
                .whereLessThanOrEqualTo("userId", "searchable_\uf8ff")
                .get()
                .await()

            searchableUsers.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            println("Debug: Cleared all debug data")
        } catch (e: Exception) {
            println("Debug: Failed to clear debug data: ${e.message}")
        }
    }
}
