package de.challenge3.questapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import de.challenge3.questapp.dao.AchievementsDao
import de.challenge3.questapp.dao.DailyQuestActiveDao
import de.challenge3.questapp.dao.DailyQuestPoolDao
import de.challenge3.questapp.dao.PermQuestActiveDao
import de.challenge3.questapp.dao.PermQuestPoolDao
import de.challenge3.questapp.dao.StatDao
import de.challenge3.questapp.entities.AchievementsEntity
import de.challenge3.questapp.entities.DailyQuestActiveEntity
import de.challenge3.questapp.entities.DailyQuestPoolEntity
import de.challenge3.questapp.entities.PermQuestActiveEntity
import de.challenge3.questapp.entities.PermQuestPoolEntity
import de.challenge3.questapp.entities.StatEntity

@Database(entities = [
    StatEntity::class,
    DailyQuestPoolEntity::class,
    DailyQuestActiveEntity::class,
    PermQuestPoolEntity::class,
    PermQuestActiveEntity::class,
    AchievementsEntity::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun statDao(): StatDao
    abstract fun dailyQuestActiveDao(): DailyQuestActiveDao
    abstract fun dailyQuestPoolDao(): DailyQuestPoolDao
    abstract fun permQuestActiveDao(): PermQuestActiveDao
    abstract fun permQuestPoolDao(): PermQuestPoolDao
    abstract fun achievementsDao(): AchievementsDao
}
