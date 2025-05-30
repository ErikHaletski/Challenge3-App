package de.challenge3.questapp

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun addieren_isCorrect() {
        var spaßmathe = Mathe()
        print(spaßmathe.addieren(1,2))
        assertEquals(3, spaßmathe.addieren(1, 2))
    }
}