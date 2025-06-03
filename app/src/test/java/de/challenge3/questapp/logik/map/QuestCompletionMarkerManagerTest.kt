package de.challenge3.questapp.logik.map

import android.graphics.PointF
import de.challenge3.questapp.ui.home.QuestCompletion
import de.challenge3.questapp.ui.home.QuestTag
import org.junit.Test
import org.junit.Assert.*

class QuestCompletionMarkerManagerTest {

    @Test
    fun `quest completion should have correct properties`() {
        // Given
        val quest = createTestQuestCompletion("1", 52.5, 13.4, QuestTag.MIGHT)

        // When & Then
        assertEquals("1", quest.id)
        assertEquals(52.5, quest.lat, 0.001)
        assertEquals(13.4, quest.lng, 0.001)
        assertEquals(QuestTag.MIGHT, quest.tag)
        assertEquals("Test quest 1", quest.questText)
        assertEquals("Test Title 1", quest.questTitle)
        assertEquals(50, quest.experiencePoints)
        assertEquals("user123", quest.userId)
        assertEquals("TestUser", quest.username)
    }

    @Test
    fun `quest completion location property should work correctly`() {
        // Given
        val quest = createTestQuestCompletion("1", 52.5, 13.4, QuestTag.MIGHT)

        // When
        val location = quest.location

        // Then
        assertEquals(52.5, location.latitude, 0.001)
        assertEquals(13.4, location.longitude, 0.001)
    }

    @Test
    fun `different quest tags should be handled`() {
        // Given & When
        val mightQuest = createTestQuestCompletion("1", 52.5, 13.4, QuestTag.MIGHT)
        val mindQuest = createTestQuestCompletion("2", 52.5, 13.4, QuestTag.MIND)
        val heartQuest = createTestQuestCompletion("3", 52.5, 13.4, QuestTag.HEART)
        val spiritQuest = createTestQuestCompletion("4", 52.5, 13.4, QuestTag.SPIRIT)

        // Then
        assertEquals(QuestTag.MIGHT, mightQuest.tag)
        assertEquals(QuestTag.MIND, mindQuest.tag)
        assertEquals(QuestTag.HEART, heartQuest.tag)
        assertEquals(QuestTag.SPIRIT, spiritQuest.tag)
    }

    @Test
    fun `quest completion list operations should work`() {
        // Given
        val quests = listOf(
            createTestQuestCompletion("1", 52.5, 13.4, QuestTag.MIGHT),
            createTestQuestCompletion("2", 52.6, 13.5, QuestTag.MIND),
            createTestQuestCompletion("3", 52.7, 13.6, QuestTag.HEART)
        )

        // When & Then
        assertEquals(3, quests.size)
        assertEquals("1", quests[0].id)
        assertEquals("2", quests[1].id)
        assertEquals("3", quests[2].id)

        // Test filtering by tag
        val mightQuests = quests.filter { it.tag == QuestTag.MIGHT }
        assertEquals(1, mightQuests.size)
        assertEquals("1", mightQuests[0].id)
    }

    @Test
    fun `quest completion timestamp should be valid`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val quest = createTestQuestCompletion("1", 52.5, 13.4, QuestTag.MIGHT)

        // When & Then
        assertTrue("Timestamp should be recent", quest.timestamp <= currentTime + 1000)
        assertTrue("Timestamp should be valid", quest.timestamp > 0)
    }

    private fun createTestQuestCompletion(
        id: String,
        lat: Double,
        lng: Double,
        tag: QuestTag
    ): QuestCompletion {
        return QuestCompletion(
            id = id,
            lat = lat,
            lng = lng,
            timestamp = System.currentTimeMillis(),
            questText = "Test quest $id",
            questTitle = "Test Title $id",
            tag = tag,
            experiencePoints = 50,
            userId = "user123",
            username = "TestUser"
        )
    }
}
