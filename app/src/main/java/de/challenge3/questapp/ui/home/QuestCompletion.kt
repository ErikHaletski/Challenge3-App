data class QuestCompletion(
    val lat: Double,
    val lng: Double,
    val timestamp: Long, // UNIX time in millis
    val questText: String,
    val tag: String
)
