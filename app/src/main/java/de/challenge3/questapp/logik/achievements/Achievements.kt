package de.challenge3.questapp.logik.achievements

enum class Achievements(val title: String, val description: String, val category: String) {
    READ_1("Once upon a time..", "First Page read.", "Reading"),
    READ_10("Reading novice", "Read 10 Pages from a Book.", "Reading"),
    READ_100("Knowledge consumer", "Read 100 Pages from a book.", "Reading"),

    RUN_1("Getting started", "Run 1 Kilometer.", "Endurance"),
    RUN_5("Everything is running great", "Run 5 Kilometers.", "Endurance"),
    RUN_10("Almost there..", "Run 10 Kilometers.", "Endurance"),

    TRAIN_1("Sore muscles", "Train for the first time", "Armstrength"),
    TRAIN_10("Getting stronger", "Train for the tenth time", "Armstrength"),
    TRAIN_100("Athlete", "Train for the hundreth time", "Armstrength"),

    BEINE_1("Sore muscles", "Train for the first time", "Legstrength"),
    BEINE_10("Getting stronger", "Train for the tenth time", "Legstrength"),
    BEINE_100("Athlete", "Train for the hundreth time", "Legstrength"),

    BRUST_1("Sore muscles", "Train for the first time", "Cheststrength"),
    BRUST_10("Getting stronger", "Train for the tenth time", "Cheststrength"),
    BRUST_100("Athlete", "Train for the hundreth time", "Cheststrength"),
}