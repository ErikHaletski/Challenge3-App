package de.challenge3.questapp

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.challenge3.questapp.database.AppDatabase
import de.challenge3.questapp.entities.StatEntity
import de.challenge3.questapp.entities.PermQuestPoolEntity
import de.challenge3.questapp.entities.PermQuestActiveEntity
import de.challenge3.questapp.entities.AchievementsEntity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Corrected integration test that works with the current DAO implementation
 */
@RunWith(AndroidJUnit4::class)
class DatabaseIntegrationTest {

    private lateinit var database: AppDatabase

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testQuestManagementWithCurrentDAO() {
        val permQuestPoolDao = database.permQuestPoolDao()
        val permQuestActiveDao = database.permQuestActiveDao()
        val achievementsDao = database.achievementsDao()

        // Setup: Add quests to pool with different requirements
        val poolQuests = listOf(
            PermQuestPoolEntity("s1", "ARMSTRENGTH", 0),
            PermQuestPoolEntity("s2", "ARMSTRENGTH", 1),
            PermQuestPoolEntity("s3", "ENDURANCE", 0),
            PermQuestPoolEntity("s4", "ENDURANCE", 2)
        )

        permQuestPoolDao.insertAll(*poolQuests.toTypedArray())

        // Test: Get allowed quests for ARMSTRENGTH level 0 (exact match)
        val allowedLevel0 = permQuestPoolDao.getAllAllowed("ARMSTRENGTH", 0)
        assertEquals("Should have 1 quest at exact level 0", 1, allowedLevel0.size)
        assertEquals("s1", allowedLevel0[0].id)

        // Test: Add achievement
        achievementsDao.insertAll(AchievementsEntity("achievement1", "ARMSTRENGTH"))
        val achievementCount = achievementsDao.getCountOf("ARMSTRENGTH")
        assertEquals(1, achievementCount)

        // Test: Get allowed quests for ARMSTRENGTH level 1 (exact match)
        val allowedLevel1 = permQuestPoolDao.getAllAllowed("ARMSTRENGTH", 1)
        assertEquals("Should have 1 quest at exact level 1", 1, allowedLevel1.size)
        assertEquals("s2", allowedLevel1[0].id)

        // Test: Verify we can still get level 0 quests
        val stillAllowedLevel0 = permQuestPoolDao.getAllAllowed("ARMSTRENGTH", 0)
        assertEquals("Should still have 1 quest at level 0", 1, stillAllowedLevel0.size)

        // Test: Move quest from pool to active
        permQuestPoolDao.dropAll(poolQuests[0])
        permQuestActiveDao.insertAll(PermQuestActiveEntity("s1"))

        val remainingPoolQuests = permQuestPoolDao.getAll()
        val activeQuests = permQuestActiveDao.getAll()

        assertEquals(3, remainingPoolQuests.size)
        assertEquals(1, activeQuests.size)
        assertEquals("s1", activeQuests[0].id)
    }
}