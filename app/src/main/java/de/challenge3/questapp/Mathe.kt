package de.challenge3.questapp

import kotlin.math.round

class Mathe {

    var expCeiling : Int = 100

    fun addieren (var1: Int, var2: Int):Int {
        return (var1 + var2)
    }
    fun raiseExpCeiling() {
        expCeiling = expCeiling + round(expCeiling * 0.1).toInt()
    }
}