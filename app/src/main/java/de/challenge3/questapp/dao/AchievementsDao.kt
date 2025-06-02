package de.challenge3.questapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.challenge3.questapp.entities.AchievementsEntity

@Dao
interface AchievementsDao {
    @Query("SELECT * FROM AchievementsEntity")
    fun getAll(): List<AchievementsEntity>

    @Query("SELECT count(*) FROM AchievementsEntity WHERE attribute = :attribute COLLATE NOCASE")
    fun getCountOf(attribute: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg: AchievementsEntity)
}
