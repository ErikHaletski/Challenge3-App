package de.challenge3.questapp.models

import java.text.SimpleDateFormat
import java.util.*

data class Friend(
    val id: String,
    val username: String,
    val email: String,
    val profileImageUrl: String? = null,
    val level: Int = 1,
    val totalExperience: Int = 0,
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val completedQuestsCount: Int = 0
) {
    val displayName: String
        get() = username.ifEmpty { email.substringBefore("@") }

    val levelProgress: Float
        get() = (totalExperience % 1000) / 1000f // Assuming 1000 XP per level

    val lastSeenFormatted: String
        get() = if (isOnline) {
            "Online"
        } else {
            val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            "Last seen: ${formatter.format(Date(lastSeen))}"
        }
}

enum class FriendshipStatus {
    PENDING_SENT,
    PENDING_RECEIVED,
    ACCEPTED,
    BLOCKED
}

data class FriendRequest(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val status: FriendshipStatus,
    val timestamp: Long,
    val fromUser: Friend? = null
) {
    val timeAgo: String
        get() {
            val diff = System.currentTimeMillis() - timestamp
            val minutes = diff / (1000 * 60)
            val hours = minutes / 60
            val days = hours / 24

            return when {
                days > 0 -> "${days}d ago"
                hours > 0 -> "${hours}h ago"
                minutes > 0 -> "${minutes}m ago"
                else -> "Just now"
            }
        }
}
