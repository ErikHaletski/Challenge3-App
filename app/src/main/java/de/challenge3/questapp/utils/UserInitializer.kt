package de.challenge3.questapp.utils

import android.content.Context
import android.provider.Settings
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Stellt sicher, dass ein User-Profil beim App-Start erstellt wird
 */
class UserInitializer(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    fun initializeUserIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            val userId = getCurrentUserId()
            checkAndCreateUser(userId)
        }
    }

    private fun getCurrentUserId(): String {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        var userId = prefs.getString("user_id", null)

        if (userId == null) {
            // Use device ID to create consistent user ID
            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            userId = "user_device_$deviceId"
            prefs.edit().putString("user_id", userId).apply()
        }
        return userId
    }

    private suspend fun checkAndCreateUser(userId: String) {
        try {
            // Prüfen ob User bereits existiert
            val userDoc = usersCollection.document(userId).get().await()

            if (!userDoc.exists()) {
                // User existiert nicht, erstellen
                createUserProfile(userId)
                println("User profile created for: $userId")
            } else {
                println("User profile already exists for: $userId")
            }
        } catch (e: Exception) {
            println("Error checking/creating user: ${e.message}")
        }
    }

    private suspend fun createUserProfile(userId: String) {
        try {
            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            val userData = mapOf(
                "userId" to userId,
                "username" to "User_${deviceId.takeLast(6)}",
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
        } catch (e: Exception) {
            println("Error creating user profile: ${e.message}")
        }
    }
}