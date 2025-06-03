package de.challenge3.questapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


//Entity zum speichern der Stats
@Entity
data class StatEntity (
    @PrimaryKey val name: String,
    @ColumnInfo(name = "exp") val exp: Int,
    @ColumnInfo(name = "lvl") val lvl: Int,
    @ColumnInfo(name = "ceiling") val ceiling: Int
)
