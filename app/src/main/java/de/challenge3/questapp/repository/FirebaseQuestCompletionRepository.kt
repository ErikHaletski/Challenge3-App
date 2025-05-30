package de.challenge3.questapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import de.challenge3.questapp.ui.home.QuestCompletion
import de.challenge3.questapp.ui.home.QuestTag
import kotlinx.coroutines.tasks.await
import java.util.UUID

// handels all quest completion data
// -> like: storing completed quests with location data, retrieving quests for users/friends
// -> like: real-time updates, filtering/querying
class FirebaseQuestCompletionRepository : QuestCompletionRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val questsCollection = firestore.collection("completed_quests")

    private val _completedQuests = MutableLiveData<List<QuestCompletion>>()
    private val _filteredQuests = MutableLiveData<List<QuestCompletion>>()
    private var questsListener: ListenerRegistration? = null
    private var filteredQuestsListener: ListenerRegistration? = null

    init {
        startListening()
    }

    private fun startListening() {
        questsListener = questsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val quests = snapshot?.documents?.mapNotNull { document ->
                    try {
                        QuestCompletion(
                            id = document.id,
                            lat = document.getDouble("lat") ?: 0.0,
                            lng = document.getDouble("lng") ?: 0.0,
                            timestamp = document.getLong("timestamp") ?: 0L,
                            questText = document.getString("questText") ?: "",
                            tag = QuestTag.valueOf(document.getString("tag") ?: "MIGHT"),
                            experiencePoints = document.getLong("experiencePoints")?.toInt() ?: 0,
                            userId = document.getString("userId") ?: "",
                            username = document.getString("username") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                _completedQuests.value = quests
            }
    }

    override fun getCompletedQuests(): LiveData<List<QuestCompletion>> = _completedQuests

    override fun getQuestsForUserAndFriends(userId: String, friendIds: List<String>): LiveData<List<QuestCompletion>> {
        // Stop previous listener if exists
        filteredQuestsListener?.remove()

        val allowedUserIds = listOf(userId) + friendIds

        if (allowedUserIds.isEmpty()) {
            _filteredQuests.value = emptyList()
            return _filteredQuests
        }

        // Firebase 'in' queries are limited to 10 items, so we need to handle this
        if (allowedUserIds.size <= 10) {
            filteredQuestsListener = questsCollection
                .whereIn("userId", allowedUserIds)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    val quests = snapshot?.documents?.mapNotNull { document ->
                        try {
                            QuestCompletion(
                                id = document.id,
                                lat = document.getDouble("lat") ?: 0.0,
                                lng = document.getDouble("lng") ?: 0.0,
                                timestamp = document.getLong("timestamp") ?: 0L,
                                questText = document.getString("questText") ?: "",
                                tag = QuestTag.valueOf(document.getString("tag") ?: "MIGHT"),
                                experiencePoints = document.getLong("experiencePoints")?.toInt() ?: 0,
                                userId = document.getString("userId") ?: "",
                                username = document.getString("username") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                    _filteredQuests.value = quests
                }
        } else {
            // For more than 10 users, we need to make multiple queries or filter client-side
            // For now, let's filter client-side from all quests
            filteredQuestsListener = questsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    val quests = snapshot?.documents?.mapNotNull { document ->
                        try {
                            val questUserId = document.getString("userId") ?: ""
                            if (questUserId in allowedUserIds) {
                                QuestCompletion(
                                    id = document.id,
                                    lat = document.getDouble("lat") ?: 0.0,
                                    lng = document.getDouble("lng") ?: 0.0,
                                    timestamp = document.getLong("timestamp") ?: 0L,
                                    questText = document.getString("questText") ?: "",
                                    tag = QuestTag.valueOf(document.getString("tag") ?: "MIGHT"),
                                    experiencePoints = document.getLong("experiencePoints")?.toInt() ?: 0,
                                    userId = questUserId,
                                    username = document.getString("username") ?: ""
                                )
                            } else null
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                    _filteredQuests.value = quests
                }
        }

        return _filteredQuests
    }

    override suspend fun addCompletedQuest(quest: QuestCompletion) {
        try {
            val questData = mapOf(
                "lat" to quest.lat,
                "lng" to quest.lng,
                "timestamp" to quest.timestamp,
                "questText" to quest.questText,
                "tag" to quest.tag.name,
                "experiencePoints" to quest.experiencePoints,
                "userId" to quest.userId,
                "username" to quest.username
            )

            if (quest.id.isNotEmpty() && quest.id != UUID.randomUUID().toString()) {
                questsCollection.document(quest.id).set(questData).await()
            } else {
                questsCollection.add(questData).await()
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    override suspend fun deleteQuest(questId: String) {
        try {
            questsCollection.document(questId).delete().await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    override fun getQuestById(id: String): QuestCompletion? {
        return _completedQuests.value?.find { it.id == id }
    }

    fun stopListening() {
        questsListener?.remove()
        filteredQuestsListener?.remove()
    }
}
