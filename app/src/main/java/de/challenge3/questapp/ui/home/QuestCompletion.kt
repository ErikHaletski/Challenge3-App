package de.challenge3.questapp.ui.home

data class QuestCompletion(
    val lat: Double,
    val lng: Double,
    val timestamp: Long,
    val questText: String,
    val tag: String
)
