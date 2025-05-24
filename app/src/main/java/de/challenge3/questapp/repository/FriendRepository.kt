package de.challenge3.questapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.challenge3.questapp.models.Friend
import de.challenge3.questapp.models.FriendRequest
import de.challenge3.questapp.models.FriendshipStatus

interface FriendRepository {
    fun getFriends(): LiveData<List<Friend>>
    fun getFriendRequests(): LiveData<List<FriendRequest>>
    suspend fun sendFriendRequest(email: String): Result<Unit>
    suspend fun acceptFriendRequest(requestId: String): Result<Unit>
    suspend fun declineFriendRequest(requestId: String): Result<Unit>
    suspend fun removeFriend(friendId: String): Result<Unit>
    suspend fun searchUsers(query: String): Result<List<Friend>>
}

class FriendRepositoryImpl : FriendRepository {
    private val _friends = MutableLiveData<List<Friend>>()
    private val _friendRequests = MutableLiveData<List<FriendRequest>>()

    private val friendsCache = mutableListOf<Friend>()
    private val requestsCache = mutableListOf<FriendRequest>()

    init {
        // Initialize with sample data
        friendsCache.addAll(getSampleFriends())
        requestsCache.addAll(getSampleRequests())
        _friends.value = friendsCache.toList()
        _friendRequests.value = requestsCache.toList()
    }

    override fun getFriends(): LiveData<List<Friend>> = _friends
    override fun getFriendRequests(): LiveData<List<FriendRequest>> = _friendRequests

    override suspend fun sendFriendRequest(email: String): Result<Unit> {
        return try {
            // Simulate API call delay
            kotlinx.coroutines.delay(1000)

            val newRequest = FriendRequest(
                id = "req_${System.currentTimeMillis()}",
                fromUserId = "current_user",
                toUserId = "user_$email",
                status = FriendshipStatus.PENDING_SENT,
                timestamp = System.currentTimeMillis()
            )
            requestsCache.add(newRequest)
            _friendRequests.postValue(requestsCache.toList())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptFriendRequest(requestId: String): Result<Unit> {
        return try {
            kotlinx.coroutines.delay(500)

            val request = requestsCache.find { it.id == requestId }
            if (request != null) {
                // Remove from requests
                requestsCache.removeAll { it.id == requestId }

                // Add to friends
                request.fromUser?.let { user ->
                    friendsCache.add(user.copy(isOnline = true))
                }

                _friends.postValue(friendsCache.toList())
                _friendRequests.postValue(requestsCache.toList())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun declineFriendRequest(requestId: String): Result<Unit> {
        return try {
            kotlinx.coroutines.delay(500)
            requestsCache.removeAll { it.id == requestId }
            _friendRequests.postValue(requestsCache.toList())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFriend(friendId: String): Result<Unit> {
        return try {
            kotlinx.coroutines.delay(500)
            friendsCache.removeAll { it.id == friendId }
            _friends.postValue(friendsCache.toList())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String): Result<List<Friend>> {
        return try {
            kotlinx.coroutines.delay(800)
            // Simulate search
            val results = getSampleSearchResults().filter {
                it.username.contains(query, ignoreCase = true) ||
                        it.email.contains(query, ignoreCase = true)
            }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getSampleFriends() = listOf(
        Friend(
            id = "friend1",
            username = "QuestMaster",
            email = "questmaster@example.com",
            level = 15,
            totalExperience = 15750,
            isOnline = true,
            completedQuestsCount = 42
        ),
        Friend(
            id = "friend2",
            username = "AdventureSeeker",
            email = "adventure@example.com",
            level = 8,
            totalExperience = 8200,
            isOnline = false,
            lastSeen = System.currentTimeMillis() - 3600000,
            completedQuestsCount = 23
        ),
        Friend(
            id = "friend3",
            username = "DragonHunter",
            email = "dragon@example.com",
            level = 22,
            totalExperience = 22500,
            isOnline = true,
            completedQuestsCount = 67
        )
    )

    private fun getSampleRequests() = listOf(
        FriendRequest(
            id = "req1",
            fromUserId = "user1",
            toUserId = "current_user",
            status = FriendshipStatus.PENDING_RECEIVED,
            timestamp = System.currentTimeMillis() - 1800000,
            fromUser = Friend(
                id = "user1",
                username = "NewExplorer",
                email = "explorer@example.com",
                level = 3,
                totalExperience = 2100,
                completedQuestsCount = 8
            )
        )
    )

    private fun getSampleSearchResults() = listOf(
        Friend(
            id = "search1",
            username = "DragonSlayer",
            email = "dragon@example.com",
            level = 20,
            totalExperience = 25000,
            completedQuestsCount = 55
        ),
        Friend(
            id = "search2",
            username = "MagicUser",
            email = "magic@example.com",
            level = 12,
            totalExperience = 12500,
            completedQuestsCount = 31
        ),
        Friend(
            id = "search3",
            username = "WiseWizard",
            email = "wizard@example.com",
            level = 18,
            totalExperience = 18750,
            completedQuestsCount = 48
        )
    )
}
