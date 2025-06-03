package de.challenge3.questapp.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.challenge3.questapp.QuestApp
import de.challenge3.questapp.entities.StatEntity
import de.challenge3.questapp.logik.stats.Attributes
import de.challenge3.questapp.logik.stats.Stats
import java.util.Locale


// StatManager und ViewModel zugleich
class SharedStatsViewModel: ViewModel() {
    val statsList: MutableLiveData<MutableSet<Stats>> = MutableLiveData<MutableSet<Stats>>()
    val text = MutableLiveData<String>()
    val statDao = QuestApp.DatabaseSetup.database?.statDao()

    // automatisches füllen der internen Statliste mit allen relevanten (= kann xp bekommen) stats
    init {
        statsList.value = mutableSetOf<Stats>()
        for (attribute in Attributes.entries) {
            if (attribute.attType == 3) {
                statsList.value?.add(Stats(attribute.name))
            }
        }
        resumeInstance()
    }


    // i xp zu einem stat hinzufügen
    fun addExperience(tar: String, experience: Int) {
        for (stat in statsList.value!!) {
            if (stat.name == tar) {
                stat.addExperience(experience)
                // jede änderung sofort speichern
                // lvl werden im stat selbst berechnet
                saveInstance()
                return
            }
        }
    }


    // getter
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

    // setter
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


    // durchschnittslevel anhand der Kinder berechnen. Relevant für layer 2, evtl layer 1
    fun getAvgLvlOf(tar: String): Float {
        var avg : Float = 0F
        var count: Int = 0
        for (attribute in Attributes.entries) {
            if (attribute.attSuper == tar) {
                count++
                avg = avg + getLevelOf(attribute.name)
            }
        }
        avg = avg / count
        // formatierung um nur 2 nachkommastellen anzuzeigen
        "%2f".format(Locale.ROOT, avg)
        return avg
    }

    fun getStat(tar: String) : Stats? {
        for (stat in statsList.value!!) {
            if (stat.name == tar) {
                return stat
            }
        }
        return null
    }


    // speichern aller stats in der lokalen datenbank
    fun saveInstance() {
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


    // abrufen aller stats und die entsprechenden werte laden
    fun resumeInstance() {
        for (stat in statDao!!.getAll()) {
            println(stat.toString())
            setLevelOf(stat.name, stat.lvl)
            setExperienceOf(stat.name, stat.exp)
            setCeilingOf(stat.name, stat.ceiling)
        }
    }
}