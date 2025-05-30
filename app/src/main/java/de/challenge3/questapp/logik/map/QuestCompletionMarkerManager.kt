package de.challenge3.questapp.logik.map

import android.content.Context
import android.graphics.PointF
import androidx.core.content.ContextCompat
import de.challenge3.questapp.R
import de.challenge3.questapp.ui.home.QuestCompletion
import de.challenge3.questapp.ui.home.QuestTag
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
    private val context: Context = mapView.context

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
        // Verwende deine definierten Quest-Tag-Farben
        val textColor = getTagColorHex(quest.tag)

        val symbol = symbolManager?.create(
            SymbolOptions()
                .withLatLng(quest.location)
                .withIconImage(iconId)
                .withIconSize(1.0f)
                // Text-Label für den Titel hinzufügen
                .withTextField(quest.displayTitle)
                .withTextSize(13f)                      // Optimale Größe für Lesbarkeit
                .withTextColor(textColor)               // Deine Quest-Tag-Farben
                .withTextHaloColor("#000000")           // Schwarzer Halo für Kontrast
                .withTextHaloWidth(2.0f)                // Optimale Halo-Breite
                .withTextOffset(arrayOf(0f, -2.2f))     // Text über dem Icon
                .withTextAnchor("bottom")               // Text unter dem Anchor-Punkt
                .withTextJustify("center")              // Zentrierter Text
                .withTextMaxWidth(12f)                  // Mehr Platz für längere Titel
                .withTextFont(arrayOf("Roboto Medium", "Arial Unicode MS Bold"))
                .withTextLetterSpacing(0.05f)           // Leicht erhöhter Buchstabenabstand
        )
        symbol?.let { questSymbolMap[it] = quest }
    }

    // Verwende deine definierten Quest-Tag-Farben aus colors.xml
    private fun getTagColorHex(tag: QuestTag): String {
        val colorRes = when (tag) {
            QuestTag.MIGHT -> R.color.tag_might    // #4CAF50 (Grün)
            QuestTag.MIND -> R.color.tag_mind      // #2196F3 (Blau)
            QuestTag.HEART -> R.color.tag_heart    // #E91E63 (Pink)
            QuestTag.SPIRIT -> R.color.tag_spirit  // #9C27B0 (Lila)
        }

        val color = ContextCompat.getColor(context, colorRes)
        return String.format("#%06X", 0xFFFFFF and color)
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
