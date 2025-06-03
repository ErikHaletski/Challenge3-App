package de.challenge3.questapp.logik.quest

import de.challenge3.questapp.logik.stats.Attributes
import de.challenge3.questapp.ui.quest.Quest
import de.challenge3.questapp.ui.quest.Quest.QuestType


// reqLvl = benötigte # der achievements
enum class PermQuests(val quest: Quest, val reqLvl: Int) {
    // 0 Achievements erledigt
    S_PUSHUPS_00(Quest("s1", "10 Pushups", "Mach 10 Liegestütze", 20, Attributes.ARMSTRENGTH.name, 30, QuestType.NORMAL, 0), 0),
    M_PUSHUPS_00(Quest("s2", "12 Pushups", "Mach 12 Liegestütze", 25, Attributes.ARMSTRENGTH.name, 34, QuestType.NORMAL, 0), 0),

    S_SITUP_00(Quest("s3","10 Sit ups", "Erledige 10 Sit ups am Stück!",20, Attributes.CORESTRENGTH.name,20, QuestType.NORMAL,0),0),
    M_SITUP_00(Quest("s4","12 Sit ups", "Erledige 12 Sit ups am Stück!",25, Attributes.CORESTRENGTH.name,24, QuestType.NORMAL,0),0),

    S_BENCHDIP_00(Quest("s5", "15 Dips", "Mach 15 Dips in einer Sitzung!",10, Attributes.CHESTSTRENGTH.name,15, QuestType.NORMAL,0),0),
    M_BENCHDIP_00(Quest("s6", "18 Dips", "Mach 18 Dips in einer Sitzung!",15, Attributes.CHESTSTRENGTH.name,18, QuestType.NORMAL,0),0),

    S_BENTOVERROW_00(Quest("s7", "8 Bent Over Rows", "Mit Kurzhanteln 8x Vorgebeugt Rudern!",20, Attributes.BACKSTRENGTH.name,20,QuestType.NORMAL,0),0),
    M_BENTOVERROW_00(Quest("s8", "10 Bent Over Rows", "Mit Kurzhanteln 10x Vorgebeugt Rudern!",25, Attributes.BACKSTRENGTH.name,24,QuestType.NORMAL,0),0),

    S_SQUAT_00(Quest("s9","15 Squats", "Mach 15 Squats!",10,Attributes.LEGSTRENGTH.name,15,QuestType.NORMAL,0),0),
    M_SQUAT_00(Quest("s10","18 Squats", "Mach 18 Squats!",15,Attributes.LEGSTRENGTH.name,18,QuestType.NORMAL,0),0),

    S_LAUFEN_00(Quest("s11", "15 Minuten Laufen", "Laufe mit einem entspanntem Tempo für 15 Minuten!", 10, Attributes.ENDURANCE.name, 30, QuestType.NORMAL, 0), 0),
    M_LAUFEN_00(Quest("s12", "18 Minuten Laufen", "Laufe mit einem entspanntem Tempo für 18 Minuten!", 15, Attributes.ENDURANCE.name, 35, QuestType.NORMAL, 0), 0),


    // 1 Achievement erledigt
    S_PUSHUPS_01(Quest("s13", "20 Pushups", "Mach 20 Liegestütze", 20, Attributes.ARMSTRENGTH.name, 40, QuestType.NORMAL, 0), 1),
    M_PUSHUPS_01(Quest("s14", "23 Pushups", "Mach 23 Liegestütze", 25, Attributes.ARMSTRENGTH.name, 45, QuestType.NORMAL, 0), 1),

    S_SITUP_01(Quest("s15","20 Sit ups", "Erledige 20 Sit ups am Stück!",20, Attributes.CORESTRENGTH.name,30, QuestType.NORMAL,0),1),
    M_SITUP_01(Quest("s16","23 Sit ups", "Erledige 23 Sit ups am Stück!",25, Attributes.CORESTRENGTH.name,35, QuestType.NORMAL,0),1),

    S_BENCHDIP_01(Quest("s17", "20 Dips", "Mach 20 Dips in einer Sitzung!",10, Attributes.CHESTSTRENGTH.name,20, QuestType.NORMAL,0),1),
    M_BENCHDIP_01(Quest("s18", "24 Dips", "Mach 24 Dips in einer Sitzung!",15, Attributes.CHESTSTRENGTH.name,24, QuestType.NORMAL,0),1),

    S_BENTOVERROW_01(Quest("s19", "15 Bent Over Rows", "Mit Kurzhanteln 15x Vorgebeugt Rudern!",20, Attributes.BACKSTRENGTH.name,30,QuestType.NORMAL,0),1),
    M_BENTOVERROW_01(Quest("s2ß", "18 Bent Over Rows", "Mit Kurzhanteln 18x Vorgebeugt Rudern!",25, Attributes.BACKSTRENGTH.name,36,QuestType.NORMAL,0),1),

    S_SQUAT_01(Quest("s21","20 Squats", "Mach 20 Squats!",10,Attributes.LEGSTRENGTH.name,20,QuestType.NORMAL,0),1),
    M_SQUAT_01(Quest("s22","24 Squats", "Mach 24 Squats!",15,Attributes.LEGSTRENGTH.name,24,QuestType.NORMAL,0),1),

    S_LAUFEN_01(Quest("s23", "20 Minuten Laufen", "Laufe mit einem entspanntem Tempo für 20 Minuten!", 10, Attributes.ENDURANCE.name, 40, QuestType.NORMAL, 0), 1),
    M_LAUFEN_01(Quest("s24", "24 Minuten Laufen", "Laufe mit einem entspanntem Tempo für 24 Minuten!", 15, Attributes.ENDURANCE.name, 45, QuestType.NORMAL, 0), 1),


    // 2 Achievements erledigt
    S_PUSHUPS_02(Quest("s25", "30 Pushups", "Mach 30 Liegestütze", 20, Attributes.ARMSTRENGTH.name, 50, QuestType.NORMAL, 0), 2),
    M_PUSHUPS_02(Quest("s26", "34 Pushups", "Mach 34 Liegestütze", 25, Attributes.ARMSTRENGTH.name, 55, QuestType.NORMAL, 0), 2),

    S_SITUP_02(Quest("s27","30 Sit ups", "Erledige 30 Sit ups am Stück!",20, Attributes.CORESTRENGTH.name,40, QuestType.NORMAL,0),2),
    M_SITUP_02(Quest("s28","34 Sit ups", "Erledige 34 Sit ups am Stück!",25, Attributes.CORESTRENGTH.name,45, QuestType.NORMAL,0),2),

    S_BENCHDIP_02(Quest("s29", "30 Dips", "Mach 30 Dips in einer Sitzung!",10, Attributes.CHESTSTRENGTH.name,30, QuestType.NORMAL,0),2),
    M_BENCHDIP_02(Quest("s30", "35 Dips", "Mach 35 Dips in einer Sitzung!",15, Attributes.CHESTSTRENGTH.name,35, QuestType.NORMAL,0),2),

    S_BENTOVERROW_02(Quest("s31", "20 Bent Over Rows", "Mit Kurzhanteln 20x Vorgebeugt Rudern!",20, Attributes.BACKSTRENGTH.name,40,QuestType.NORMAL,0),2),
    M_BENTOVERROW_02(Quest("s32", "24 Bent Over Rows", "Mit Kurzhanteln 24x Vorgebeugt Rudern!",25, Attributes.BACKSTRENGTH.name,48,QuestType.NORMAL,0),2),

    S_SQUAT_02(Quest("s33","30 Squats", "Mach 30 Squats!",10,Attributes.LEGSTRENGTH.name,30,QuestType.NORMAL,0),2),
    M_SQUAT_02(Quest("s34","35 Squats", "Mach 35 Squats!",15,Attributes.LEGSTRENGTH.name,35,QuestType.NORMAL,0),2),

    S_LAUFEN_02(Quest("s35", "30 Minuten Laufen", "Laufe mit einem entspanntem Tempo für 30 Minuten!", 10, Attributes.ENDURANCE.name, 50, QuestType.NORMAL, 0), 2),
    M_LAUFEN_02(Quest("s36", "35 Minuten Laufen", "Laufe mit einem entspanntem Tempo für 35 Minuten!", 15, Attributes.ENDURANCE.name, 55, QuestType.NORMAL, 0), 2),


    // 3 Achievements erledigt
    S_PUSHUPS_03(Quest("s37", "40 Pushups", "Mach 40 Liegestütze", 20, Attributes.ARMSTRENGTH.name, 60, QuestType.NORMAL, 0), 3),
    M_PUSHUPS_03(Quest("s38", "45 Pushups", "Mach 45 Liegestütze", 25, Attributes.ARMSTRENGTH.name, 65, QuestType.NORMAL, 0), 3),

    S_SITUP_03(Quest("s39","40 Sit ups", "Erledige 40 Sit ups am Stück!",20, Attributes.CORESTRENGTH.name,50, QuestType.NORMAL,0),3),
    M_SITUP_03(Quest("s40","45 Sit ups", "Erledige 45 Sit ups am Stück!",25, Attributes.CORESTRENGTH.name,55, QuestType.NORMAL,0),3),

    S_BENCHDIP_03(Quest("s41", "40 Dips", "Mach 40 Dips in einer Sitzung!",10, Attributes.CHESTSTRENGTH.name,40, QuestType.NORMAL,0),3),
    M_BENCHDIP_03(Quest("s42", "45 Dips", "Mach 45 Dips in einer Sitzung!",15, Attributes.CHESTSTRENGTH.name,45, QuestType.NORMAL,0),3),

    S_BENTOVERROW_03(Quest("s43", "30 Bent Over Rows", "Mit Kurzhanteln 30x Vorgebeugt Rudern!",20, Attributes.BACKSTRENGTH.name,50,QuestType.NORMAL,0),3),
    M_BENTOVERROW_03(Quest("s44", "35 Bent Over Rows", "Mit Kurzhanteln 35x Vorgebeugt Rudern!",25, Attributes.BACKSTRENGTH.name,58,QuestType.NORMAL,0),3),

    S_SQUAT_03(Quest("s45","40 Squats", "Mach 40 Squats!",10,Attributes.LEGSTRENGTH.name,40,QuestType.NORMAL,0),3),
    M_SQUAT_03(Quest("s46","45 Squats", "Mach 45 Squats!",15,Attributes.LEGSTRENGTH.name,45,QuestType.NORMAL,0),3),

    S_LAUFEN_03(Quest("s47", "40 Minuten Laufen", "Laufe mit einem entspanntem Tempo für 40 Minuten!", 10, Attributes.ENDURANCE.name, 60, QuestType.NORMAL, 0), 3),
    M_LAUFEN_03(Quest("s48", "45 Minuten Laufen", "Laufe mit einem entspanntem Tempo für 45 Minuten!", 15, Attributes.ENDURANCE.name, 65, QuestType.NORMAL, 0), 3),
}
