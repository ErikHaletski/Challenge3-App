package de.challenge3.questapp.logik.stats

import kotlin.math.round

class Stats {


    val name: String
    var experience: Int = 0
    var level: Int = 1
    var expCeiling: Int = 100

    constructor(name: String) {
        this.name = name
    }

    fun addExperience(exp: Int) {
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
