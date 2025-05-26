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

class FirebaseFriendRepository(private val context: Context) : FriendRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // Firebase Collections - these write to your Firestore database
    private val friendsCollection = firestore.collection("friends")
    private val friendRequestsCollection = firestore.collection("friend_requests")
    private val usersCollection = firestore.collection("users")

    private val _friends = MutableLiveData<List<Friend>>()
    private val _friendRequests = MutableLiveData<List<FriendRequest>>()

    private var friendsListener: ListenerRegistration? = null
    private var requestsListener: ListenerRegistration? = null

    // Generate unique user ID for this device
    private val _currentUserId: String by lazy {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        var userId = prefs.getString("user_id", null)

        if (userId == null) {
            userId = "user_${UUID.randomUUID()}"
            prefs.edit().putString("user_id", userId).apply()
            createUserProfile(userId)
        }
        userId
    }

    init {
        startListening()
    }

    /**
     * Creates user profile in Firebase - THIS WRITES TO DATABASE
     */
    private fun createUserProfile(userId: String) {
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

        // THIS WRITES TO FIREBASE
        usersCollection.document(userId).set(userData)
    }

    /**
     * Sets up real-time listeners for friends and friend requests
     */
    private fun startListening() {
        val userId = _currentUserId // Updated reference

        // Listen to friends collection
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

        // Listen to friend requests
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

    /**
     * Sends friend request - THIS WRITES TO DATABASE
     */
    override suspend fun sendFriendRequest(email: String): Result<Unit> {
        return try {
            val currentUser = _currentUserId // Updated reference

            // Find user by email
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

            // Check if friendship already exists
            val existingFriendship = friendsCollection
                .whereEqualTo("userId", currentUser)
                .whereEqualTo("friendId", targetUserId)
                .get()
                .await()

            if (!existingFriendship.documents.isEmpty()) {
                return Result.failure(Exception("Already friends with this user"))
            }

            // Check if request already exists
            val existingRequest = friendRequestsCollection
                .whereEqualTo("fromUserId", currentUser)
                .whereEqualTo("toUserId", targetUserId)
                .whereEqualTo("status", "PENDING_SENT")
                .get()
                .await()

            if (!existingRequest.documents.isEmpty()) {
                return Result.failure(Exception("Friend request already exists"))
            }

            // Create friend request - THIS WRITES TO DATABASE
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

    /**
     * Accepts friend request - THIS WRITES TO DATABASE
     */
    override suspend fun acceptFriendRequest(requestId: String): Result<Unit> {
        return try {
            val currentUser = _currentUserId // Updated reference

            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            val fromUserId = requestDoc.getString("fromUserId") ?: return Result.failure(Exception("Invalid request"))
            val toUserId = requestDoc.getString("toUserId") ?: return Result.failure(Exception("Invalid request"))

            if (toUserId != currentUser) {
                return Result.failure(Exception("Not authorized to accept this request"))
            }

            // Create friendship entries for both users - THIS WRITES TO DATABASE
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

            // Delete the friend request
            batch.delete(friendRequestsCollection.document(requestId))

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Declines friend request - THIS WRITES TO DATABASE
     */
    override suspend fun declineFriendRequest(requestId: String): Result<Unit> {
        return try {
            val currentUser = _currentUserId // Updated reference

            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            val toUserId = requestDoc.getString("toUserId")

            if (toUserId != currentUser) {
                return Result.failure(Exception("Not authorized to decline this request"))
            }

            // Delete the request - THIS WRITES TO DATABASE
            friendRequestsCollection.document(requestId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Removes friend - THIS WRITES TO DATABASE
     */
    override suspend fun removeFriend(friendId: String): Result<Unit> {
        return try {
            val currentUser = _currentUserId // Updated reference

            // Remove both friendship entries - THIS WRITES TO DATABASE
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
            val currentUser = _currentUserId // Updated reference

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

    // Add debug methods
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
