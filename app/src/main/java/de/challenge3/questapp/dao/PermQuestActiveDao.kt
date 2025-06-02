package de.challenge3.questapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.challenge3.questapp.entities.PermQuestActiveEntity

@Dao
interface PermQuestActiveDao {
    @Query("SELECT * FROM PermQuestActiveEntity")
    fun getAll(): MutableList<PermQuestActiveEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg permQuestActiveEntity: PermQuestActiveEntity)

    @Delete
    fun dropAll(vararg permQuestActiveEntity: PermQuestActiveEntity)
}
