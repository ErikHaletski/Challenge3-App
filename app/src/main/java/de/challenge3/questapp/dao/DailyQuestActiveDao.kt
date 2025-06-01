package de.challenge3.questapp.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.challenge3.questapp.entities.DailyQuestActiveEntity

@Dao
interface DailyQuestActiveDao {
    @Query("SELECT * FROM DailyQuestActiveEntity")
    fun getAll(): MutableList<DailyQuestActiveEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg dailyQuestActiveEntity: DailyQuestActiveEntity)

    @Delete
    fun dropAll(vararg dailyQuestActiveEntity: DailyQuestActiveEntity)
}