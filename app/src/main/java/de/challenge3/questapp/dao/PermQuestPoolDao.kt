package de.challenge3.questapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.challenge3.questapp.entities.PermQuestPoolEntity

@Dao
interface PermQuestPoolDao {
    @Query("SELECT * FROM PermQuestPoolEntity")
    fun getAll(): MutableList<PermQuestPoolEntity>


    // gefilterte suche nach allen vom user erfüllbaren quests (gefiltert nach erledigten achievements)
    @Query("SELECT * FROM PermQuestPoolEntity WHERE stat = :stat AND reqLvl = :lvl COLLATE NOCASE")
    fun getAllAllowed(stat: String, lvl: Int): MutableList<PermQuestPoolEntity>


    // replace onconflict damit wir quests ändern können
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg permQuestPoolEntity: PermQuestPoolEntity)


    @Delete
    fun dropAll(vararg permQuestPoolEntity: PermQuestPoolEntity)
}
