package de.challenge3.questapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.challenge3.questapp.QuestApp
import de.challenge3.questapp.entities.DailyQuestActiveEntity
import de.challenge3.questapp.entities.DailyQuestPoolEntity
import de.challenge3.questapp.entities.PermQuestActiveEntity
import de.challenge3.questapp.entities.PermQuestPoolEntity
import de.challenge3.questapp.logik.quest.DailyQuests
import de.challenge3.questapp.logik.quest.PermQuests
import de.challenge3.questapp.ui.quest.DailyQuestPool
import de.challenge3.questapp.ui.quest.Quest

class SharedQuestViewModel : ViewModel() {
    private var _questList = MutableLiveData<List<Quest>>()
    val questList: LiveData<List<Quest>> get() = _questList
    val dailyQuestPoolDao = QuestApp.database?.dailyQuestPoolDao()
    val dailyQuestActiveDao = QuestApp.database?.dailyQuestActiveDao()
    val permQuestPoolDao = QuestApp.database?.permQuestPoolDao()
    val permQuestActiveDao = QuestApp.database?.permQuestActiveDao()

    init {
//        var combinedQuests = mutableListOf<Quest>()
//
//        // 3 zufällige Daily Quests generieren
//        var dailyQuests = DailyQuestPool.getRandomDailyQuests(3)
//        combinedQuests.addAll(dailyQuests)
//
//        // Beispiel für permanente Quests
////        combinedQuests.add(
////            Quest("s1", "Trainiere 3x", "Trainiere mindestens 3x diese Woche", 100, "Strength", 3, QuestType.NORMAL, 0)
////        )
////        combinedQuests.add(
////            Quest("s2", "Verbünde dich mit einem Freund", "Füge jemanden als Freund hinzu", 80, "Charisma", 2, QuestType.NORMAL, 0)
////        )
////        combinedQuests.add(
////            Quest("s3", "Yo mama", "Grab his ass", 2000, "Charisma", 2, QuestType.NORMAL, 0)
////        )
//        var permanentQuests = mutableListOf<Quest>()
//        for (permQuests in PermQuests.entries) {
//            permanentQuests.add(permQuests.quest)
//        }
//        permanentQuests.shuffle()
//        for (i in 1..3) {
//            combinedQuests.add(permanentQuests[i])
//        }
//
//        _questList.value = combinedQuests

        if (permQuestPoolDao!!.getAll().isEmpty()) {
            resetPermQuestsPool()
        }
        if (dailyQuestPoolDao!!.getAll().isEmpty()) {
            resetDailyQuestsPool()
        }
    }

    fun triggerUpdate() {
        _questList.value = _questList.value
    }

    fun removeQuest(tar: String, type: Quest.QuestType) {
        _questList.value = _questList.value?.filterNot { tar == it.id }
        if (type == Quest.QuestType.NORMAL) {
            permQuestActiveDao!!.dropAll(PermQuestActiveEntity(tar))
        } else {
            dailyQuestActiveDao!!.dropAll(DailyQuestActiveEntity(tar))
        }
    }

    fun addActiveDailyQuests(count: Int) {
        var remainingDailies : MutableList<DailyQuestPoolEntity> = dailyQuestPoolDao!!.getAll()
        if (remainingDailies.size < count) {
            resetDailyQuestsPool()
            remainingDailies = dailyQuestPoolDao.getAll()
        }
        remainingDailies.shuffle()
        for (i in 0..< count) {
            dailyQuestPoolDao.dropAll(remainingDailies[i])
            dailyQuestActiveDao!!.insertAll(DailyQuestActiveEntity(remainingDailies[i].id))
        }
        updateActiveQuests()
    }

    fun resetPermQuestsPool() {
        for (permQuests in PermQuests.entries) {
            permQuestPoolDao!!.insertAll(PermQuestPoolEntity(permQuests.quest.id))
        }
    }

    fun resetDailyQuestsPool() {
        for (dailyQuests in DailyQuests.entries) {
            dailyQuestPoolDao!!.insertAll(DailyQuestPoolEntity(dailyQuests.quest.id))
        }
    }

    fun addActivePermQuests(count: Int) {
        var remainingQuests = permQuestPoolDao!!.getAll()
        if (remainingQuests.size < count) {
            resetPermQuestsPool()
            remainingQuests = permQuestPoolDao.getAll()
        }
        remainingQuests.shuffle()
        for (i in 0..< count) {
            println("0<=$i<$count")
            permQuestPoolDao.dropAll(remainingQuests[i])
            permQuestActiveDao!!.insertAll(PermQuestActiveEntity(remainingQuests[i].id))
        }
        updateActiveQuests()
    }

    fun updateActiveQuests() {
        var combinedQuests = mutableListOf<Quest>()
        for (dailyActive in dailyQuestActiveDao!!.getAll()) {
            for (dailyQuests in DailyQuests.entries) {
                if (dailyActive.id == dailyQuests.quest.id) {
                    combinedQuests.add(dailyQuests.quest)
                }
            }
        }

        for (permActive in permQuestActiveDao!!.getAll()) {
            println("active quest: $permActive")
            for (permQuests in PermQuests.entries) {
                if (permActive.id == permQuests.quest.id) {
                    combinedQuests.add(permQuests.quest)
                }
            }
        }
        _questList.value = combinedQuests
    }

//    fun addExperience(tar: String, exp: Int) {
//        statsManager.addExperience(tar, exp)
//    }
}