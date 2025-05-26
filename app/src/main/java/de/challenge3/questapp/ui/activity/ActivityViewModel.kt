package de.challenge3.questapp.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.map
import androidx.lifecycle.MediatorLiveData
import de.challenge3.questapp.ui.home.QuestCompletion
import de.challenge3.questapp.repository.QuestRepository
import de.challenge3.questapp.repository.FirebaseQuestRepository
import de.challenge3.questapp.repository.FriendRepository
import de.challenge3.questapp.repository.FirebaseFriendRepository
import de.challenge3.questapp.models.Friend
import org.maplibre.android.geometry.LatLng
import android.content.Context

class ActivityViewModel(
    private val context: Context,
    private val questRepository: QuestRepository = FirebaseQuestRepository(),
    private val friendRepository: FriendRepository = FirebaseFriendRepository(context)
) : ViewModel() {

    private val currentUserId = friendRepository.getCurrentUserId()

    // Get friends list
    val friends = friendRepository.getFriends()

    // Filter state
    private val _selectedFriendIds = MutableLiveData<Set<String>>(emptySet())
    private val _showMyQuests = MutableLiveData<Boolean>(true)
    val showMyQuests: LiveData<Boolean> = _showMyQuests

    // All quests (unfiltered)
    private val allQuests: LiveData<List<QuestCompletion>> = friends.switchMap { friendsList ->
        val friendIds = friendsList.map { it.id }
        questRepository.getQuestsForUserAndFriends(currentUserId, friendIds)
    }

    // Filtered quests based on user selection
    val completedQuests: LiveData<List<QuestCompletion>> = MediatorLiveData<List<QuestCompletion>>().apply {
        addSource(allQuests) { updateFilteredQuests() }
        addSource(_selectedFriendIds) { updateFilteredQuests() }
        addSource(_showMyQuests) { updateFilteredQuests() }
    }

    // Friend filter items for the UI
    val friendFilterItems: LiveData<List<FriendFilterItem>> = MediatorLiveData<List<FriendFilterItem>>().apply {
        addSource(friends) { updateFriendFilterItems() }
        addSource(allQuests) { updateFriendFilterItems() }
        addSource(_selectedFriendIds) { updateFriendFilterItems() }
    }

    // Quest counts
    val myQuestCount: LiveData<Int> = allQuests.map { quests ->
        quests.count { it.userId == currentUserId }
    }

    private val _selectedQuest = MutableLiveData<QuestCompletion?>()
    val selectedQuest: LiveData<QuestCompletion?> = _selectedQuest

    private val _mapState = MutableLiveData<MapState>()
    val mapState: LiveData<MapState> = _mapState

    init {
        // Initialize with all friends selected
        friends.observeForever { friendsList ->
            if (_selectedFriendIds.value?.isEmpty() == true) {
                _selectedFriendIds.value = friendsList.map { it.id }.toSet()
            }
        }
    }

    private fun updateFilteredQuests() {
        val quests = allQuests.value ?: return
        val selectedFriends = _selectedFriendIds.value ?: emptySet()
        val showMy = _showMyQuests.value ?: true

        val filtered = quests.filter { quest ->
            when {
                quest.userId == currentUserId -> showMy
                quest.userId in selectedFriends -> true
                else -> false
            }
        }

        (completedQuests as MediatorLiveData).value = filtered
    }

    private fun updateFriendFilterItems() {
        val friendsList = friends.value ?: return
        val quests = allQuests.value ?: emptyList()
        val selectedIds = _selectedFriendIds.value ?: emptySet()

        val items = friendsList.map { friend ->
            val questCount = quests.count { it.userId == friend.id }
            FriendFilterItem(
                friend = friend,
                isSelected = friend.id in selectedIds,
                questCount = questCount
            )
        }

        (friendFilterItems as MediatorLiveData).value = items
    }

    fun toggleFriendFilter(friendId: String, isSelected: Boolean) {
        val currentSelected = _selectedFriendIds.value ?: emptySet()
        _selectedFriendIds.value = if (isSelected) {
            currentSelected + friendId
        } else {
            currentSelected - friendId
        }
    }

    fun toggleMyQuests(show: Boolean) {
        _showMyQuests.value = show
    }

    fun selectAllFriends() {
        val allFriendIds = friends.value?.map { it.id }?.toSet() ?: emptySet()
        _selectedFriendIds.value = allFriendIds
    }

    fun deselectAllFriends() {
        _selectedFriendIds.value = emptySet()
    }

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
