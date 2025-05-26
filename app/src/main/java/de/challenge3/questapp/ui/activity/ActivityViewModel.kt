package de.challenge3.questapp.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.map
import androidx.lifecycle.MediatorLiveData
import de.challenge3.questapp.ui.home.QuestCompletion
import de.challenge3.questapp.ui.home.QuestTag
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

    // Friend filter state
    private val _isEveryoneSelected = MutableLiveData<Boolean>(true)
    val isEveryoneSelected: LiveData<Boolean> = _isEveryoneSelected

    private val _isMeSelected = MutableLiveData<Boolean>(false)
    val isMeSelected: LiveData<Boolean> = _isMeSelected

    private val _selectedFriendIds = MutableLiveData<Set<String>>(emptySet())

    // Quest filters
    private val _selectedTagFilter = MutableLiveData<QuestTag?>(null)
    val selectedTagFilter: LiveData<QuestTag?> = _selectedTagFilter

    // Sort state
    private val _sortOption = MutableLiveData<QuestSortOption>(QuestSortOption.NEWEST_FIRST)
    val sortOption: LiveData<QuestSortOption> = _sortOption

    // All quests (unfiltered)
    private val allQuests: LiveData<List<QuestCompletion>> = friends.switchMap { friendsList ->
        val friendIds = friendsList.map { it.id }
        questRepository.getQuestsForUserAndFriends(currentUserId, friendIds)
    }

    // Filtered quests based on friend selection
    private val friendFilteredQuests: LiveData<List<QuestCompletion>> = MediatorLiveData<List<QuestCompletion>>().apply {
        addSource(allQuests) { updateFriendFilteredQuests() }
        addSource(_isEveryoneSelected) { updateFriendFilteredQuests() }
        addSource(_isMeSelected) { updateFriendFilteredQuests() }
        addSource(_selectedFriendIds) { updateFriendFilteredQuests() }
    }

    // Final filtered and sorted quests
    val filteredAndSortedQuests: LiveData<List<QuestCompletion>> = MediatorLiveData<List<QuestCompletion>>().apply {
        addSource(friendFilteredQuests) { updateFilteredAndSortedQuests() }
        addSource(_selectedTagFilter) { updateFilteredAndSortedQuests() }
        addSource(_sortOption) { updateFilteredAndSortedQuests() }
        addSource(friends) { updateFilteredAndSortedQuests() }
    }

    // For backward compatibility with map markers
    val completedQuests: LiveData<List<QuestCompletion>> = filteredAndSortedQuests

    // Friend checkbox items for the UI
    val friendCheckboxItems: LiveData<List<FriendCheckboxItem>> = MediatorLiveData<List<FriendCheckboxItem>>().apply {
        addSource(friends) { updateFriendCheckboxItems() }
        addSource(allQuests) { updateFriendCheckboxItems() }
        addSource(_selectedFriendIds) { updateFriendCheckboxItems() }
        addSource(_isEveryoneSelected) { updateFriendCheckboxItems() }
    }

    // Friend filter summary
    val friendFilterSummary: LiveData<String> = MediatorLiveData<String>().apply {
        addSource(_isEveryoneSelected) { updateFriendFilterSummary() }
        addSource(_isMeSelected) { updateFriendFilterSummary() }
        addSource(_selectedFriendIds) { updateFriendFilterSummary() }
        addSource(friends) { updateFriendFilterSummary() }
    }

    val totalQuestCount: LiveData<Int> = filteredAndSortedQuests.map { it.size }

    private val _selectedQuest = MutableLiveData<QuestCompletion?>()
    val selectedQuest: LiveData<QuestCompletion?> = _selectedQuest

    private val _mapState = MutableLiveData<MapState>()
    val mapState: LiveData<MapState> = _mapState

    private fun updateFriendFilteredQuests() {
        val quests = allQuests.value ?: return
        val isEveryone = _isEveryoneSelected.value ?: false
        val isMe = _isMeSelected.value ?: false
        val selectedFriends = _selectedFriendIds.value ?: emptySet()

        val filtered = when {
            isEveryone -> quests // Show all quests
            else -> {
                quests.filter { quest ->
                    (isMe && quest.userId == currentUserId) ||
                            (quest.userId in selectedFriends)
                }
            }
        }

        (friendFilteredQuests as MediatorLiveData).value = filtered
    }

    private fun updateFilteredAndSortedQuests() {
        val quests = friendFilteredQuests.value ?: return
        val tagFilter = _selectedTagFilter.value
        val sortBy = _sortOption.value ?: QuestSortOption.NEWEST_FIRST
        val friendsList = friends.value ?: emptyList()

        // Apply tag filter
        var filtered = quests
        if (tagFilter != null) {
            filtered = filtered.filter { it.tag == tagFilter }
        }

        // Sort the filtered list
        val sorted = when (sortBy) {
            QuestSortOption.NEWEST_FIRST -> filtered.sortedByDescending { it.timestamp }
            QuestSortOption.OLDEST_FIRST -> filtered.sortedBy { it.timestamp }
            QuestSortOption.FRIEND_NAME -> filtered.sortedWith { quest1, quest2 ->
                val name1 = getFriendDisplayName(quest1, friendsList)
                val name2 = getFriendDisplayName(quest2, friendsList)
                name1.compareTo(name2, ignoreCase = true)
            }
            QuestSortOption.QUEST_TAG -> filtered.sortedBy { it.tag.displayName }
            QuestSortOption.EXPERIENCE_HIGH -> filtered.sortedByDescending { it.experiencePoints }
            QuestSortOption.EXPERIENCE_LOW -> filtered.sortedBy { it.experiencePoints }
        }

        (filteredAndSortedQuests as MediatorLiveData).value = sorted
    }

    private fun updateFriendCheckboxItems() {
        val friendsList = friends.value ?: return
        val quests = allQuests.value ?: emptyList()
        val selectedIds = _selectedFriendIds.value ?: emptySet()
        val isEveryone = _isEveryoneSelected.value ?: false

        val items = friendsList.map { friend ->
            val questCount = quests.count { it.userId == friend.id }
            FriendCheckboxItem(
                friend = friend,
                isSelected = !isEveryone && friend.id in selectedIds,
                questCount = questCount
            )
        }

        (friendCheckboxItems as MediatorLiveData).value = items
    }

    private fun updateFriendFilterSummary() {
        val isEveryone = _isEveryoneSelected.value ?: false
        val isMe = _isMeSelected.value ?: false
        val selectedFriends = _selectedFriendIds.value ?: emptySet()
        val friendsList = friends.value ?: emptyList()

        val summary = when {
            isEveryone -> "Everyone"
            else -> {
                val parts = mutableListOf<String>()
                if (isMe) parts.add("Me")

                val selectedFriendNames = friendsList
                    .filter { it.id in selectedFriends }
                    .map { it.displayName }
                parts.addAll(selectedFriendNames)

                when {
                    parts.isEmpty() -> "None selected"
                    parts.size == 1 -> parts.first()
                    parts.size <= 3 -> parts.joinToString(", ")
                    else -> "${parts.take(2).joinToString(", ")} + ${parts.size - 2} more"
                }
            }
        }

        (friendFilterSummary as MediatorLiveData).value = summary
    }

    private fun getFriendDisplayName(quest: QuestCompletion, friendsList: List<Friend>): String {
        return when {
            quest.userId == currentUserId -> "You"
            quest.username.isNotEmpty() -> quest.username
            else -> {
                friendsList.find { it.id == quest.userId }?.displayName ?: "Unknown"
            }
        }
    }

    fun setEveryoneFilter(isSelected: Boolean) {
        if (isSelected) {
            // Everyone is exclusive - clear other selections
            _isEveryoneSelected.value = true
            _isMeSelected.value = false
            _selectedFriendIds.value = emptySet()
        } else {
            _isEveryoneSelected.value = false
        }
    }

    fun setMeFilter(isSelected: Boolean) {
        if (isSelected) {
            // Can't select Me with Everyone
            _isEveryoneSelected.value = false
        }
        _isMeSelected.value = isSelected
    }

    fun toggleFriendInFilter(friendId: String, isSelected: Boolean) {
        if (isSelected) {
            // Can't select friends with Everyone
            _isEveryoneSelected.value = false
            val currentSelected = _selectedFriendIds.value ?: emptySet()
            _selectedFriendIds.value = currentSelected + friendId
        } else {
            val currentSelected = _selectedFriendIds.value ?: emptySet()
            _selectedFriendIds.value = currentSelected - friendId
        }
    }

    fun setSortOption(option: QuestSortOption) {
        _sortOption.value = option
    }

    fun setTagFilter(tag: QuestTag?) {
        _selectedTagFilter.value = tag
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
