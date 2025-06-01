package de.challenge3.questapp.ui.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.challenge3.questapp.logik.stats.Attributes
import de.challenge3.questapp.logik.stats.Stats
import de.challenge3.questapp.logik.stats.StatsManager

class StatspageViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is statspage Fragment"
    }
    val text: LiveData<String> = _text
}
