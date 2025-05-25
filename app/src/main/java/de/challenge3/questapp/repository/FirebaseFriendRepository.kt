package de.challenge3.questapp.repository

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import de.challenge3.questapp.models.Friend
import de.challenge3.questapp.models.FriendRequest
import de.challenge3.questapp.models.FriendshipStatus
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseFriendRepository(private val context: Context) : FriendRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private val friendsCollection = firestore.collection("friends")
    private val friendRequestsCollection = firestore.collection("friend_requests")
    private val usersCollection = firestore.collection("users")

    private val _friends = MutableLiveData<List<Friend>>()
    private val _friendRequests = MutableLiveData<List<FriendRequest>>()

    private var friendsListener: ListenerRegistration? = null
    private var requestsListener: ListenerRegistration? = null

    // Use device ID as user identifier
    private val currentUserId: String by lazy {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        var userId = prefs.getString("user_id", null)

        if (userId == null) {
            // Generate a unique user ID for this device
            userId = "user_${UUID.randomUUID()}"
            prefs.edit().putString("user_id", userId).apply()

            // Also create a user profile
            createUserProfile(userId)
        }

        userId
    }

    init {
        startListening()
    }

    private fun createUserProfile(userId: String) {
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val userData = mapOf(
            "userId" to userId,
            "username" to "User_${deviceId.takeLast(6)}", // Simple username based on device
            "email" to "$userId@questapp.local", // Fake email for this device
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
        val userId = currentUserId
        println("DEBUG: Starting listeners for user: $userId")

        // Listen to friends
        friendsListener = friendsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG: Error listening to friends: ${error.message}")
                    return@addSnapshotListener
                }

                println("DEBUG: Friends snapshot received, ${snapshot?.documents?.size ?: 0} documents")

                val friendIds = snapshot?.documents?.mapNotNull { doc ->
                    doc.getString("friendId")
                } ?: emptyList()

                if (friendIds.isNotEmpty()) {
                    fetchFriendDetails(friendIds)
                } else {
                    _friends.value = emptyList()
                }
            }

        // Listen to friend requests - Enhanced debugging
        println("DEBUG: Setting up friend requests listener for toUserId: $userId")
        requestsListener = friendRequestsCollection
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "PENDING_SENT")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG: Error listening to friend requests: ${error.message}")
                    return@addSnapshotListener
                }

                println("DEBUG: Friend requests snapshot received")
                println("DEBUG: Snapshot size: ${snapshot?.documents?.size ?: 0}")

                snapshot?.documents?.forEach { doc ->
                    println("DEBUG: Request document: ${doc.id}")
                    println("DEBUG: fromUserId: ${doc.getString("fromUserId")}")
                    println("DEBUG: toUserId: ${doc.getString("toUserId")}")
                    println("DEBUG: status: ${doc.getString("status")}")
                    println("DEBUG: timestamp: ${doc.getLong("timestamp")}")
                }

                val requests = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val fromUserId = document.getString("fromUserId") ?: return@mapNotNull null

                        println("DEBUG: Processing request from $fromUserId")

                        FriendRequest(
                            id = document.id,
                            fromUserId = fromUserId,
                            toUserId = document.getString("toUserId") ?: "",
                            status = FriendshipStatus.PENDING_RECEIVED, // Convert to PENDING_RECEIVED for the receiver
                            timestamp = document.getLong("timestamp") ?: 0L,
                            fromUser = null
                        )
                    } catch (e: Exception) {
                        println("DEBUG: Error parsing friend request: ${e.message}")
                        null
                    }
                } ?: emptyList()

                println("DEBUG: Found ${requests.size} friend requests for user $userId")

                if (requests.isNotEmpty()) {
                    fetchRequestDetails(requests)
                } else {
                    println("DEBUG: No friend requests found, setting empty list")
                    _friendRequests.value = emptyList()
                }
            }
    }

    private fun fetchFriendDetails(friendIds: List<String>) {
        println("DEBUG: Fetching friend details for: $friendIds")
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
                println("DEBUG: Setting ${friends.size} friends")
                _friends.value = friends
            }
            .addOnFailureListener { error ->
                println("DEBUG: Error fetching friend details: ${error.message}")
            }
    }

    private fun fetchRequestDetails(requests: List<FriendRequest>) {
        println("DEBUG: Fetching request details for ${requests.size} requests")

        if (requests.isEmpty()) {
            _friendRequests.value = emptyList()
            return
        }

        val fromUserIds = requests.map { it.fromUserId }
        println("DEBUG: Looking up users: $fromUserIds")

        usersCollection
            .whereIn("userId", fromUserIds)
            .get()
            .addOnSuccessListener { snapshot ->
                println("DEBUG: User lookup returned ${snapshot.documents.size} users")

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
                        println("DEBUG: Error parsing user: ${e.message}")
                        null
                    }
                    userId to user
                }

                val requestsWithUsers = requests.map { request ->
                    val user = userMap[request.fromUserId]
                    println("DEBUG: Request ${request.id} from ${request.fromUserId} -> user: ${user?.username}")
                    request.copy(fromUser = user)
                }

                println("DEBUG: Setting ${requestsWithUsers.size} friend requests with user details")
                _friendRequests.value = requestsWithUsers
            }
            .addOnFailureListener { error ->
                println("DEBUG: Error fetching request details: ${error.message}")
            }
    }

    override fun getFriends(): LiveData<List<Friend>> = _friends

    override fun getFriendRequests(): LiveData<List<FriendRequest>> = _friendRequests

    override suspend fun sendFriendRequest(email: String): Result<Unit> {
        return try {
            val currentUser = currentUserId

            println("DEBUG: Sending friend request from $currentUser to $email")

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

            println("DEBUG: Target user found: $targetUserId")

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

            // Check if request already exists (in either direction)
            val existingRequest1 = friendRequestsCollection
                .whereEqualTo("fromUserId", currentUser)
                .whereEqualTo("toUserId", targetUserId)
                .whereEqualTo("status", "PENDING_SENT")
                .get()
                .await()

            val existingRequest2 = friendRequestsCollection
                .whereEqualTo("fromUserId", targetUserId)
                .whereEqualTo("toUserId", currentUser)
                .whereEqualTo("status", "PENDING_SENT")
                .get()
                .await()

            if (!existingRequest1.documents.isEmpty() || !existingRequest2.documents.isEmpty()) {
                return Result.failure(Exception("Friend request already exists"))
            }

            // Create friend request
            val requestData = mapOf(
                "fromUserId" to currentUser,
                "toUserId" to targetUserId,
                "status" to "PENDING_SENT", // Store as PENDING_SENT
                "timestamp" to System.currentTimeMillis()
            )

            val docRef = friendRequestsCollection.add(requestData).await()
            println("DEBUG: Created friend request with ID: ${docRef.id}")
            println("DEBUG: Request data: $requestData")

            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error sending friend request: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun acceptFriendRequest(requestId: String): Result<Unit> {
        return try {
            val currentUser = currentUserId

            println("DEBUG: Accepting friend request $requestId for user $currentUser")

            // Get the request
            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            val fromUserId = requestDoc.getString("fromUserId") ?: return Result.failure(Exception("Invalid request"))
            val toUserId = requestDoc.getString("toUserId") ?: return Result.failure(Exception("Invalid request"))

            if (toUserId != currentUser) {
                return Result.failure(Exception("Not authorized to accept this request"))
            }

            // Create friendship entries for both users
            val batch = firestore.batch()

            // Add friendship for current user
            val friendship1 = friendsCollection.document()
            batch.set(friendship1, mapOf(
                "userId" to currentUser,
                "friendId" to fromUserId,
                "timestamp" to System.currentTimeMillis()
            ))

            // Add friendship for the other user
            val friendship2 = friendsCollection.document()
            batch.set(friendship2, mapOf(
                "userId" to fromUserId,
                "friendId" to currentUser,
                "timestamp" to System.currentTimeMillis()
            ))

            // Delete the friend request
            batch.delete(friendRequestsCollection.document(requestId))

            batch.commit().await()
            println("DEBUG: Friend request accepted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error accepting friend request: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun declineFriendRequest(requestId: String): Result<Unit> {
        return try {
            val currentUser = currentUserId

            // Verify the request belongs to current user
            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            val toUserId = requestDoc.getString("toUserId")

            if (toUserId != currentUser) {
                return Result.failure(Exception("Not authorized to decline this request"))
            }

            friendRequestsCollection.document(requestId).delete().await()
            println("DEBUG: Friend request declined successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error declining friend request: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun removeFriend(friendId: String): Result<Unit> {
        return try {
            val currentUser = currentUserId

            // Remove both friendship entries
            val batch = firestore.batch()

            // Remove friendship from current user's list
            val friendship1Query = friendsCollection
                .whereEqualTo("userId", currentUser)
                .whereEqualTo("friendId", friendId)
                .get()
                .await()

            friendship1Query.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            // Remove friendship from friend's list
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
            val currentUser = currentUserId

            // Search by username or email
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
                        if (userId == currentUser) return@mapNotNull null // Don't include current user

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

    fun stopListening() {
        friendsListener?.remove()
        requestsListener?.remove()
    }

    // Debug method to get current user ID - renamed to avoid conflict
    fun getDebugUserId(): String = currentUserId

    // Debug method to manually check for friend requests
    suspend fun debugCheckFriendRequests(): List<FriendRequest> {
        return try {
            val userId = currentUserId
            println("DEBUG: Manual check for friend requests for user: $userId")

            val snapshot = friendRequestsCollection
                .whereEqualTo("toUserId", userId)
                .whereEqualTo("status", "PENDING_SENT")
                .get()
                .await()

            println("DEBUG: Manual check found ${snapshot.documents.size} requests")

            snapshot.documents.forEach { doc ->
                println("DEBUG: Manual - Request ${doc.id}: from=${doc.getString("fromUserId")}, to=${doc.getString("toUserId")}, status=${doc.getString("status")}")
            }

            snapshot.documents.mapNotNull { document ->
                try {
                    FriendRequest(
                        id = document.id,
                        fromUserId = document.getString("fromUserId") ?: "",
                        toUserId = document.getString("toUserId") ?: "",
                        status = FriendshipStatus.PENDING_RECEIVED,
                        timestamp = document.getLong("timestamp") ?: 0L,
                        fromUser = null
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            println("DEBUG: Error in manual check: ${e.message}")
            emptyList()
        }
    }
}
