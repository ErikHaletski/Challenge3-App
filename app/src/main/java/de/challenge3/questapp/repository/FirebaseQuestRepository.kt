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

class FirebaseQuestRepository : QuestRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val questsCollection = firestore.collection("completed_quests")

    private val _completedQuests = MutableLiveData<List<QuestCompletion>>()
    private var questsListener: ListenerRegistration? = null

    init {
        startListening()
    }

    private fun startListening() {
        questsListener = questsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error - you might want to log this
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
                            experiencePoints = document.getLong("experiencePoints")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        // Skip invalid documents
                        null
                    }
                } ?: emptyList()

                _completedQuests.value = quests
            }
    }

    override fun getCompletedQuests(): LiveData<List<QuestCompletion>> = _completedQuests

    override suspend fun addCompletedQuest(quest: QuestCompletion) {
        try {
            val questData = mapOf(
                "lat" to quest.lat,
                "lng" to quest.lng,
                "timestamp" to quest.timestamp,
                "questText" to quest.questText,
                "tag" to quest.tag.name,
                "experiencePoints" to quest.experiencePoints
            )

            if (quest.id.isNotEmpty() && quest.id != UUID.randomUUID().toString()) {
                // Update existing quest
                questsCollection.document(quest.id).set(questData).await()
            } else {
                // Add new quest
                questsCollection.add(questData).await()
            }
        } catch (e: Exception) {
            // Handle error - you might want to throw or log this
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
    }
}