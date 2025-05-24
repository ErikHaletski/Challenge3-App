package de.challenge3.questapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.challenge3.questapp.ui.home.QuestCompletion
import de.challenge3.questapp.ui.home.QuestTag

interface QuestRepository {
    fun getCompletedQuests(): LiveData<List<QuestCompletion>>
    suspend fun addCompletedQuest(quest: QuestCompletion)
    suspend fun deleteQuest(questId: String)
    fun getQuestById(id: String): QuestCompletion?
}

class QuestRepositoryImpl : QuestRepository {
    private val _completedQuests = MutableLiveData<List<QuestCompletion>>()
    private val questsCache = mutableListOf<QuestCompletion>()

    init {
        // Initialize with sample data
        questsCache.addAll(getSampleQuests())
        _completedQuests.value = questsCache.toList()
    }

    override fun getCompletedQuests(): LiveData<List<QuestCompletion>> = _completedQuests

    override suspend fun addCompletedQuest(quest: QuestCompletion) {
        questsCache.add(quest)
        _completedQuests.postValue(questsCache.toList())
    }

    override suspend fun deleteQuest(questId: String) {
        questsCache.removeAll { it.id == questId }
        _completedQuests.postValue(questsCache.toList())
    }

    override fun getQuestById(id: String): QuestCompletion? {
        return questsCache.find { it.id == id }
    }

    private fun getSampleQuests() = listOf(
        QuestCompletion(
            lat = 52.5200, lng = 13.4050,
            timestamp = System.currentTimeMillis(),
            questText = "Defeat the Goblin King",
            tag = QuestTag.MIGHT,
            experiencePoints = 150
        ),
        QuestCompletion(
            lat = 48.8566, lng = 2.3522,
            timestamp = System.currentTimeMillis() - 3600000,
            questText = "Solve the ancient riddle",
            tag = QuestTag.MIND,
            experiencePoints = 100
        ),
        QuestCompletion(
            lat = 51.5074, lng = -0.1278,
            timestamp = System.currentTimeMillis() - 7200000,
            questText = "Heal the wounded traveler",
            tag = QuestTag.HEART,
            experiencePoints = 75
        ),
        QuestCompletion(
            lat = 54.5074, lng = -0.7278,
            timestamp = System.currentTimeMillis() - 5000000,
            questText = "Meditate for 10 minutes",
            tag = QuestTag.SPIRIT,
            experiencePoints = 75
        )
    )
}
