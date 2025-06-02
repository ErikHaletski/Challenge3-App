package de.challenge3.questapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AchievementsEntity(
    @PrimaryKey val id: String,
    @ColumnInfo val attribute: String
)
