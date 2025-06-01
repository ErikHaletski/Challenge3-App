package de.challenge3.questapp.logik.quest

import de.challenge3.questapp.ui.quest.Quest
import de.challenge3.questapp.ui.quest.Quest.QuestType

enum class DailyQuests (val quest: Quest) {
    CALM_DOWN(Quest("d1", "Komm zur Ruhe", "5 Min meditieren", 40, "Willpower", 1, QuestType.DAILY, 0)),
    HYDRATE(Quest("d2", "Bleib hydriert", "2L Wasser trinken", 30, "Health", 1, QuestType.DAILY, 0)),
    TOUCH_GRASS(Quest("d3", "Geh raus", "Geh spazieren", 40, "Endurance", 1, QuestType.DAILY, 0))
}