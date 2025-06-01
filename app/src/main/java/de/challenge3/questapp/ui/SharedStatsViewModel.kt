package de.challenge3.questapp.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.challenge3.questapp.logik.stats.Attributes
import de.challenge3.questapp.logik.stats.Stats
import de.challenge3.questapp.logik.stats.StatsManager

class SharedStatsViewModel: ViewModel() {
    val statsList: MutableLiveData<MutableSet<Stats>> = MutableLiveData<MutableSet<Stats>>()
    val text = MutableLiveData<String>()

    init {
        text.value = "1"
        statsList.value = mutableSetOf<Stats>()
        for (attribute in Attributes.entries) {
            if (attribute.attType == 3) {
                statsList.value?.add(Stats(attribute.name))
            }
        }
    }
    fun addExperience(tar: String, experience: Int) {
        text.value = "0"
        println(text.value)
        for (stat in statsList.value!!) {
            println(stat)
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
        for (stat in statsList.value!!) {
            println(stat)
            println(stat.name)
            println("text: ${text.value}")
            if (stat.name == tar) {
                return stat.experience
            }
        }
        return 0
    }

    fun getLevelOf(tar: String) : Int {
        for (stat in statsList.value!!) {
            if (stat.name == tar) {
                return stat.level
            }
        }
        return 0
    }

}