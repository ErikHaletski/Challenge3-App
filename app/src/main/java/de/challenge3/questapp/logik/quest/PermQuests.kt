package de.challenge3.questapp.logik.quest

import de.challenge3.questapp.logik.stats.Attributes
import de.challenge3.questapp.ui.quest.Quest
import de.challenge3.questapp.ui.quest.Quest.QuestType

enum class PermQuests(val quest: Quest, val reqLvl: Int) {
    S_PUSHUPS_00(Quest("s1", "10 Pushups", "Mach 10 Liegestütze", 50, Attributes.ARMSTRENGTH.name, 2, QuestType.NORMAL, 0), 0),
    M_PUSHUPS_00(Quest("s2", "12 Pushups", "Mach 12 Liegestütze", 60, Attributes.ARMSTRENGTH.name, 2, QuestType.NORMAL, 0), 0),
    S_BEINE_00(Quest("s3", "beine", "mach beine", 30, Attributes.LEGSTRENGTH.name, 2, QuestType.NORMAL, 0), 0),
    S_ENDURANCE_00(Quest("s4", "ausdauer", "mach ausdauer", 40, Attributes.ENDURANCE.name, 3, QuestType.NORMAL, 0), 0),
    S_PUSHUPS_01(Quest("s5", "20 Pushups", "Mach 20 Liegestütze", 50, Attributes.ARMSTRENGTH.name, 2, QuestType.NORMAL, 0), 1),
    M_PUSHUPS_01(Quest("s6", "22 Pushups", "Mach 22 Liegestütze", 60, Attributes.ARMSTRENGTH.name, 2, QuestType.NORMAL, 0), 1),
    S_BEINE_01(Quest("s7", "beine sehr", "mach beine", 30, Attributes.LEGSTRENGTH.name, 2, QuestType.NORMAL, 0), 1),
    S_ENDURANCE_01(Quest("s8", "ausdauer sehr", "mach ausdauer", 40, Attributes.ENDURANCE.name, 3, QuestType.NORMAL, 0), 1),
    S_PUSHUPS_02(Quest("s9", "30 Pushups", "Mach 30 Liegestütze", 50, Attributes.ARMSTRENGTH.name, 2, QuestType.NORMAL, 0), 2),
    M_PUSHUPS_02(Quest("s10", "32 Pushups", "Mach 32 Liegestütze", 60, Attributes.ARMSTRENGTH.name, 2, QuestType.NORMAL, 0), 2),
    S_BEINE_02(Quest("s11", "beine sehr sehr", "mach beine", 30, Attributes.LEGSTRENGTH.name, 2, QuestType.NORMAL, 0), 2),
    S_ENDURANCE_02(Quest("s12", "ausdauer sehr sehr", "mach ausdauer", 40, Attributes.ENDURANCE.name, 3, QuestType.NORMAL, 0), 2),
    L_PUSHUPS_02(Quest("s13", "34 Pushups", "Mach 34 Liegestütze", 50, Attributes.ARMSTRENGTH.name, 2, QuestType.NORMAL, 0), 2),
    XL_PUSHUPS_02(Quest("s14", "36 Pushups", "Mach 36 Liegestütze", 60, Attributes.ARMSTRENGTH.name, 2, QuestType.NORMAL, 0), 2),
    M_BEINE_02(Quest("s15", "beine sehr sehr mittel", "mach beine", 30, Attributes.LEGSTRENGTH.name, 2, QuestType.NORMAL, 0), 2),
    M_ENDURANCE_02(Quest("s16", "ausdauer sehr sehr mittel", "mach ausdauer", 40, Attributes.ENDURANCE.name, 3, QuestType.NORMAL, 0), 2),
}