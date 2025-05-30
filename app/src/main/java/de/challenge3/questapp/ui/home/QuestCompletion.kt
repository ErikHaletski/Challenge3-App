package de.challenge3.questapp.ui.home

import org.maplibre.android.geometry.LatLng
import java.text.SimpleDateFormat
import java.util.*

data class QuestCompletion(
    val id: String = UUID.randomUUID().toString(),
    val lat: Double,
    val lng: Double,
    val timestamp: Long,
    val questText: String,
    val tag: QuestTag,
    val experiencePoints: Int = 0,
    val userId: String = "",
    val username: String = ""
) {
    val formattedTimestamp: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(timestamp))

    val location: LatLng
        get() = LatLng(lat, lng)
}

enum class QuestTag(val displayName: String) {
    MIGHT("Might"),
    MIND("Mind"),
    HEART("Heart"),
    SPIRIT("Spirit")
}
