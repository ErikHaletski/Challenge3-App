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
                if (error != null) return@addSnapshotListener

                val quests = snapshot?.documents?.mapNotNull { createQuestCompletion(it) } ?: emptyList()
                _completedQuests.value = quests
            }
    }

    private fun createQuestCompletion(document: com.google.firebase.firestore.DocumentSnapshot): QuestCompletion? {
        return try {
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
    }

    override fun getCompletedQuests(): LiveData<List<QuestCompletion>> = _completedQuests

    override fun getQuestsForUserAndFriends(userId: String, friendIds: List<String>): LiveData<List<QuestCompletion>> {
        filteredQuestsListener?.remove()

        val allowedUserIds = listOf(userId) + friendIds
        if (allowedUserIds.isEmpty()) {
            _filteredQuests.value = emptyList()
            return _filteredQuests
        }

        if (allowedUserIds.size <= 10) {
            // Use Firebase 'in' query for <= 10 users
            filteredQuestsListener = questsCollection
                .whereIn("userId", allowedUserIds)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val quests = snapshot?.documents?.mapNotNull { createQuestCompletion(it) } ?: emptyList()
                    _filteredQuests.value = quests
                }
        } else {
            // Filter client-side for > 10 users
            filteredQuestsListener = questsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val quests = snapshot?.documents?.mapNotNull { document ->
                        createQuestCompletion(document)?.takeIf { it.userId in allowedUserIds }
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
            // Handle error appropriately
        }
    }

    override suspend fun deleteQuest(questId: String) {
        try {
            questsCollection.document(questId).delete().await()
        } catch (e: Exception) {
            // Handle error appropriately
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
