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
    private var questsListener: ListenerRegistration? = null

    private var isListening = false

    init {
        startListening()
    }

    private fun startListening() {
        if (isListening) return
        isListening = true

        println("FirebaseQuestCompletionRepository: Starting to listen to ALL completed quests")

        questsListener = questsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error listening to quests: ${error.message}")
                    return@addSnapshotListener
                }

                val quests = snapshot?.documents?.mapNotNull { createQuestCompletion(it) } ?: emptyList()
                println("FirebaseQuestCompletionRepository: Received ${quests.size} quests from Firestore")
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
                questTitle = document.getString("questTitle") ?: "",
                tag = QuestTag.valueOf(document.getString("tag") ?: "MIGHT"),
                experiencePoints = document.getLong("experiencePoints")?.toInt() ?: 0,
                userId = document.getString("userId") ?: "",
                username = document.getString("username") ?: ""
            )
        } catch (e: Exception) {
            println("Error creating QuestCompletion: ${e.message}")
            null
        }
    }

    override fun getCompletedQuests(): LiveData<List<QuestCompletion>> = _completedQuests

    override fun getQuestsForUserAndFriends(userId: String, friendIds: List<String>): LiveData<List<QuestCompletion>> {
        println("FirebaseQuestCompletionRepository: getQuestsForUserAndFriends called for user $userId with ${friendIds.size} friends")
        return _completedQuests
    }

    override suspend fun addCompletedQuest(quest: QuestCompletion) {
        try {
            val questData = mapOf(
                "lat" to quest.lat,
                "lng" to quest.lng,
                "timestamp" to quest.timestamp,
                "questText" to quest.questText,
                "questTitle" to quest.questTitle,
                "tag" to quest.tag.name,
                "experiencePoints" to quest.experiencePoints,
                "userId" to quest.userId,
                "username" to quest.username
            )

            println("Adding quest to Firestore: ${quest.questTitle} - ${quest.questText}")

            if (quest.id.isNotEmpty() && quest.id != UUID.randomUUID().toString()) {
                questsCollection.document(quest.id).set(questData).await()
            } else {
                val docRef = questsCollection.add(questData).await()
                println("Quest added with ID: ${docRef.id}")
            }
        } catch (e: Exception) {
            println("Error adding quest to Firestore: ${e.message}")
            throw e
        }
    }

    override suspend fun deleteQuest(questId: String) {
        try {
            questsCollection.document(questId).delete().await()
            println("Quest deleted: $questId")
        } catch (e: Exception) {
            println("Error deleting quest: ${e.message}")
            throw e
        }
    }

    override fun getQuestById(id: String): QuestCompletion? {
        return _completedQuests.value?.find { it.id == id }
    }

    fun stopListening() {
        questsListener?.remove()
        isListening = false
        println("Stopped listening to Firestore")
    }

    fun forceRefresh() {
        println("Force refreshing quest data...")
        stopListening()
        isListening = false
        startListening()
    }
}
