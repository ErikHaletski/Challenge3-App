package de.challenge3.questapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.challenge3.questapp.entities.StatEntity

@Dao
interface StatDao {
    @Query("SELECT * FROM StatEntity")
    fun getAll(): List<StatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg statEntities: StatEntity)

    @Update
    fun updateStats(vararg statEntities: StatEntity)
}
