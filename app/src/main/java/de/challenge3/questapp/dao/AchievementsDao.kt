package de.challenge3.questapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.challenge3.questapp.entities.AchievementsEntity
import de.challenge3.questapp.logik.stats.Attributes

@Dao
interface AchievementsDao {
    @Query("SELECT count(*) FROM AchievementsEntity WHERE attribute = :attribute COLLATE NOCASE")
    fun getCountOf(attribute: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg: AchievementsEntity)
}