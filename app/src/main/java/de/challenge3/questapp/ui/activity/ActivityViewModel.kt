package de.challenge3.questapp.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import de.challenge3.questapp.ui.home.QuestCompletion
import de.challenge3.questapp.repository.QuestRepository
import de.challenge3.questapp.repository.FirebaseQuestRepository
import de.challenge3.questapp.repository.FriendRepository
import de.challenge3.questapp.repository.FirebaseFriendRepository
import org.maplibre.android.geometry.LatLng
import android.content.Context

class ActivityViewModel(
    private val context: Context,
    private val questRepository: QuestRepository = FirebaseQuestRepository(),
    private val friendRepository: FriendRepository = FirebaseFriendRepository(context)
) : ViewModel() {

    private val currentUserId = friendRepository.getCurrentUserId()

    // Get friends list
    private val friends = friendRepository.getFriends()

    // Create filtered quests based on current user + friends
    val completedQuests: LiveData<List<QuestCompletion>> = friends.switchMap { friendsList ->
        val friendIds = friendsList.map { it.id }
        questRepository.getQuestsForUserAndFriends(currentUserId, friendIds)
    }

    private val _selectedQuest = MutableLiveData<QuestCompletion?>()
    val selectedQuest: LiveData<QuestCompletion?> = _selectedQuest

    private val _mapState = MutableLiveData<MapState>()
    val mapState: LiveData<MapState> = _mapState

    fun selectQuest(quest: QuestCompletion) {
        _selectedQuest.value = quest
    }

    fun clearSelectedQuest() {
        _selectedQuest.value = null
    }

    fun getQuestInfoText(quest: QuestCompletion): String {
        val userInfo = if (quest.userId == currentUserId) {
            "You"
        } else {
            quest.username.ifEmpty { "Friend" }
        }

        return """
            ${quest.tag.displayName.uppercase()}
            ${quest.questText}
            Completed by: $userInfo
            XP: ${quest.experiencePoints}
            ${quest.formattedTimestamp}
        """.trimIndent()
    }

    fun updateMapState(state: MapState) {
        _mapState.value = state
    }

    override fun onCleared() {
        super.onCleared()
        (questRepository as? FirebaseQuestRepository)?.stopListening()
        friendRepository.stopListening()
    }
}

data class MapState(
    val isLocationEnabled: Boolean = false,
    val currentLocation: LatLng? = null,
    val zoomLevel: Double = 10.5
)
