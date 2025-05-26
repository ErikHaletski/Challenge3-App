package de.challenge3.questapp.logik.map

import de.challenge3.questapp.ui.home.QuestCompletion

interface QuestMarkerHandler {
    fun addQuestMarkers(quests: List<QuestCompletion>)
    fun clearMarkers()
}
