package de.challenge3.questapp

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.challenge3.questapp.ui.SharedStatsViewModel
import de.challenge3.questapp.ui.SharedQuestViewModel
import de.challenge3.questapp.ui.quest.Quest
import de.challenge3.questapp.utils.UserManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Rule
import androidx.lifecycle.Observer
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.runBlocking
import android.content.Context

/**
 * Integration test for the complete quest completion flow.
 * Tests the interaction between ViewModels, UserManager, and stats system.
 */
@RunWith(AndroidJUnit4::class)
class QuestCompletionIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var userManager: UserManager
    private lateinit var sharedStatsViewModel: SharedStatsViewModel
    private lateinit var sharedQuestViewModel: SharedQuestViewModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        // Initialize QuestApp database for testing
        QuestApp.database = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            de.challenge3.questapp.database.AppDatabase::class.java
        ).allowMainThreadQueries().build()

        userManager = UserManager(context)
        sharedStatsViewModel = SharedStatsViewModel()
        sharedQuestViewModel = SharedQuestViewModel()
    }

    @Test
    fun testCompleteQuestFlow() = runBlocking {
        // Setup: Complete user setup first
        val testUsername = "TestUser123"
        val setupResult = userManager.completeUserSetup(testUsername)
        assertTrue("User setup should succeed", setupResult.isSuccess)

        // Verify initial state
        val initialLevel = userManager.getPlayerLevel()
        val initialExp = userManager.getTotalExperience()
        assertEquals(1, initialLevel)
        assertEquals(0, initialExp)

        // Test: Complete the quest (simulate quest completion)
        val expGained = 50
        val levelsGained = userManager.completeQuest(expGained)

        // Verify player level progression
        val newLevel = userManager.getPlayerLevel()
        val newExp = userManager.getTotalExperience()
        assertEquals(50, newExp)
        assertEquals(1, newLevel) // Should still be level 1 with 50 XP
        assertEquals(0, levelsGained) // No level up yet

        // Test: Add stat experience
        sharedStatsViewModel.addExperience("ARMSTRENGTH", 25)

        // Verify stat progression
        val armStrengthExp = sharedStatsViewModel.getExperienceOf("ARMSTRENGTH")
        val armStrengthLevel = sharedStatsViewModel.getLevelOf("ARMSTRENGTH")
        assertEquals(25, armStrengthExp)
        assertEquals(1, armStrengthLevel)

        // Test: Complete enough quests to level up
        val additionalExpForLevelUp = 50 // Need 100 total for level 2
        val levelsGainedOnLevelUp = userManager.completeQuest(additionalExpForLevelUp)

        val finalLevel = userManager.getPlayerLevel()
        val finalExp = userManager.getTotalExperience()
        assertEquals(100, finalExp)
        assertEquals(2, finalLevel)
        assertEquals(1, levelsGainedOnLevelUp)
    }

    @Test
    fun testStatsManagerIntegration() {
        // Test: Initialize stats and verify integration with SharedStatsViewModel
        val initialArmStrengthExp = sharedStatsViewModel.getExperienceOf("ARMSTRENGTH")
        val initialEnduranceExp = sharedStatsViewModel.getExperienceOf("ENDURANCE")
        val initialLegStrengthExp = sharedStatsViewModel.getExperienceOf("LEGSTRENGTH")

        assertEquals(0, initialArmStrengthExp)
        assertEquals(0, initialEnduranceExp)
        assertEquals(0, initialLegStrengthExp)

        // Test: Add experience to different stats
        sharedStatsViewModel.addExperience("ARMSTRENGTH", 75)
        sharedStatsViewModel.addExperience("ENDURANCE", 150) // This will cause level up (150 > 100)
        sharedStatsViewModel.addExperience("LEGSTRENGTH", 50)

        // Verify experience addition (accounting for level-ups)
        assertEquals(75, sharedStatsViewModel.getExperienceOf("ARMSTRENGTH"))

        // ENDURANCE: 150 exp causes level up (150 > 100 ceiling)
        // After level up: level = 2, experience = 150 - 100 = 50, new ceiling = 110
        assertEquals("ENDURANCE should have 50 exp after leveling up", 50, sharedStatsViewModel.getExperienceOf("ENDURANCE"))

        assertEquals(50, sharedStatsViewModel.getExperienceOf("LEGSTRENGTH"))

        // Test: Verify level progression
        assertEquals(1, sharedStatsViewModel.getLevelOf("ARMSTRENGTH")) // 75 exp = level 1 (no level up)
        assertEquals(2, sharedStatsViewModel.getLevelOf("ENDURANCE"))   // 150 exp caused level up to 2
        assertEquals(1, sharedStatsViewModel.getLevelOf("LEGSTRENGTH")) // 50 exp = level 1 (no level up)

        // Test: Save and resume instance (database persistence)
        sharedStatsViewModel.saveInstance()

        // Create new instance and resume
        val newStatsViewModel = SharedStatsViewModel()
        newStatsViewModel.resumeInstance()

        // Verify data persistence
        assertEquals(75, newStatsViewModel.getExperienceOf("ARMSTRENGTH"))
        assertEquals(50, newStatsViewModel.getExperienceOf("ENDURANCE")) // Should be 50 after level up
        assertEquals(50, newStatsViewModel.getExperienceOf("LEGSTRENGTH"))
        assertEquals(1, newStatsViewModel.getLevelOf("ARMSTRENGTH"))
        assertEquals(2, newStatsViewModel.getLevelOf("ENDURANCE"))
        assertEquals(1, newStatsViewModel.getLevelOf("LEGSTRENGTH"))

        // Test: Verify ceiling calculations persist
        assertTrue("Ceiling should be greater than experience",
            newStatsViewModel.getCeilingOf("ARMSTRENGTH") > 75)
        assertTrue("Ceiling should be greater than experience",
            newStatsViewModel.getCeilingOf("ENDURANCE") > 50)
    }

    @Test
    fun testStepByStepLevelUp() {
        // Test level ups one by one to understand the exact behavior

        // Start: level 1, exp 0, ceiling 100
        assertEquals(1, sharedStatsViewModel.getLevelOf("ARMSTRENGTH"))
        assertEquals(0, sharedStatsViewModel.getExperienceOf("ARMSTRENGTH"))
        assertEquals(100, sharedStatsViewModel.getCeilingOf("ARMSTRENGTH"))

        // Add 101 to trigger first level up
        sharedStatsViewModel.addExperience("ARMSTRENGTH", 101)
        assertEquals("First level up", 2, sharedStatsViewModel.getLevelOf("ARMSTRENGTH"))
        assertEquals("Remaining exp after first level up", 1, sharedStatsViewModel.getExperienceOf("ARMSTRENGTH"))
        assertEquals("New ceiling after first level up", 110, sharedStatsViewModel.getCeilingOf("ARMSTRENGTH"))

        // Add enough for second level up: need > 110, so add 110 more (total will be 111)
        sharedStatsViewModel.addExperience("ARMSTRENGTH", 110)
        assertEquals("Second level up", 3, sharedStatsViewModel.getLevelOf("ARMSTRENGTH"))
        assertEquals("Remaining exp after second level up", 1, sharedStatsViewModel.getExperienceOf("ARMSTRENGTH"))

        val ceilingAfterSecond = sharedStatsViewModel.getCeilingOf("ARMSTRENGTH")
        // ceiling = 110 + round(110 * 0.1) = 110 + 11 = 121
        assertEquals("Ceiling after second level up", 121, ceilingAfterSecond)

        // Add enough for third level up: need > 121, so add 121 more (total will be 122)
        sharedStatsViewModel.addExperience("ARMSTRENGTH", 121)
        assertEquals("Third level up", 4, sharedStatsViewModel.getLevelOf("ARMSTRENGTH"))
        assertEquals("Remaining exp after third level up", 1, sharedStatsViewModel.getExperienceOf("ARMSTRENGTH"))

        val finalCeiling = sharedStatsViewModel.getCeilingOf("ARMSTRENGTH")
        // ceiling = 121 + round(121 * 0.1) = 121 + 12 = 133
        assertEquals("Final ceiling", 133, finalCeiling)
    }

    @Test
    fun testExactLevelUpBoundary() {
        // Test the exact boundary condition

        // Add 99 experience (should not level up)
        sharedStatsViewModel.addExperience("LEGSTRENGTH", 99)
        assertEquals("Should be level 1 with 99 exp", 1, sharedStatsViewModel.getLevelOf("LEGSTRENGTH"))
        assertEquals("Should have 99 exp", 99, sharedStatsViewModel.getExperienceOf("LEGSTRENGTH"))

        // Add exactly 100 experience total (should not level up because 100 > 100 is false)
        sharedStatsViewModel.addExperience("LEGSTRENGTH", 1)
        assertEquals("Should still be level 1 with exactly 100 exp", 1, sharedStatsViewModel.getLevelOf("LEGSTRENGTH"))
        assertEquals("Should have 100 exp", 100, sharedStatsViewModel.getExperienceOf("LEGSTRENGTH"))

        // Add 1 more to trigger level up (101 > 100 is true)
        sharedStatsViewModel.addExperience("LEGSTRENGTH", 1)
        assertEquals("Should be level 2 with 101 total exp", 2, sharedStatsViewModel.getLevelOf("LEGSTRENGTH"))
        assertEquals("Should have 1 exp after level up", 1, sharedStatsViewModel.getExperienceOf("LEGSTRENGTH"))
    }
}