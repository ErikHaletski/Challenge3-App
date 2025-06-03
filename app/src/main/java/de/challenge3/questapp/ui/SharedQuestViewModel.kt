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
import de.challenge3.questapp.logik.stats.Attributes
import de.challenge3.questapp.ui.quest.Quest

class SharedQuestViewModel : ViewModel() {
    private var _questList = MutableLiveData<List<Quest>>()
    val questList: LiveData<List<Quest>> get() = _questList
    val dailyQuestPoolDao = QuestApp.database?.dailyQuestPoolDao()
    val dailyQuestActiveDao = QuestApp.database?.dailyQuestActiveDao()
    val permQuestPoolDao = QuestApp.database?.permQuestPoolDao()
    val permQuestActiveDao = QuestApp.database?.permQuestActiveDao()
    val achievementsDao = QuestApp.database?.achievementsDao()


    // initial vergewissern dass der questpool gefüllt ist
    init {
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


    // entfernen einer quest aus der list und dem entsprechendem akitvem pool
    fun removeQuest(tar: String, type: Quest.QuestType) {
        _questList.value = _questList.value?.filterNot { tar == it.id }
        if (type == Quest.QuestType.NORMAL) {
            permQuestActiveDao!!.dropAll(PermQuestActiveEntity(tar))
        } else {
            dailyQuestActiveDao!!.dropAll(DailyQuestActiveEntity(tar))
        }
    }


    // hinzufügen von 3 zufälligen daily quests
    fun addActiveDailyQuests(count: Int) {
        var remainingDailies : MutableList<DailyQuestPoolEntity> = dailyQuestPoolDao!!.getAll()

        // wenn weniger quests als benötigt ausgegeben würden wird der pool resettet und neue quests ausgesucht.
        // Bedeutet auch: mit Pech könnten einige quests für längere Zeit nicht dran kommen. Unwahrscheinlich
        if (remainingDailies.size < count) {
            resetDailyQuestsPool()
            remainingDailies = dailyQuestPoolDao.getAll()
        }
        // shuffle + aussuch der ersten i sorgt für zufällige auswahl der quests
        remainingDailies.shuffle()
        for (i in 0..< count) {
            dailyQuestPoolDao.dropAll(remainingDailies[i])
            dailyQuestActiveDao!!.insertAll(DailyQuestActiveEntity(remainingDailies[i].id))
        }
        updateActiveQuests()
    }


    // refresh der Quest Pools
    fun resetPermQuestsPool() {
        for (permQuests in PermQuests.entries) {
            permQuestPoolDao!!.insertAll(PermQuestPoolEntity(permQuests.quest.id, permQuests.quest.statType, permQuests.reqLvl))
        }
    }

    fun resetDailyQuestsPool() {
        for (dailyQuests in DailyQuests.entries) {
            dailyQuestPoolDao!!.insertAll(DailyQuestPoolEntity(dailyQuests.quest.id))
        }
    }


    // hinzufügen von 3 zufälligen permanenten quests unter anbetracht der erfüllten korrespondierenden achievements
    fun addActivePermQuests(count: Int) {
        var remainingQuests = mutableListOf<PermQuestPoolEntity>()
        for (attribute in Attributes.entries) {
            if (attribute.attType == 3) {
                var filteredQuests = permQuestPoolDao!!.getAllAllowed(attribute.name, achievementsDao!!.getCountOf(attribute.name))
                remainingQuests.addAll(filteredQuests)
            }
        }

        // wenn weniger quests als benötigt ausgegeben würden wird der pool resettet und neue quests ausgesucht.
        // Bedeutet auch: mit Pech könnten einige quests für längere Zeit nicht dran kommen. Unwahrscheinlich
        if (remainingQuests.size < count) {
            resetPermQuestsPool()
            for (attribute in Attributes.entries) {
                if (attribute.attType == 3) {
                    var filteredQuests = permQuestPoolDao!!.getAllAllowed(attribute.name, achievementsDao!!.getCountOf(attribute.name))
                    remainingQuests.addAll(filteredQuests)
                }
            }
        }
        // shuffle + aussuch der ersten i sorgt für zufällige auswahl der quests
        remainingQuests.shuffle()
        for (i in 0..< count) {
            permQuestPoolDao!!.dropAll(remainingQuests[i])
            permQuestActiveDao!!.insertAll(PermQuestActiveEntity(remainingQuests[i].id))
        }
        updateActiveQuests()
    }


    // update der questliste im View, garantieren der Sichtbarkeit im Screen
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
}