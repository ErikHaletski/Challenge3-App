package de.challenge3.questapp.logik.stats

import kotlin.math.round

interface Stats {


    val name: String
    var experience: Int
    var level: Int
    var expCeiling: Int

    public fun addExperience(exp: Int) {
        experience = experience + exp
        reachedLvlUp()
    }

    private fun reachedLvlUp() {
        if (experience > expCeiling) {
            level = level + 1
            experience = experience - expCeiling
            raiseExpCeiling()
        }
    }

    private fun raiseExpCeiling() {
        expCeiling = expCeiling + round(expCeiling * 0.1).toInt()
    }
}
