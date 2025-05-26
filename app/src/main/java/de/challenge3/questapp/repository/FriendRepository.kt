package de.challenge3.questapp.repository

import androidx.lifecycle.LiveData
import de.challenge3.questapp.models.Friend
import de.challenge3.questapp.models.FriendRequest

interface FriendRepository {
    fun getFriends(): LiveData<List<Friend>>
    fun getFriendRequests(): LiveData<List<FriendRequest>>
    suspend fun sendFriendRequest(email: String): Result<Unit>
    suspend fun acceptFriendRequest(requestId: String): Result<Unit>
    suspend fun declineFriendRequest(requestId: String): Result<Unit>
    suspend fun removeFriend(friendId: String): Result<Unit>
    suspend fun searchUsers(query: String): Result<List<Friend>>
    fun stopListening()
    fun getCurrentUserId(): String
}
