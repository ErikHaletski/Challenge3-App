package de.challenge3.questapp.logik.stats

import de.challenge3.questapp.R

enum class Attributes(val attType: Int, val button: Int, val layout: Int) {
    //attType = layer der stats (d.h. 1 = primär / oberste layer)
    MIGHT(1, R.id.buttonMight, R.id.mightSubstats),
    MIND(1, R.id.buttonMind, R.id.mindSubstats),
    HEART(1, R.id.buttonHeart, R.id.heartSubstats),
    SPIRIT(1, R.id.buttonSpirit, R.id.spiritSubstats),

    //TODO: layouts für restliche stats (mit layout = 0) hinzufügen
    STRENGTH(2, R.id.buttonStrength, R.id.strengthFoundation),
    ENDURANCE(2, R.id.buttonEndurance, R.id.enduranceFoundation),
    INTELLIGENCE(2, R.id.buttonIntelligence, 0),
    WISDOM(2, R.id.buttonWisdom, 0),
    COMPASSION(2, R.id.buttonCompassion, 0),
    CHARISMA(2, R.id.buttonCharisma, 0),
    WILLPOWER(2, R.id.buttonWillpower, 0),
    RESILIENCE(2, R.id.buttonResilience, 0),
}
