package de.challenge3.questapp.logik.map

import de.challenge3.questapp.ui.home.QuestCompletion
import org.maplibre.android.maps.Style

interface MarkerController {
    fun initialize(style: Style)
    fun updateMarkers(quests: List<QuestCompletion>)
    fun clearMarkers()
    fun onDestroy()
}
