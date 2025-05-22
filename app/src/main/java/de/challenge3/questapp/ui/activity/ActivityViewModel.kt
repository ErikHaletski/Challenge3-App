package de.challenge3.questapp.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.challenge3.questapp.ui.home.QuestCompletion
import java.text.SimpleDateFormat
import java.util.*

class ActivityViewModel : ViewModel() {

    private val _completedQuests = MutableLiveData<List<QuestCompletion>>().apply {
        value = listOf(
            QuestCompletion(52.5200, 13.4050, System.currentTimeMillis(), "Defeat the Goblin King", "might"),
            QuestCompletion(48.8566, 2.3522, System.currentTimeMillis() - 3600000, "Solve the ancient riddle", "mind"),
            QuestCompletion(51.5074, -0.1278, System.currentTimeMillis() - 7200000, "Heal the wounded traveler", "heart")
        )
    }
    val completedQuests: LiveData<List<QuestCompletion>> = _completedQuests

    fun getQuestInfoText(quest: QuestCompletion): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val formattedTime = dateFormat.format(Date(quest.timestamp))
        return """
            ${quest.tag.uppercase()}
            ${quest.questText}
            Finished at: $formattedTime
        """.trimIndent()
    }
}