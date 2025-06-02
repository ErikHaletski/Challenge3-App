package de.challenge3.questapp.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.challenge3.questapp.QuestApp
import de.challenge3.questapp.entities.StatEntity
import de.challenge3.questapp.logik.stats.Attributes
import de.challenge3.questapp.logik.stats.Stats

class SharedStatsViewModel: ViewModel() {
    val statsList: MutableLiveData<MutableSet<Stats>> = MutableLiveData<MutableSet<Stats>>()
    val text = MutableLiveData<String>()
    val statDao = QuestApp.DatabaseSetup.database?.statDao()

    init {
        statsList.value = mutableSetOf<Stats>()
        for (attribute in Attributes.entries) {
            if (attribute.attType == 3) {
                statsList.value?.add(Stats(attribute.name))
            }
        }
        resumeInstance()
    }
    fun addExperience(tar: String, experience: Int) {
        println("ADD EXPERIENCE")
        for (stat in statsList.value!!) {
            if (stat.name == tar) {
                stat.addExperience(experience)
                saveInstance()
                return
            }
        }
    }

    fun getExperienceOf(tar: String) : Int {
        for (stat in statsList.value!!) {
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

    fun getCeilingOf(tar: String) : Int {
        for (stat in statsList.value!!) {
            if (stat.name == tar) {
                return stat.expCeiling
            }
        }
        return 0
    }

    fun setLevelOf(tar: String, lvl: Int) {
        for (stat in statsList.value!!) {
            if (stat.name == tar) {
                stat.level = lvl
            }
        }
    }
    fun setExperienceOf(tar: String, exp: Int) {
        for (stat in statsList.value!!) {
            if (stat.name == tar) {
                stat.experience = exp
            }
        }
    }
    fun setCeilingOf(tar: String, ceiling: Int) {
        for (stat in statsList.value!!) {
            if (stat.name == tar) {
                stat.expCeiling = ceiling
            }
        }
    }

    fun getStat(tar: String) : Stats? {
        for (stat in statsList.value!!) {
            if (stat.name == tar) {
                return stat
            }
        }
        return null
    }

    fun saveInstance() {
        println("SAVE INSTANCE")
        for (stat in statsList.value!!) {
            println(stat.toString())
            statDao!!.insertAll(
                StatEntity(
                    stat.name,
                    stat.experience,
                    stat.level,
                    stat.expCeiling
                )
            )
        }
    }

    fun resumeInstance() {
        println("RESUM INSTANCE")
        for (stat in statDao!!.getAll()) {
            println(stat.toString())
            setLevelOf(stat.name, stat.lvl)
            setExperienceOf(stat.name, stat.exp)
            setCeilingOf(stat.name, stat.ceiling)
        }
    }
}