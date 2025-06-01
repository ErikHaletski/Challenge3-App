package de.challenge3.questapp.logik.quest

import de.challenge3.questapp.logik.stats.Attributes
import de.challenge3.questapp.ui.quest.Quest
import de.challenge3.questapp.ui.quest.Quest.QuestType

enum class PermQuests(val quest: Quest, val reqLvl: Int) {
    S_PUSHUPS_01(Quest("s1", "10 Pushups", "Mach 10 Liegestütze", 50, Attributes.ARMSTRENGTH.name, 2, QuestType.NORMAL, 0), 1),
    M_PUSHUPS_01(Quest("s2", "12 Pushups", "Mach 12 Liegestütze", 60, Attributes.ARMSTRENGTH.name, 2, QuestType.NORMAL, 0), 1),
    S_BEINE_01(Quest("s3", "beine", "mach beine", 30, Attributes.LEGSTRENGTH.name, 2, QuestType.NORMAL, 0), 1),
    S_ENDURANCE_01(Quest("s4", "ausdauer", "mach ausdauer", 40, Attributes.ENDURANCE.name, 3, QuestType.NORMAL, 0), 1)
}