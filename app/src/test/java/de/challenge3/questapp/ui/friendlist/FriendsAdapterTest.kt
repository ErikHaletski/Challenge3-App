package de.challenge3.questapp.ui.friendlist

import de.challenge3.questapp.models.Friend
import org.junit.Test
import org.junit.Assert.*

class FriendsAdapterTest {

    @Test
    fun `friend with empty username should display email prefix`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "",
            email = "testuser@example.com",
            level = 1,
            totalExperience = 0,
            isOnline = false,
            completedQuestsCount = 0
        )

        // When & Then
        assertEquals("testuser", friend.displayName)
    }

    @Test
    fun `friend with username should display username`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "john_doe",
            email = "john@example.com",
            level = 1,
            totalExperience = 0,
            isOnline = false,
            completedQuestsCount = 0
        )

        // When & Then
        assertEquals("john_doe", friend.displayName)
    }

    @Test
    fun `friend level progress calculation should be correct`() {
        // Test various level progress scenarios
        val testCases = listOf(
            Triple(1, 0, 0f),      // Level 1, 0 XP -> 0% progress
            Triple(1, 50, 0.5f),   // Level 1, 50 XP -> 50% progress
            Triple(1, 100, 1f),    // Level 1, 100 XP -> 100% progress (should be level 2)
            Triple(2, 150, 0.5f),  // Level 2, 150 total XP (50 in level 2) -> 50% progress
            Triple(3, 250, 0.5f)   // Level 3, 250 total XP (50 in level 3) -> 50% progress
        )

        testCases.forEach { (level, totalExp, expectedProgress) ->
            val friend = Friend(
                id = "test",
                username = "test",
                email = "test@example.com",
                level = level,
                totalExperience = totalExp,
                isOnline = false,
                completedQuestsCount = 0
            )

            assertEquals(
                "Level $level with $totalExp XP should have ${expectedProgress * 100}% progress",
                expectedProgress,
                friend.levelProgress,
                0.001f
            )
        }
    }

    @Test
    fun `friend list operations should work correctly`() {
        // Given
        val friends = listOf(
            createTestFriend("1", "alice", level = 5, totalExperience = 450),
            createTestFriend("2", "bob", level = 3, totalExperience = 250),
            createTestFriend("3", "charlie", level = 1, totalExperience = 50)
        )

        // When & Then
        assertEquals(3, friends.size)

        // Test sorting by level
        val sortedByLevel = friends.sortedByDescending { it.level }
        assertEquals("alice", sortedByLevel[0].username)
        assertEquals("bob", sortedByLevel[1].username)
        assertEquals("charlie", sortedByLevel[2].username)

        // Test filtering online friends
        val onlineFriends = friends.filter { it.isOnline }
        assertEquals(0, onlineFriends.size) // All are offline by default
    }

    @Test
    fun `friend experience calculations should be accurate`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 3,
            totalExperience = 250,
            isOnline = false,
            completedQuestsCount = 10
        )

        // When & Then
        assertEquals(50, friend.currentLevelExperience) // 250 - (2 * 100) = 50
        assertEquals(0.5f, friend.levelProgress, 0.001f) // 50/100 = 0.5
        assertEquals(50, friend.experienceUntilNextLevel) // 100 - 50 = 50
    }

    @Test
    fun `friend online status should be handled correctly`() {
        // Given
        val onlineFriend = createTestFriend("1", "online_user", isOnline = true)
        val offlineFriend = createTestFriend("2", "offline_user", isOnline = false)

        // When & Then
        assertTrue("Online friend should be online", onlineFriend.isOnline)
        assertFalse("Offline friend should be offline", offlineFriend.isOnline)

        // Online friend should have null lastSeen
        assertNull("Online friend should have null lastSeen", onlineFriend.lastSeen)

        // Offline friend should have a lastSeen timestamp
        assertNotNull("Offline friend should have lastSeen", offlineFriend.lastSeen)
    }

    @Test
    fun `friend quest count should be tracked correctly`() {
        // Given
        val friend = createTestFriend("1", "quest_master", completedQuestsCount = 42)

        // When & Then
        assertEquals(42, friend.completedQuestsCount)
    }

    private fun createTestFriend(
        id: String,
        username: String,
        isOnline: Boolean = false,
        level: Int = 1,
        totalExperience: Int = 0,
        completedQuestsCount: Int = 0
    ): Friend {
        return Friend(
            id = id,
            username = username,
            email = "$username@example.com",
            level = level,
            totalExperience = totalExperience,
            isOnline = isOnline,
            lastSeen = if (isOnline) null else System.currentTimeMillis() - 3600000,
            completedQuestsCount = completedQuestsCount
        )
    }
}
