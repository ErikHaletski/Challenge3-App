package de.challenge3.questapp.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PermQuestPoolEntity(
    @PrimaryKey val id: String
)
