package de.challenge3.questapp.ui.achievements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.challenge3.questapp.logik.achievements.Achievements

data class AchievementItem(
    val achievement: Achievements,
    var unlocked: Boolean = false
)

class AchievmentsViewModel : ViewModel() {
    private val _achievements = MutableLiveData<List<AchievementItem>>()

    val achievements: LiveData<List<AchievementItem>> = _achievements

    init {
        _achievements.value = Achievements.values().map {
            AchievementItem(it)
        }
    }

    fun toggleAchievement(achievement: Achievements) {
        _achievements.value = _achievements.value?.map {
            if (it.achievement == achievement) it.copy(unlocked = true) else it
        }
    }
}
