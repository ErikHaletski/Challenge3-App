package de.challenge3.questapp.logik.stats

class StatsManager {
    var statsList: MutableSet<Stats> = mutableSetOf<Stats>()
        private set

    constructor() {
        for (attribute in Attributes.entries) {
            if (attribute.attType == 3) {
                statsList.add(Stats(attribute.name))
            }
        }
    }

    fun addExperience(tar: String, experience: Int) {
        for (stat in statsList) {
            if (stat.name == tar) {
                stat.addExperience(experience)
                return
            }
        }
    }

    fun getExperienceOf(tar: String) : Int {
        for (stat in statsList) {
            if (stat.name == tar) {
                return stat.experience
            }
        }
        return 0
    }

    fun getLevelOf(tar: String) : Int {
        for (stat in statsList) {
            if (stat.name == tar) {
                return stat.level
            }
        }
        return 0
    }

}