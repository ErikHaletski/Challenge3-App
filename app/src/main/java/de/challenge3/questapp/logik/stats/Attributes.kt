package de.challenge3.questapp.logik.stats

import de.challenge3.questapp.R

enum class Attributes(val attType: Int, val button: Int, val layout: Int, val string: Int) {
    //attType = layer der stats (d.h. 1 = primär / oberste layer)
    MIGHT(1, R.id.buttonMight, R.id.mightSubstats, 0),
    MIND(1, R.id.buttonMind, R.id.mindSubstats, 0),
    HEART(1, R.id.buttonHeart, R.id.heartSubstats, 0),
    SPIRIT(1, R.id.buttonSpirit, R.id.spiritSubstats, 0),

    //TODO: layouts für restliche stats (mit layout = 0) hinzufügen
    STRENGTH(2, R.id.buttonStrength, R.id.strengthFoundation, 0),
    ENDURANCE(2, R.id.buttonEndurance, R.id.enduranceFoundation , 0),
    INTELLIGENCE(2, R.id.buttonIntelligence, 0, 0),
    WISDOM(2, R.id.buttonWisdom, 0, 0),
    COMPASSION(2, R.id.buttonCompassion, 0, 0),
    CHARISMA(2, R.id.buttonCharisma, 0, 0),
    WILLPOWER(2, R.id.buttonWillpower, 0, 0),
    RESILIENCE(2, R.id.buttonResilience, 0, 0),

    LEGSTRENGTH(3,R.id.buttonLegStrength, 0, R.string.sp_leg_strength),
    ARMSTRENGTH(3,R.id.buttonArmStrength, 0, R.string.sp_arm_strength),
}
