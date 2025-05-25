package de.challenge3.questapp.models

import java.text.SimpleDateFormat
import java.util.*

data class Friend(
    val id: String,
    val username: String,
    val email: String,
    val level: Int,
    val totalExperience: Int,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val completedQuestsCount: Int = 0
) {
    val displayName: String
        get() = username.ifEmpty { email.substringBefore("@") }

    val levelProgress: Float
        get() {
            val baseXpForLevel = level * 1000
            val xpInCurrentLevel = totalExperience % 1000
            return xpInCurrentLevel / 1000f
        }

    val lastSeenFormatted: String
        get() = when {
            isOnline -> "Online"
            lastSeen != null -> {
                val timeDiff = System.currentTimeMillis() - lastSeen
                when {
                    timeDiff < 60000 -> "Just now"
                    timeDiff < 3600000 -> "${timeDiff / 60000}m ago"
                    timeDiff < 86400000 -> "${timeDiff / 3600000}h ago"
                    timeDiff < 604800000 -> "${timeDiff / 86400000}d ago"
                    else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(lastSeen))
                }
            }
            else -> "Offline"
        }
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
            val timeDiff = System.currentTimeMillis() - timestamp
            return when {
                timeDiff < 60000 -> "Just now"
                timeDiff < 3600000 -> "${timeDiff / 60000}m ago"
                timeDiff < 86400000 -> "${timeDiff / 3600000}h ago"
                timeDiff < 604800000 -> "${timeDiff / 86400000}d ago"
                else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
            }
        }
}

enum class FriendshipStatus {
    PENDING_SENT,
    PENDING_RECEIVED,
    ACCEPTED,
    DECLINED
}
