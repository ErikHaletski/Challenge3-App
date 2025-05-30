package de.challenge3.questapp.logik.map

import android.graphics.PointF
import de.challenge3.questapp.ui.home.QuestCompletion
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

class QuestCompletionMarkerManager(
    private val map: MapLibreMap,
    private val mapView: MapView,
    private val iconId: String,
    private val onQuestClick: (QuestCompletion, PointF) -> Unit
) : MarkerController {

    private var symbolManager: SymbolManager? = null
    private val questSymbolMap = mutableMapOf<Symbol, QuestCompletion>()

    override fun initialize(style: Style) {
        cleanup()
        symbolManager = SymbolManager(mapView, map, style).apply {
            iconAllowOverlap = true
            textAllowOverlap = true
            addClickListener(::handleMarkerClick)
        }
    }

    override fun updateMarkers(quests: List<QuestCompletion>) {
        symbolManager?.deleteAll()
        questSymbolMap.clear()
        quests.forEach(::createMarkerForQuest)
    }

    private fun createMarkerForQuest(quest: QuestCompletion) {
        val symbol = symbolManager?.create(
            SymbolOptions()
                .withLatLng(quest.location)
                .withIconImage(iconId)
                .withIconSize(1.0f)
        )
        symbol?.let { questSymbolMap[it] = quest }
    }

    private fun handleMarkerClick(symbol: Symbol): Boolean {
        questSymbolMap[symbol]?.let { quest ->
            val screenPoint = map.projection.toScreenLocation(symbol.latLng)
            onQuestClick(quest, screenPoint)
        }
        return true
    }

    override fun clearMarkers() {
        symbolManager?.deleteAll()
        questSymbolMap.clear()
    }

    private fun cleanup() {
        symbolManager?.onDestroy()
        questSymbolMap.clear()
    }

    override fun onDestroy() {
        cleanup()
    }
}
