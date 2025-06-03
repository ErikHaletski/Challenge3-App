package de.challenge3.questapp.logik.stats

import de.challenge3.questapp.R

enum class Attributes(val attType: Int, val attSuper: String, val button: Int, val layout: Int, val string: Int) {
    //attType = layer der stats (d.h. 1 = primär / oberste layer)
    //attSuper = Elternteil des Objektes / Zugehörigkeit
    MIGHT(1, "", R.id.buttonMight, R.id.mightSubstats, R.string.sp_might),
    MIND(1, "", R.id.buttonMind, R.id.mindSubstats, R.string.sp_mind),
    HEART(1, "", R.id.buttonHeart, R.id.heartSubstats, R.string.sp_heart),
    SPIRIT(1, "", R.id.buttonSpirit, R.id.spiritSubstats, R.string.sp_spirit),

    //attType = 3 bedeutet, dass das Objekt keine Kinder hat, d.h. die unterste Layer ist der XP hinzugefügt wird
    STRENGTH(2, Attributes.MIGHT.name, R.id.buttonStrength, R.id.strengthFoundation, R.string.sp_strength),
    ENDURANCE(3, Attributes.MIGHT.name, R.id.buttonEndurance, R.id.enduranceFoundation , R.string.sp_endurance),
    INTELLIGENCE(2, Attributes.MIND.name, R.id.buttonIntelligence, 0, R.string.sp_intelligence),
    WISDOM(2, Attributes.MIND.name, R.id.buttonWisdom, 0, R.string.sp_wisdom),
    COMPASSION(2, Attributes.HEART.name, R.id.buttonCompassion, 0, R.string.sp_compassion),
    CHARISMA(2, Attributes.HEART.name, R.id.buttonCharisma, 0, R.string.sp_charisma),
    WILLPOWER(2, Attributes.SPIRIT.name, R.id.buttonWillpower, 0, R.string.sp_willpower),
    RESILIENCE(2, Attributes.SPIRIT.name, R.id.buttonResilience, 0, R.string.sp_resilience),

    LEGSTRENGTH(3, Attributes.STRENGTH.name, R.id.buttonLegStrength, 0, R.string.sp_leg_strength),
    ARMSTRENGTH(3, Attributes.STRENGTH.name, R.id.buttonArmStrength, 0, R.string.sp_arm_strength),
    CHESTSTRENGTH(3, Attributes.STRENGTH.name, R.id.buttonChestStrength, 0, R.string.sp_chest_strength),
    BACKSTRENGTH(3, Attributes.STRENGTH.name, R.id.buttonBackStrength, 0, R.string.sp_back_strength),
    CORESTRENGTH(3, Attributes.STRENGTH.name, R.id.buttonCoreStrength, 0, R.string.sp_core_strength)
}
