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
        print("Adding Experience...")
        for (stat in statsList) {
            println(stat.name)
            if (stat.name == tar) {
                println("to " + stat.name)
                println("Experience: " + stat.experience)
                stat.addExperience(experience)
                println("Experience: " + stat.experience)
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