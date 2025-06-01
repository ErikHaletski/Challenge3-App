package de.challenge3.questapp.ui.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import de.challenge3.questapp.utils.UserManager
import kotlinx.coroutines.launch

class UserSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val userManager = UserManager(application)

    private val _isUsernameValid = MutableLiveData<Boolean>()
    val isUsernameValid: LiveData<Boolean> = _isUsernameValid

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _setupComplete = MutableLiveData<Boolean>()
    val setupComplete: LiveData<Boolean> = _setupComplete

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun validateUsername(username: String) {
        _isUsernameValid.value = userManager.isValidUsername(username)
    }

    fun completeSetup(username: String) {
        viewModelScope.launch {
            _isLoading.value = true

            userManager.completeUserSetup(username)
                .onSuccess {
                    _setupComplete.value = true
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
