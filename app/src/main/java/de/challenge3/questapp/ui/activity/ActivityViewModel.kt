package de.challenge3.questapp.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.map
import androidx.lifecycle.MediatorLiveData
import de.challenge3.questapp.ui.home.QuestCompletion
import de.challenge3.questapp.ui.home.QuestTag
import de.challenge3.questapp.repository.QuestCompletionRepository
import de.challenge3.questapp.repository.FirebaseQuestCompletionRepository
import de.challenge3.questapp.repository.FriendRepository
import de.challenge3.questapp.repository.FirebaseFriendRepository
import de.challenge3.questapp.models.Friend
import org.maplibre.android.geometry.LatLng
import android.content.Context


// manages activity/map screen state
// -> like: tag filtering, friend filtering, sorting, map state (location permissions, current location)
// -> like: quest selection (for popup display)
// important methods:   setAllTagsFilter()      -> toggles showing all quests tags
//                      toggleFriendInFilter()  -> add/remove friends from filter
//                      setSortOption()         -> change sorting method
//                      selectQuest()           -> select quest for detailed view
class ActivityViewModel(
    private val context: Context,
    private val questCompletionRepository: QuestCompletionRepository = FirebaseQuestCompletionRepository(),
    private val friendRepository: FriendRepository = FirebaseFriendRepository(context)
) : ViewModel() {

    private val currentUserId = friendRepository.getCurrentUserId()

    // Get friends list
    val friends = friendRepository.getFriends()

    // Tag filter state
    private val _isAllTagsSelected = MutableLiveData<Boolean>(true)
    val isAllTagsSelected: LiveData<Boolean> = _isAllTagsSelected

    private val _selectedTags = MutableLiveData<Set<QuestTag>>(emptySet())
    val selectedTags: LiveData<Set<QuestTag>> = _selectedTags

    // Friend filter state
    private val _isEveryoneSelected = MutableLiveData<Boolean>(true)
    val isEveryoneSelected: LiveData<Boolean> = _isEveryoneSelected

    private val _isMeSelected = MutableLiveData<Boolean>(false)
    val isMeSelected: LiveData<Boolean> = _isMeSelected

    private val _selectedFriendIds = MutableLiveData<Set<String>>(emptySet())

    // Sort state
    private val _sortOption = MutableLiveData<QuestCompletionSortOption>(QuestCompletionSortOption.NEWEST_FIRST)
    val sortOption: LiveData<QuestCompletionSortOption> = _sortOption

    // All quests (unfiltered)
    private val allQuests: LiveData<List<QuestCompletion>> = friends.switchMap { friendsList ->
        val friendIds = friendsList.map { it.id }
        questCompletionRepository.getQuestsForUserAndFriends(currentUserId, friendIds)
    }

    // Tag filtered quests
    private val tagFilteredQuests: LiveData<List<QuestCompletion>> = MediatorLiveData<List<QuestCompletion>>().apply {
        addSource(allQuests) { updateTagFilteredQuests() }
        addSource(_isAllTagsSelected) { updateTagFilteredQuests() }
        addSource(_selectedTags) { updateTagFilteredQuests() }
    }

    // Friend filtered quests
    private val friendFilteredQuests: LiveData<List<QuestCompletion>> = MediatorLiveData<List<QuestCompletion>>().apply {
        addSource(tagFilteredQuests) { updateFriendFilteredQuests() }
        addSource(_isEveryoneSelected) { updateFriendFilteredQuests() }
        addSource(_isMeSelected) { updateFriendFilteredQuests() }
        addSource(_selectedFriendIds) { updateFriendFilteredQuests() }
    }

    // Final filtered and sorted quests
    val filteredAndSortedQuests: LiveData<List<QuestCompletion>> = MediatorLiveData<List<QuestCompletion>>().apply {
        addSource(friendFilteredQuests) { updateFilteredAndSortedQuests() }
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

    // Filter summaries
    val tagFilterSummary: LiveData<String> = MediatorLiveData<String>().apply {
        addSource(_isAllTagsSelected) { updateTagFilterSummary() }
        addSource(_selectedTags) { updateTagFilterSummary() }
    }

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

    private fun updateTagFilteredQuests() {
        val quests = allQuests.value ?: return
        val isAllTags = _isAllTagsSelected.value ?: false
        val selectedTags = _selectedTags.value ?: emptySet()

        val filtered = when {
            isAllTags -> quests // Show all tags
            selectedTags.isEmpty() -> emptyList() // No tags selected
            else -> quests.filter { it.tag in selectedTags }
        }

        (tagFilteredQuests as MediatorLiveData).value = filtered
    }

    private fun updateFriendFilteredQuests() {
        val quests = tagFilteredQuests.value ?: return
        val isEveryone = _isEveryoneSelected.value ?: false
        val isMe = _isMeSelected.value ?: false
        val selectedFriends = _selectedFriendIds.value ?: emptySet()

        val filtered = when {
            isEveryone -> quests // Show all friends
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
        val sortBy = _sortOption.value ?: QuestCompletionSortOption.NEWEST_FIRST
        val friendsList = friends.value ?: emptyList()

        // Sort the filtered list
        val sorted = when (sortBy) {
            QuestCompletionSortOption.NEWEST_FIRST -> quests.sortedByDescending { it.timestamp }
            QuestCompletionSortOption.OLDEST_FIRST -> quests.sortedBy { it.timestamp }
            QuestCompletionSortOption.FRIEND_NAME -> quests.sortedWith { quest1, quest2 ->
                val name1 = getFriendDisplayName(quest1, friendsList)
                val name2 = getFriendDisplayName(quest2, friendsList)
                name1.compareTo(name2, ignoreCase = true)
            }
            QuestCompletionSortOption.QUEST_TAG -> quests.sortedBy { it.tag.displayName }
            QuestCompletionSortOption.EXPERIENCE_HIGH -> quests.sortedByDescending { it.experiencePoints }
            QuestCompletionSortOption.EXPERIENCE_LOW -> quests.sortedBy { it.experiencePoints }
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

    private fun updateTagFilterSummary() {
        val isAllTags = _isAllTagsSelected.value ?: false
        val selectedTags = _selectedTags.value ?: emptySet()

        val summary = when {
            isAllTags -> "All Tags"
            selectedTags.isEmpty() -> "No tags selected"
            selectedTags.size == 1 -> selectedTags.first().displayName
            selectedTags.size == QuestTag.values().size -> "All Tags"
            else -> "${selectedTags.size} tags selected"
        }

        (tagFilterSummary as MediatorLiveData).value = summary
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

    // Tag filter methods
    fun setAllTagsFilter(isSelected: Boolean) {
        if (isSelected) {
            // All Tags is exclusive - clear individual tag selections
            _isAllTagsSelected.value = true
            _selectedTags.value = emptySet()
        } else {
            _isAllTagsSelected.value = false
        }
    }

    fun toggleTagFilter(tag: QuestTag, isSelected: Boolean) {
        if (isSelected) {
            // Can't select individual tags with All Tags
            _isAllTagsSelected.value = false
            val currentSelected = _selectedTags.value ?: emptySet()
            _selectedTags.value = currentSelected + tag
        } else {
            val currentSelected = _selectedTags.value ?: emptySet()
            _selectedTags.value = currentSelected - tag
        }
    }

    // Friend filter methods
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

    fun setSortOption(option: QuestCompletionSortOption) {
        _sortOption.value = option
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
        (questCompletionRepository as? FirebaseQuestCompletionRepository)?.stopListening()
        friendRepository.stopListening()
    }
}

data class MapState(
    val isLocationEnabled: Boolean = false,
    val currentLocation: LatLng? = null,
    val zoomLevel: Double = 10.5
)
