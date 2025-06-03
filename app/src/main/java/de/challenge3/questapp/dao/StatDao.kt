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


    // onconflict replace, da die stats IMMER in der tabelle bleiben. werden nie gelöscht, nur überschrieben
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg statEntities: StatEntity)


    @Update
    fun updateStats(vararg statEntities: StatEntity)
}
