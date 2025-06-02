package de.challenge3.questapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DailyQuestPoolEntity(
    @PrimaryKey val id: String
)
