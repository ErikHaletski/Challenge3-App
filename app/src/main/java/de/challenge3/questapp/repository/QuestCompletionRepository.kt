package de.challenge3.questapp.repository

import androidx.lifecycle.LiveData
import de.challenge3.questapp.ui.home.QuestCompletion

interface QuestCompletionRepository {
    fun getCompletedQuests(): LiveData<List<QuestCompletion>>
    fun getQuestsForUserAndFriends(userId: String, friendIds: List<String>): LiveData<List<QuestCompletion>>
    suspend fun addCompletedQuest(quest: QuestCompletion)
    suspend fun deleteQuest(questId: String)
    fun getQuestById(id: String): QuestCompletion?
}
