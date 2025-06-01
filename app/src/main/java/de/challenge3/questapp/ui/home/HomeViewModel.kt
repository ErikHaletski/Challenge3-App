package de.challenge3.questapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.challenge3.questapp.logik.quest.PermQuests
import de.challenge3.questapp.logik.stats.StatsManager
import de.challenge3.questapp.ui.quest.DailyQuestPool
import de.challenge3.questapp.ui.quest.Quest
import de.challenge3.questapp.ui.quest.Quest.QuestType

class HomeViewModel() : ViewModel() {

    private var _questList = MutableLiveData<List<Quest>>()
    val questList: LiveData<List<Quest>> get() = _questList

    init {
        var combinedQuests = mutableListOf<Quest>()

        // 3 zufällige Daily Quests generieren
        var dailyQuests = DailyQuestPool.getRandomDailyQuests(3)
        combinedQuests.addAll(dailyQuests)

        // Beispiel für permanente Quests
//        combinedQuests.add(
//            Quest("s1", "Trainiere 3x", "Trainiere mindestens 3x diese Woche", 100, "Strength", 3, QuestType.NORMAL, 0)
//        )
//        combinedQuests.add(
//            Quest("s2", "Verbünde dich mit einem Freund", "Füge jemanden als Freund hinzu", 80, "Charisma", 2, QuestType.NORMAL, 0)
//        )
//        combinedQuests.add(
//            Quest("s3", "Yo mama", "Grab his ass", 2000, "Charisma", 2, QuestType.NORMAL, 0)
//        )
        var permanentQuests = mutableListOf<Quest>()
        for (permQuests in PermQuests.entries) {
            permanentQuests.add(permQuests.quest)
        }
        permanentQuests.shuffle()
        for (i in 1..3) {
            combinedQuests.add(permanentQuests[i])
        }

        _questList.value = combinedQuests

    }

    fun triggerUpdate() {
        _questList.value = _questList.value
    }

    fun removeQuest(tar: String) {
        _questList.value = _questList.value?.filterNot { tar == it.id }
    }

//    fun addExperience(tar: String, exp: Int) {
//        statsManager.addExperience(tar, exp)
//    }
}
