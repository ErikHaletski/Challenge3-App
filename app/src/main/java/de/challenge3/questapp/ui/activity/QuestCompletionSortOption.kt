package de.challenge3.questapp.ui.activity

enum class QuestCompletionSortOption(val displayName: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    FRIEND_NAME("Friend Name"),
    QUEST_TAG("Quest Tag"),
    EXPERIENCE_HIGH("Highest XP"),
    EXPERIENCE_LOW("Lowest XP");

    companion object {
        fun getDisplayNames(): Array<String> {
            return values().map { it.displayName }.toTypedArray()
        }
    }
}
