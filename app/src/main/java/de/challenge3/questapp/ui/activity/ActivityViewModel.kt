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

    // Unified filter state
    private val _selectedFriendIds = MutableLiveData<Set<String>>(emptySet())
    private val _showMyQuests = MutableLiveData<Boolean>(true)
    val showMyQuests: LiveData<Boolean> = _showMyQuests

    // Unified quest filters
    private val _selectedTagFilter = MutableLiveData<QuestTag?>(null)
    val selectedTagFilter: LiveData<QuestTag?> = _selectedTagFilter

    private val _selectedFriendFilter = MutableLiveData<String?>(null)
    val selectedFriendFilter: LiveData<String?> = _selectedFriendFilter

    // Sort state
    private val _sortOption = MutableLiveData<QuestSortOption>(QuestSortOption.NEWEST_FIRST)
    val sortOption: LiveData<QuestSortOption> = _sortOption

    // All quests (unfiltered)
    private val allQuests: LiveData<List<QuestCompletion>> = friends.switchMap { friendsList ->
        val friendIds = friendsList.map { it.id }
        questRepository.getQuestsForUserAndFriends(currentUserId, friendIds)
    }

    // Unified filtered quests (for both map markers and quest list)
    private val baseFilteredQuests: LiveData<List<QuestCompletion>> = MediatorLiveData<List<QuestCompletion>>().apply {
        addSource(allQuests) { updateBaseFilteredQuests() }
        addSource(_selectedFriendIds) { updateBaseFilteredQuests() }
        addSource(_showMyQuests) { updateBaseFilteredQuests() }
    }

    // Final filtered and sorted quests (for quest list display and map markers)
    val filteredAndSortedQuests: LiveData<List<QuestCompletion>> = MediatorLiveData<List<QuestCompletion>>().apply {
        addSource(baseFilteredQuests) { updateFilteredAndSortedQuests() }
        addSource(_selectedTagFilter) { updateFilteredAndSortedQuests() }
        addSource(_selectedFriendFilter) { updateFilteredAndSortedQuests() }
        addSource(_sortOption) { updateFilteredAndSortedQuests() }
        addSource(friends) { updateFilteredAndSortedQuests() }
    }

    // For backward compatibility with map markers (same as filteredAndSortedQuests)
    val completedQuests: LiveData<List<QuestCompletion>> = filteredAndSortedQuests

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

    val totalQuestCount: LiveData<Int> = filteredAndSortedQuests.map { it.size }

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

    private fun updateBaseFilteredQuests() {
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

        (baseFilteredQuests as MediatorLiveData).value = filtered
    }

    private fun updateFilteredAndSortedQuests() {
        val quests = baseFilteredQuests.value ?: return
        val tagFilter = _selectedTagFilter.value
        val friendFilter = _selectedFriendFilter.value
        val sortBy = _sortOption.value ?: QuestSortOption.NEWEST_FIRST
        val friendsList = friends.value ?: emptyList()

        // Apply additional filters
        var filtered = quests

        // Filter by tag
        if (tagFilter != null) {
            filtered = filtered.filter { it.tag == tagFilter }
        }

        // Filter by specific friend
        if (friendFilter != null && friendFilter != "all") {
            filtered = if (friendFilter == "me") {
                filtered.filter { it.userId == currentUserId }
            } else {
                filtered.filter { it.userId == friendFilter }
            }
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

    private fun getFriendDisplayName(quest: QuestCompletion, friendsList: List<Friend>): String {
        return when {
            quest.userId == currentUserId -> "You"
            quest.username.isNotEmpty() -> quest.username
            else -> {
                friendsList.find { it.id == quest.userId }?.displayName ?: "Unknown"
            }
        }
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

    fun setSortOption(option: QuestSortOption) {
        _sortOption.value = option
    }

    fun setTagFilter(tag: QuestTag?) {
        _selectedTagFilter.value = tag
    }

    fun setFriendFilter(friendId: String?) {
        _selectedFriendFilter.value = friendId
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