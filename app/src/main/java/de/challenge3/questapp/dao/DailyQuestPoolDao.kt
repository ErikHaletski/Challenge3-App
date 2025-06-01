package de.challenge3.questapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.challenge3.questapp.entities.DailyQuestPoolEntity

@Dao
interface DailyQuestPoolDao {
    @Query("SELECT * FROM DailyQuestPoolEntity")
    fun getAll(): MutableList<DailyQuestPoolEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg dailyQuestPoolEntity: DailyQuestPoolEntity)

    @Delete
    fun dropAll(vararg dailyQuestPoolEntity: DailyQuestPoolEntity)
}