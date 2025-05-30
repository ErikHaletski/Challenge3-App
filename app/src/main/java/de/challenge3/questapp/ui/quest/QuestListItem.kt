//Datenklasse für den QuestAdapter
// Für im RecyclerView die Mischung von Headern und Quests.
//Macht auf- und zuklappbare Kategorien möglich.

package de.challenge3.questapp.ui.quest

sealed class QuestListItem {
    enum class HeaderType {
        DAILY, PERMANENT
    }

    data class Header(val title: String, val type: HeaderType) : QuestListItem()
    data class QuestItem(val quest: Quest) : QuestListItem()
}
