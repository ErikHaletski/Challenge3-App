package de.challenge3.questapp.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import de.challenge3.questapp.models.Friend
import de.challenge3.questapp.models.FriendRequest
import de.challenge3.questapp.models.FriendshipStatus
import de.challenge3.questapp.utils.UserManager
import kotlinx.coroutines.tasks.await

class FirebaseFriendRepository(private val context: Context) : FriendRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val friendsCollection = firestore.collection("friends")
    private val friendRequestsCollection = firestore.collection("friend_requests")
    private val usersCollection = firestore.collection("users")
    private val userManager = UserManager(context)

    private val _friends = MutableLiveData<List<Friend>>()
    private val _friendRequests = MutableLiveData<List<FriendRequest>>()

    private var friendsListener: ListenerRegistration? = null
    private var requestsListener: ListenerRegistration? = null
    private var userDataListeners: MutableList<ListenerRegistration> = mutableListOf()

    init {
        startListening()
    }

    private fun startListening() {
        startFriendsListener()
        startRequestsListener()
    }

    private fun startFriendsListener() {
        val currentUserId = userManager.getCurrentUserId()

        friendsListener = friendsCollection
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val friendIds = snapshot?.documents?.mapNotNull { it.getString("friendId") } ?: emptyList()
                if (friendIds.isNotEmpty()) {
                    fetchFriendDetails(friendIds)
                    startUserDataListeners(friendIds) // Start listening to user data changes
                } else {
                    _friends.value = emptyList()
                    stopUserDataListeners()
                }
            }
    }

    private fun startUserDataListeners(friendIds: List<String>) {
        // Stop existing listeners
        stopUserDataListeners()

        // Start new listeners for each friend's user data
        friendIds.forEach { friendId ->
            val listener = usersCollection.document(friendId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener

                    // When any friend's data changes, refresh all friends
                    fetchFriendDetails(friendIds)
                }
            userDataListeners.add(listener)
        }
    }

    private fun stopUserDataListeners() {
        userDataListeners.forEach { it.remove() }
        userDataListeners.clear()
    }

    private fun startRequestsListener() {
        val currentUserId = userManager.getCurrentUserId()

        requestsListener = friendRequestsCollection
            .whereEqualTo("toUserId", currentUserId)
            .whereEqualTo("status", "PENDING_SENT")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val requests = snapshot?.documents?.mapNotNull { document ->
                    createFriendRequest(document)
                } ?: emptyList()

                if (requests.isNotEmpty()) {
                    fetchRequestDetails(requests)
                } else {
                    _friendRequests.value = emptyList()
                }
            }
    }

    private fun createFriendRequest(document: com.google.firebase.firestore.DocumentSnapshot): FriendRequest? {
        return try {
            FriendRequest(
                id = document.id,
                fromUserId = document.getString("fromUserId") ?: return null,
                toUserId = document.getString("toUserId") ?: "",
                status = FriendshipStatus.PENDING_RECEIVED,
                timestamp = document.getLong("timestamp") ?: 0L,
                fromUser = null
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchFriendDetails(friendIds: List<String>) {
        usersCollection
            .whereIn("userId", friendIds)
            .get()
            .addOnSuccessListener { snapshot ->
                val friends = snapshot.documents.mapNotNull { createFriend(it) }
                _friends.value = friends
            }
    }

    private fun fetchRequestDetails(requests: List<FriendRequest>) {
        val fromUserIds = requests.map { it.fromUserId }
        usersCollection
            .whereIn("userId", fromUserIds)
            .get()
            .addOnSuccessListener { snapshot ->
                val userMap = snapshot.documents.associate { document ->
                    val userId = document.getString("userId") ?: ""
                    userId to createFriend(document)
                }

                val requestsWithUsers = requests.map { request ->
                    request.copy(fromUser = userMap[request.fromUserId])
                }
                _friendRequests.value = requestsWithUsers
            }
    }

    private fun createFriend(document: com.google.firebase.firestore.DocumentSnapshot): Friend? {
        return try {
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

    override fun getFriends(): LiveData<List<Friend>> = _friends
    override fun getFriendRequests(): LiveData<List<FriendRequest>> = _friendRequests

    /**
     * Force refresh friends data - useful when we know data has changed
     */
    fun refreshFriendsData() {
        val currentFriends = _friends.value ?: emptyList()
        val friendIds = currentFriends.map { it.id }
        if (friendIds.isNotEmpty()) {
            fetchFriendDetails(friendIds)
        }
    }

    override suspend fun sendFriendRequest(email: String): Result<Unit> {
        return try {
            val currentUserId = userManager.getCurrentUserId()
            val targetUser = findUserByEmail(email) ?: return Result.failure(Exception("User not found"))
            val targetUserId = targetUser.getString("userId") ?: return Result.failure(Exception("Invalid user"))

            validateFriendRequest(targetUserId, currentUserId)

            val requestData = mapOf(
                "fromUserId" to currentUserId,
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

    private suspend fun findUserByEmail(email: String): com.google.firebase.firestore.DocumentSnapshot? {
        val userQuery = usersCollection.whereEqualTo("email", email).get().await()
        return userQuery.documents.firstOrNull()
    }

    private suspend fun validateFriendRequest(targetUserId: String, currentUserId: String) {
        if (targetUserId == currentUserId) {
            throw Exception("Cannot send friend request to yourself")
        }

        val existingFriendship = friendsCollection
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("friendId", targetUserId)
            .get()
            .await()

        if (!existingFriendship.documents.isEmpty()) {
            throw Exception("Already friends with this user")
        }

        val existingRequest = friendRequestsCollection
            .whereEqualTo("fromUserId", currentUserId)
            .whereEqualTo("toUserId", targetUserId)
            .whereEqualTo("status", "PENDING_SENT")
            .get()
            .await()

        if (!existingRequest.documents.isEmpty()) {
            throw Exception("Friend request already exists")
        }
    }

    override suspend fun acceptFriendRequest(requestId: String): Result<Unit> {
        return try {
            val currentUserId = userManager.getCurrentUserId()
            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            val fromUserId = requestDoc.getString("fromUserId") ?: return Result.failure(Exception("Invalid request"))
            val toUserId = requestDoc.getString("toUserId") ?: return Result.failure(Exception("Invalid request"))

            if (toUserId != currentUserId) {
                return Result.failure(Exception("Not authorized to accept this request"))
            }

            val batch = firestore.batch()

            // Create bidirectional friendship
            val friendship1 = friendsCollection.document()
            batch.set(friendship1, mapOf(
                "userId" to currentUserId,
                "friendId" to fromUserId,
                "timestamp" to System.currentTimeMillis()
            ))

            val friendship2 = friendsCollection.document()
            batch.set(friendship2, mapOf(
                "userId" to fromUserId,
                "friendId" to currentUserId,
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
            val currentUserId = userManager.getCurrentUserId()
            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            val toUserId = requestDoc.getString("toUserId")

            if (toUserId != currentUserId) {
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
            val currentUserId = userManager.getCurrentUserId()
            val batch = firestore.batch()

            // Remove both directions of friendship
            val friendship1Query = friendsCollection
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("friendId", friendId)
                .get()
                .await()

            friendship1Query.documents.forEach { batch.delete(it.reference) }

            val friendship2Query = friendsCollection
                .whereEqualTo("userId", friendId)
                .whereEqualTo("friendId", currentUserId)
                .get()
                .await()

            friendship2Query.documents.forEach { batch.delete(it.reference) }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String): Result<List<Friend>> {
        return try {
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

            val currentUserId = userManager.getCurrentUserId()
            val allResults = (usernameQuery.documents + emailQuery.documents)
                .distinctBy { it.id }
                .mapNotNull { createFriend(it) }
                .filter { it.id != currentUserId }

            Result.success(allResults)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun stopListening() {
        friendsListener?.remove()
        requestsListener?.remove()
        stopUserDataListeners()
    }

    override fun getCurrentUserId(): String = userManager.getCurrentUserId()

    fun getDebugUserId(): String = userManager.getCurrentUserId()

    suspend fun debugCheckFriendRequests(): List<FriendRequest> {
        return try {
            val currentUserId = userManager.getCurrentUserId()
            val snapshot = friendRequestsCollection
                .whereEqualTo("toUserId", currentUserId)
                .get()
                .await()

            snapshot.documents.mapNotNull { createFriendRequest(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
