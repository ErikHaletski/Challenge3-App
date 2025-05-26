package de.challenge3.questapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.challenge3.questapp.ui.quest.DailyQuestPool
import de.challenge3.questapp.ui.quest.Quest
import de.challenge3.questapp.ui.quest.Quest.QuestType

class HomeViewModel : ViewModel() {

    private val _questList = MutableLiveData<List<Quest>>()
    val questList: LiveData<List<Quest>> = _questList

    init {
        val combinedQuests = mutableListOf<Quest>()

        // 3 zufällige Daily Quests generieren
        val dailyQuests = DailyQuestPool.getRandomDailyQuests(3)
        combinedQuests.addAll(dailyQuests)

        // Beispiel für permanente Quests
        combinedQuests.add(
            Quest("s1", "Trainiere 3x", "Trainiere mindestens 3x diese Woche", 100, "Strength", 3, QuestType.NORMAL, 0)
        )
        combinedQuests.add(
            Quest("s2", "Verbünde dich mit einem Freund", "Füge jemanden als Freund hinzu", 80, "Charisma", 2, QuestType.NORMAL, 0)
        )
        combinedQuests.add(
            Quest("s3", "Yo mama", "Grab his ass", 2000, "Charisma", 2, QuestType.NORMAL, 0)
        )

        _questList.value = combinedQuests
    }

    fun triggerUpdate() {
        _questList.value = _questList.value
    }
}
