package de.challenge3.questapp.logik.achievements

enum class Achievements(val title: String, val description: String, val category: String) {
    READ_1("Once upon a time..", "First Page read.", "Reading"),
    READ_10("Reading novice", "Read 10 Pages from a Book.", "Reading"),
    READ_100("Knowledge consumer", "Read 100 Pages from a book.", "Reading"),

    RUN_1("Getting started", "Run 1 Kilometer.", "Running"),
    RUN_5("Everything is running great", "Run 5 Kilometers.", "Running"),
    RUN_10("Almost there..", "Run 10 Kilometers.", "Running"),

    TRAIN_1("Sore muscles", "Train for the first time", "Training"),
    TRAIN_10("Getting stronger", "Train for the tenth time", "Training"),
    TRAIN_100("Athlete", "Train for the hundreth time", "Training"),
}