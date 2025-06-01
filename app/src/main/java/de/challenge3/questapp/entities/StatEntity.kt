package de.challenge3.questapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Stat (
    @PrimaryKey val name: String,
    @ColumnInfo(name = "exp") var exp: Int,
    @ColumnInfo(name = "lvl") var lvl: Int,
    @ColumnInfo(name = "ceiling") var ceiling: Int

)

