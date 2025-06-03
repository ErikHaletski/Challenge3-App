package de.challenge3.questapp.models

import org.junit.Test
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

class FriendTest {

    @Test
    fun `displayName should return username when not empty`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 5,
            totalExperience = 450
        )

        // When & Then
        assertEquals("testuser", friend.displayName)
    }

    @Test
    fun `displayName should return email prefix when username is empty`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "",
            email = "test@example.com",
            level = 5,
            totalExperience = 450
        )

        // When & Then
        assertEquals("test", friend.displayName)
    }

    @Test
    fun `currentLevelExperience should calculate correctly for level 1`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 1,
            totalExperience = 50
        )

        // When & Then
        assertEquals(50, friend.currentLevelExperience)
    }

    @Test
    fun `currentLevelExperience should calculate correctly for higher levels`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 5,
            totalExperience = 450
        )

        // When
        val currentLevelExp = friend.currentLevelExperience

        // Then
        // Level 5 means 4 * 100 = 400 XP for previous levels
        // So current level XP = 450 - 400 = 50
        assertEquals(50, currentLevelExp)
    }

    @Test
    fun `levelProgress should return correct percentage`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 3,
            totalExperience = 250
        )

        // When
        val progress = friend.levelProgress

        // Then
        // Level 3 means 2 * 100 = 200 XP for previous levels
        // Current level XP = 250 - 200 = 50
        // Progress = 50/100 = 0.5
        assertEquals(0.5f, progress, 0.001f)
    }

    @Test
    fun `levelProgress should be clamped between 0 and 1`() {
        // Given - friend with more than 100 XP in current level (edge case)
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 2,
            totalExperience = 250 // This would be 150 XP in level 2, but should be clamped
        )

        // When
        val progress = friend.levelProgress

        // Then
        assertTrue("Progress should be between 0 and 1", progress >= 0f && progress <= 1f)
    }

    @Test
    fun `experienceUntilNextLevel should calculate correctly`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 3,
            totalExperience = 250
        )

        // When
        val expUntilNext = friend.experienceUntilNextLevel

        // Then
        // Current level XP = 50, need 100 for level, so 100 - 50 = 50
        assertEquals(50, expUntilNext)
    }

    @Test
    fun `lastSeenFormatted should return Online when isOnline is true`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 1,
            totalExperience = 0,
            isOnline = true,
            lastSeen = System.currentTimeMillis() - 1000
        )

        // When & Then
        assertEquals("Online", friend.lastSeenFormatted)
    }

    @Test
    fun `lastSeenFormatted should return Offline when lastSeen is null`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 1,
            totalExperience = 0,
            isOnline = false,
            lastSeen = null
        )

        // When & Then
        assertEquals("Offline", friend.lastSeenFormatted)
    }

    @Test
    fun `lastSeenFormatted should return Just now for recent activity`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 1,
            totalExperience = 0,
            isOnline = false,
            lastSeen = System.currentTimeMillis() - 30000 // 30 seconds ago
        )

        // When & Then
        assertEquals("Just now", friend.lastSeenFormatted)
    }

    @Test
    fun `lastSeenFormatted should return minutes for recent activity`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 1,
            totalExperience = 0,
            isOnline = false,
            lastSeen = System.currentTimeMillis() - 300000 // 5 minutes ago
        )

        // When & Then
        assertEquals("5m ago", friend.lastSeenFormatted)
    }

    @Test
    fun `lastSeenFormatted should return hours for activity within day`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 1,
            totalExperience = 0,
            isOnline = false,
            lastSeen = System.currentTimeMillis() - 7200000 // 2 hours ago
        )

        // When & Then
        assertEquals("2h ago", friend.lastSeenFormatted)
    }

    @Test
    fun `lastSeenFormatted should return days for activity within week`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 1,
            totalExperience = 0,
            isOnline = false,
            lastSeen = System.currentTimeMillis() - 172800000 // 2 days ago
        )

        // When & Then
        assertEquals("2d ago", friend.lastSeenFormatted)
    }

    @Test
    fun `lastSeenFormatted should return formatted date for old activity`() {
        // Given
        val oneWeekAgo = System.currentTimeMillis() - 604800000 - 86400000 // More than a week ago
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 1,
            totalExperience = 0,
            isOnline = false,
            lastSeen = oneWeekAgo
        )

        // When
        val formatted = friend.lastSeenFormatted

        // Then
        val expectedFormat = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(oneWeekAgo))
        assertEquals(expectedFormat, formatted)
    }

    @Test
    fun `friend with zero experience should be level 1`() {
        // Given
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 1,
            totalExperience = 0
        )

        // When & Then
        assertEquals(1, friend.level)
        assertEquals(0, friend.currentLevelExperience)
        assertEquals(0f, friend.levelProgress, 0.001f)
        assertEquals(100, friend.experienceUntilNextLevel)
    }

    @Test
    fun `friend at exact level boundary should have correct values`() {
        // Given - exactly at level 3 (200 total XP)
        val friend = Friend(
            id = "1",
            username = "testuser",
            email = "test@example.com",
            level = 3,
            totalExperience = 200
        )

        // When & Then
        assertEquals(0, friend.currentLevelExperience) // 200 - (2 * 100) = 0
        assertEquals(0f, friend.levelProgress, 0.001f)
        assertEquals(100, friend.experienceUntilNextLevel)
    }
}
