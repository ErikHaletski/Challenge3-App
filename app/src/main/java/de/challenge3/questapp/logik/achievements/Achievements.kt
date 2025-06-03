package de.challenge3.questapp.logik.achievements


// anhand der category wird das menü item erstellt UND das entsprechende stat/questreihe ausgemacht. suche ist caseinsensitive
enum class Achievements(val title: String, val description: String, val category: String) {
    RUN_10("Ausdauer 10x", "Laufe für insgesamt 10 Stunden", "Endurance"),
    RUN_50("Ausdauer 50x", "Laufe für insgesamt 50 Stunden", "Endurance"),
    RUN_100("Ausdauer 100x", "Laufe für insgesamt 100 Stunden", "Endurance"),

    TRAIN_10("Arme 10x", "Trainiere deine Arme 10 mal", "Armstrength"),
    TRAIN_50("Arme 50x", "Trainiere deine Arme 50 mal", "Armstrength"),
    TRAIN_100("Arme 100x", "Trainiere deine Arme 100 mal", "Armstrength"),

    BEINE_10("Beine 10x", "Trainiere deine Beine 10 mal", "Legstrength"),
    BEINE_50("Beine 50x", "Trainiere deine Beine 50 mal", "Legstrength"),
    BEINE_100("Beine 100x", "Trainiere deine Beine 100 mal", "Legstrength"),

    BRUST_10("Brust 10x", "Trainiere deine Brust 10 mal", "Cheststrength"),
    BRUST_50("Brust 50x", "Trainiere deine Brust 50 mal", "Cheststrength"),
    BRUST_100("Brust 100x", "Trainiere deine Brust 100 mal", "Cheststrength"),

    CORE_10("Core 10x","Trainiere deinen Core 10 mal","Corestrength"),
    CORE_50("Core 50x","Trainiere deinen Core 50 mal","Corestrength"),
    CORE_100("Core 100x","Trainiere deinen Core 100 mal","Corestrength"),

    BACK_10("Rücken 10x","Trainiere deinen Rücken 10 mal","Backstrength"),
    BACK_50("Rücken 50x","Trainiere deinen Rücken 50 mal","Backstrength"),
    BACK_100("Rücken 100x","Trainiere deinen Rücken 100 mal","Backstrength"),

}
