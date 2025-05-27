package de.challenge3.questapp.repository

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.challenge3.questapp.models.Friend
import de.challenge3.questapp.models.FriendRequest
import de.challenge3.questapp.models.FriendshipStatus
import kotlinx.coroutines.tasks.await
import java.util.UUID

// handels all friend-related data operations
// -> like: send/accept/decline friend request, search for users, manage friendships
// -> like: listening to friend updates, generating user IDs based on device ID
class FirebaseFriendRepository(private val context: Context) : FriendRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val friendsCollection = firestore.collection("friends")
    private val friendRequestsCollection = firestore.collection("friend_requests")
    private val usersCollection = firestore.collection("users")

    private val _friends = MutableLiveData<List<Friend>>()
    private val _friendRequests = MutableLiveData<List<FriendRequest>>()

    private var friendsListener: ListenerRegistration? = null
    private var requestsListener: ListenerRegistration? = null

    // Generate user ID based on device ID for consistency
    // by lazy = property initialized only once
    private val _currentUserId: String by lazy {
        // loads persistent local storage (key-value paris)
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        var userId = prefs.getString("user_id", null)

        if (userId == null) {
            // Use device ID to create consistent user ID
            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            userId = "user_device_$deviceId"
            prefs.edit().putString("user_id", userId).apply()
            createUserProfile(userId)
        }
        userId
    }

    init {
        startListening()
    }

    private fun createUserProfile(userId: String) {
        // get the device id from android settings.secure api (device id = 64bit hex string)
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

        usersCollection.document(userId).set(userData)
    }

    private fun startListening() {
        val userId = _currentUserId

        friendsListener = friendsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error listening to friends: ${error.message}")
                    return@addSnapshotListener
                }

                val friendIds = snapshot?.documents?.mapNotNull { doc ->
                    doc.getString("friendId")
                } ?: emptyList()

                if (friendIds.isNotEmpty()) {
                    fetchFriendDetails(friendIds)
                } else {
                    _friends.value = emptyList()
                }
            }

        requestsListener = friendRequestsCollection
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "PENDING_SENT")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error listening to friend requests: ${error.message}")
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val fromUserId = document.getString("fromUserId") ?: return@mapNotNull null
                        FriendRequest(
                            id = document.id,
                            fromUserId = fromUserId,
                            toUserId = document.getString("toUserId") ?: "",
                            status = FriendshipStatus.PENDING_RECEIVED,
                            timestamp = document.getLong("timestamp") ?: 0L,
                            fromUser = null
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                if (requests.isNotEmpty()) {
                    fetchRequestDetails(requests)
                } else {
                    _friendRequests.value = emptyList()
                }
            }
    }

    private fun fetchFriendDetails(friendIds: List<String>) {
        usersCollection
            .whereIn("userId", friendIds)
            .get()
            .addOnSuccessListener { snapshot ->
                val friends = snapshot.documents.mapNotNull { document ->
                    try {
                        Friend(
                            id = document.getString("userId") ?: "",
                            username = document.getString("username") ?: "",
                            email = document.getString("email") ?: "",
                            level = document.getLong("level")?.toInt() ?: 1,
                            totalExperience = document.getLong("totalExperience")?.toInt() ?: 0,
                            isOnline = document.getBoolean("isOnline") ?: false,
                            lastSeen = document.getLong("lastSeen"),
                            completedQuestsCount = document.getLong("completedQuestsCount")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                _friends.value = friends
            }
    }

    private fun fetchRequestDetails(requests: List<FriendRequest>) {
        if (requests.isEmpty()) {
            _friendRequests.value = emptyList()
            return
        }

        val fromUserIds = requests.map { it.fromUserId }
        usersCollection
            .whereIn("userId", fromUserIds)
            .get()
            .addOnSuccessListener { snapshot ->
                val userMap = snapshot.documents.associate { document ->
                    val userId = document.getString("userId") ?: ""
                    val user = try {
                        Friend(
                            id = userId,
                            username = document.getString("username") ?: "",
                            email = document.getString("email") ?: "",
                            level = document.getLong("level")?.toInt() ?: 1,
                            totalExperience = document.getLong("totalExperience")?.toInt() ?: 0,
                            isOnline = document.getBoolean("isOnline") ?: false,
                            lastSeen = document.getLong("lastSeen"),
                            completedQuestsCount = document.getLong("completedQuestsCount")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                    userId to user
                }

                val requestsWithUsers = requests.map { request ->
                    request.copy(fromUser = userMap[request.fromUserId])
                }
                _friendRequests.value = requestsWithUsers
            }
    }

    override fun getFriends(): LiveData<List<Friend>> = _friends
    override fun getFriendRequests(): LiveData<List<FriendRequest>> = _friendRequests

    override suspend fun sendFriendRequest(email: String): Result<Unit> {
        return try {
            val currentUser = _currentUserId

            val userQuery = usersCollection
                .whereEqualTo("email", email)
                .get()
                .await()

            if (userQuery.documents.isEmpty()) {
                return Result.failure(Exception("User not found"))
            }

            val targetUser = userQuery.documents.first()
            val targetUserId = targetUser.getString("userId") ?: return Result.failure(Exception("Invalid user"))

            if (targetUserId == currentUser) {
                return Result.failure(Exception("Cannot send friend request to yourself"))
            }

            val existingFriendship = friendsCollection
                .whereEqualTo("userId", currentUser)
                .whereEqualTo("friendId", targetUserId)
                .get()
                .await()

            if (!existingFriendship.documents.isEmpty()) {
                return Result.failure(Exception("Already friends with this user"))
            }

            val existingRequest = friendRequestsCollection
                .whereEqualTo("fromUserId", currentUser)
                .whereEqualTo("toUserId", targetUserId)
                .whereEqualTo("status", "PENDING_SENT")
                .get()
                .await()

            if (!existingRequest.documents.isEmpty()) {
                return Result.failure(Exception("Friend request already exists"))
            }

            val requestData = mapOf(
                "fromUserId" to currentUser,
                "toUserId" to targetUserId,
                "status" to "PENDING_SENT",
                "timestamp" to System.currentTimeMillis()
            )

            friendRequestsCollection.add(requestData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptFriendRequest(requestId: String): Result<Unit> {
        return try {
            val currentUser = _currentUserId

            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            val fromUserId = requestDoc.getString("fromUserId") ?: return Result.failure(Exception("Invalid request"))
            val toUserId = requestDoc.getString("toUserId") ?: return Result.failure(Exception("Invalid request"))

            if (toUserId != currentUser) {
                return Result.failure(Exception("Not authorized to accept this request"))
            }

            val batch = firestore.batch()

            val friendship1 = friendsCollection.document()
            batch.set(friendship1, mapOf(
                "userId" to currentUser,
                "friendId" to fromUserId,
                "timestamp" to System.currentTimeMillis()
            ))

            val friendship2 = friendsCollection.document()
            batch.set(friendship2, mapOf(
                "userId" to fromUserId,
                "friendId" to currentUser,
                "timestamp" to System.currentTimeMillis()
            ))

            batch.delete(friendRequestsCollection.document(requestId))

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun declineFriendRequest(requestId: String): Result<Unit> {
        return try {
            val currentUser = _currentUserId

            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            val toUserId = requestDoc.getString("toUserId")

            if (toUserId != currentUser) {
                return Result.failure(Exception("Not authorized to decline this request"))
            }

            friendRequestsCollection.document(requestId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFriend(friendId: String): Result<Unit> {
        return try {
            val currentUser = _currentUserId

            val batch = firestore.batch()

            val friendship1Query = friendsCollection
                .whereEqualTo("userId", currentUser)
                .whereEqualTo("friendId", friendId)
                .get()
                .await()

            friendship1Query.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            val friendship2Query = friendsCollection
                .whereEqualTo("userId", friendId)
                .whereEqualTo("friendId", currentUser)
                .get()
                .await()

            friendship2Query.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String): Result<List<Friend>> {
        return try {
            val currentUser = _currentUserId

            val usernameQuery = usersCollection
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .limit(10)
                .get()
                .await()

            val emailQuery = usersCollection
                .whereGreaterThanOrEqualTo("email", query)
                .whereLessThanOrEqualTo("email", query + "\uf8ff")
                .limit(10)
                .get()
                .await()

            val allResults = (usernameQuery.documents + emailQuery.documents)
                .distinctBy { it.id }
                .mapNotNull { document ->
                    try {
                        val userId = document.getString("userId") ?: return@mapNotNull null
                        if (userId == currentUser) return@mapNotNull null

                        Friend(
                            id = userId,
                            username = document.getString("username") ?: "",
                            email = document.getString("email") ?: "",
                            level = document.getLong("level")?.toInt() ?: 1,
                            totalExperience = document.getLong("totalExperience")?.toInt() ?: 0,
                            isOnline = document.getBoolean("isOnline") ?: false,
                            lastSeen = document.getLong("lastSeen"),
                            completedQuestsCount = document.getLong("completedQuestsCount")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

            Result.success(allResults)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun stopListening() {
        friendsListener?.remove()
        requestsListener?.remove()
    }

    override fun getCurrentUserId(): String = _currentUserId

    fun getDebugUserId(): String = _currentUserId

    suspend fun debugCheckFriendRequests(): List<FriendRequest> {
        return try {
            val snapshot = friendRequestsCollection
                .whereEqualTo("toUserId", _currentUserId)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    FriendRequest(
                        id = document.id,
                        fromUserId = document.getString("fromUserId") ?: "",
                        toUserId = document.getString("toUserId") ?: "",
                        status = FriendshipStatus.valueOf(document.getString("status") ?: "PENDING_RECEIVED"),
                        timestamp = document.getLong("timestamp") ?: 0L,
                        fromUser = null
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
