package de.challenge3.questapp.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.challenge3.questapp.ui.home.QuestCompletion
import de.challenge3.questapp.repository.QuestRepository
import de.challenge3.questapp.repository.QuestRepositoryImpl
import org.maplibre.android.geometry.LatLng

class ActivityViewModel(
    private val questRepository: QuestRepository = QuestRepositoryImpl()
) : ViewModel() {

    val completedQuests: LiveData<List<QuestCompletion>> = questRepository.getCompletedQuests()

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
        return """
            ${quest.tag.displayName.uppercase()}
            ${quest.questText}
            XP: ${quest.experiencePoints}
            Completed: ${quest.formattedTimestamp}
        """.trimIndent()
    }

    fun updateMapState(state: MapState) {
        _mapState.value = state
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up resources
    }
}

data class MapState(
    val isLocationEnabled: Boolean = false,
    val currentLocation: LatLng? = null,
    val zoomLevel: Double = 10.5
)
