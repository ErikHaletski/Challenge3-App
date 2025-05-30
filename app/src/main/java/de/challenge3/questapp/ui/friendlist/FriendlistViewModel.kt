package de.challenge3.questapp.ui.friendlist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.challenge3.questapp.models.Friend
import de.challenge3.questapp.models.FriendRequest
import de.challenge3.questapp.repository.FirebaseFriendRepository
import de.challenge3.questapp.repository.FriendRepository
import kotlinx.coroutines.launch

class FriendlistViewModel(
    private val context: Context,
    private val friendRepository: FriendRepository = FirebaseFriendRepository(context)
) : ViewModel() {

    val friends: LiveData<List<Friend>> = friendRepository.getFriends()
    val friendRequests: LiveData<List<FriendRequest>> = friendRepository.getFriendRequests()

    private val _searchResults = MutableLiveData<List<Friend>>()
    val searchResults: LiveData<List<Friend>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching: LiveData<Boolean> = _isSearching

    fun sendFriendRequest(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Please enter a valid email"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            friendRepository.sendFriendRequest(email)
                .onSuccess {
                    _successMessage.value = "Friend request sent to $email!"
                    clearSearchResults()
                }
                .onFailure { error ->
                    _errorMessage.value = "Failed to send request: ${error.message}"
                }
            _isLoading.value = false
        }
    }

    fun acceptFriendRequest(requestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            friendRepository.acceptFriendRequest(requestId)
                .onSuccess {
                    _successMessage.value = "Friend request accepted!"
                }
                .onFailure { error ->
                    _errorMessage.value = "Failed to accept request: ${error.message}"
                }
            _isLoading.value = false
        }
    }

    fun declineFriendRequest(requestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            friendRepository.declineFriendRequest(requestId)
                .onSuccess {
                    _successMessage.value = "Friend request declined"
                }
                .onFailure { error ->
                    _errorMessage.value = "Failed to decline request: ${error.message}"
                }
            _isLoading.value = false
        }
    }

    fun removeFriend(friendId: String, friendName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            friendRepository.removeFriend(friendId)
                .onSuccess {
                    _successMessage.value = "$friendName removed from friends"
                }
                .onFailure { error ->
                    _errorMessage.value = "Failed to remove friend: ${error.message}"
                }
            _isLoading.value = false
        }
    }

    fun searchUsers(query: String) {
        if (query.length < 3) {
            clearSearchResults()
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            friendRepository.searchUsers(query)
                .onSuccess { results ->
                    _searchResults.value = results
                }
                .onFailure { error ->
                    _errorMessage.value = "Search failed: ${error.message}"
                }
            _isSearching.value = false
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        friendRepository.stopListening()
    }
}
