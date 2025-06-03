package de.challenge3.questapp.logik.stats

import org.junit.Test
import kotlin.test.assertEquals

class StatsTest {
    @Test
    fun `exp should be added correctly`() {
        // given
        val stat = Stats("stat")

        // when
        stat.addExperience(13)

        // then
        assertEquals(13, stat.experience)
    }

    @Test
    fun `exp ceiling calc should increase by 10 percent on lvlup`() {
        // given
        val stat = Stats("stat")

        // when
        stat.addExperience(100)

        // then
        assertEquals(110,stat.expCeiling)
    }
}