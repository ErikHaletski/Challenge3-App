package de.challenge3.questapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserManager(private val context: Context) {
    private val TAG = "UserManager"
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_SETUP_COMPLETE = "user_setup_complete"
    }

    // Callback for when quest count changes
    private var onQuestCountChangedCallback: (() -> Unit)? = null

    fun setOnQuestCountChangedCallback(callback: () -> Unit) {
        onQuestCountChangedCallback = callback
    }

    /**
     * Gets the current user ID, creating one if it doesn't exist
     */
    fun getCurrentUserId(): String {
        var userId = prefs.getString(KEY_USER_ID, null)

        if (userId == null) {
            val deviceId = getDeviceId()
            userId = "user_device_$deviceId"
            prefs.edit().putString(KEY_USER_ID, userId).apply()
        }

        return userId
    }

    /**
     * Checks if user setup is complete (username chosen)
     */
    fun isUserSetupComplete(): Boolean {
        return prefs.getBoolean(KEY_USER_SETUP_COMPLETE, false)
    }

    /**
     * Gets the stored username
     */
    fun getStoredUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    /**
     * Ensures user exists in Firebase, returns true if setup is needed
     */
    suspend fun ensureUserExists(): Boolean {
        val userId = getCurrentUserId()

        try {
            // Check if user profile exists in Firebase
            val userDoc = usersCollection.document(userId).get().await()

            if (!userDoc.exists()) {
                // User doesn't exist in Firebase
                if (isUserSetupComplete()) {
                    // We have a username stored locally, create profile
                    val username = getStoredUsername() ?: getDefaultUsername()
                    createUserProfile(userId, username)
                    Log.d(TAG, "Created user profile for existing user: $username")
                    return false // Setup already complete
                } else {
                    // Need username selection
                    Log.d(TAG, "New user needs setup: $userId")
                    return true // Setup needed
                }
            } else {
                // User exists in Firebase
                if (!isUserSetupComplete()) {
                    // Mark setup as complete if user exists in Firebase
                    val username = userDoc.getString("username") ?: getDefaultUsername()
                    prefs.edit()
                        .putString(KEY_USERNAME, username)
                        .putBoolean(KEY_USER_SETUP_COMPLETE, true)
                        .apply()
                    Log.d(TAG, "Marked existing user setup as complete: $username")
                }

                // Set user as online when app starts
                setUserOnlineStatus(true)

                return false // No setup needed
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking user existence", e)
            return false
        }
    }

    /**
     * Completes user setup with chosen username
     */
    suspend fun completeUserSetup(username: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()

            // Validate username
            if (username.isBlank() || username.length < 2) {
                return Result.failure(Exception("Username must be at least 2 characters long"))
            }

            if (username.length > 20) {
                return Result.failure(Exception("Username must be 20 characters or less"))
            }

            // Check if username is already taken
            val existingUser = usersCollection
                .whereEqualTo("username", username)
                .get()
                .await()

            if (!existingUser.documents.isEmpty()) {
                val existingUserId = existingUser.documents.first().getString("userId")
                if (existingUserId != userId) {
                    return Result.failure(Exception("Username '$username' is already taken"))
                }
            }

            // Create user profile
            createUserProfile(userId, username)

            // Mark setup as complete
            prefs.edit()
                .putString(KEY_USERNAME, username)
                .putBoolean(KEY_USER_SETUP_COMPLETE, true)
                .apply()

            Log.d(TAG, "User setup completed: $username")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error completing user setup", e)
            Result.failure(e)
        }
    }

    /**
     * Creates user profile in Firebase
     */
    private suspend fun createUserProfile(userId: String, username: String) {
        val deviceId = getDeviceId()
        val userData = mapOf(
            "userId" to userId,
            "username" to username,
            "email" to "$userId@questapp.local",
            "level" to 1,
            "totalExperience" to 0,
            "isOnline" to true,
            "lastSeen" to System.currentTimeMillis(),
            "completedQuestsCount" to 0,
            "createdAt" to System.currentTimeMillis(),
            "deviceId" to deviceId
        )

        usersCollection.document(userId).set(userData).await()
        Log.d(TAG, "Created user profile: $username ($userId)")
    }

    /**
     * Updates user profile information
     */
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            usersCollection.document(userId).update(updates).await()

            // Update local storage if username was changed
            updates["username"]?.let { newUsername ->
                prefs.edit().putString(KEY_USERNAME, newUsername.toString()).apply()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
            Result.failure(e)
        }
    }

    /**
     * Increments the user's completed quests count in Firebase
     */
    suspend fun incrementCompletedQuestsCount(): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            val userDoc = usersCollection.document(userId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDoc)
                val currentCount = snapshot.getLong("completedQuestsCount") ?: 0
                transaction.update(userDoc, "completedQuestsCount", currentCount + 1)
            }.await()

            Log.d(TAG, "Incremented quest count for user: $userId")

            // Notify that quest count changed
            onQuestCountChangedCallback?.invoke()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementing quest count", e)
            Result.failure(e)
        }
    }

    /**
     * Sets the user's online status
     */
    suspend fun setUserOnlineStatus(isOnline: Boolean): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            val updates = mutableMapOf<String, Any>(
                "isOnline" to isOnline,
                "lastSeen" to System.currentTimeMillis()
            )

            usersCollection.document(userId).update(updates).await()
            Log.d(TAG, "Updated online status for user $userId: $isOnline")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating online status", e)
            Result.failure(e)
        }
    }

    /**
     * Sets user as online
     */
    suspend fun setUserOnline(): Result<Unit> {
        return setUserOnlineStatus(true)
    }

    /**
     * Sets user as offline
     */
    suspend fun setUserOffline(): Result<Unit> {
        return setUserOnlineStatus(false)
    }

    /**
     * Gets device ID
     */
    private fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    /**
     * Generates default username based on device ID
     */
    private fun getDefaultUsername(): String {
        val deviceId = getDeviceId()
        return "User_${deviceId.takeLast(6)}"
    }

    /**
     * Validates username format
     */
    fun isValidUsername(username: String): Boolean {
        return username.isNotBlank() &&
                username.length in 2..20 &&
                username.matches(Regex("^[a-zA-Z0-9_]+$"))
    }

    /**
     * Gets debug information
     */
    fun getDebugInfo(): String {
        val userId = getCurrentUserId()
        val username = getStoredUsername()
        val setupComplete = isUserSetupComplete()
        val deviceId = getDeviceId()

        return """
            User ID: $userId
            Username: $username
            Setup Complete: $setupComplete
            Device ID: $deviceId
            Device Suffix: ${deviceId.takeLast(6)}
        """.trimIndent()
    }
}