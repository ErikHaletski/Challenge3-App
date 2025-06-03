package de.challenge3.questapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


// Entity für das perm questpool. benötigt das zugehörige stat und reqlvl da so das filtern funktioniert
@Entity
data class PermQuestPoolEntity(
    @PrimaryKey val id: String,
    @ColumnInfo val stat: String,
    @ColumnInfo val reqLvl: Int
)
