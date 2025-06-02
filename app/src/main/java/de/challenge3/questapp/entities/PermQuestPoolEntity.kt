package de.challenge3.questapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PermQuestPoolEntity(
    @PrimaryKey val id: String,
    @ColumnInfo val stat: String,
    @ColumnInfo val reqLvl: Int
)
