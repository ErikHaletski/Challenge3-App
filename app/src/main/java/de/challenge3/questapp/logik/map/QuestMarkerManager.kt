package de.challenge3.questapp.logik.map

import android.graphics.PointF
import de.challenge3.questapp.ui.home.QuestCompletion
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

class QuestMarkerManager(
    private val map: MapLibreMap,
    private val mapView: MapView,
    private val iconId: String,
    private val onQuestClick: (QuestCompletion, PointF) -> Unit
) : QuestMarkerHandler {

    private var symbolManager: SymbolManager? = null
    private val questSymbolMap = mutableMapOf<Symbol, QuestCompletion>()

    fun initialize(style: Style) {
        symbolManager?.onDestroy()
        symbolManager = SymbolManager(mapView, map, style).apply {
            iconAllowOverlap = true
            textAllowOverlap = true
        }

        symbolManager!!.addClickListener { symbol ->
            questSymbolMap[symbol]?.let { quest ->
                val screenPoint = map.projection.toScreenLocation(symbol.latLng)
                onQuestClick(quest, screenPoint)
            }
            true
        }
    }

    override fun addQuestMarkers(quests: List<QuestCompletion>) {
        symbolManager?.deleteAll()
        questSymbolMap.clear()

        quests.forEach { quest ->
            val symbol = symbolManager!!.create(
                SymbolOptions()
                    .withLatLng(LatLng(quest.lat, quest.lng))
                    .withIconImage(iconId)
                    .withIconSize(1.0f)
            )
            questSymbolMap[symbol] = quest
        }
    }

    override fun clearMarkers() {
        symbolManager?.deleteAll()
        questSymbolMap.clear()
    }

    fun onDestroy() {
        symbolManager?.onDestroy()
    }
}
