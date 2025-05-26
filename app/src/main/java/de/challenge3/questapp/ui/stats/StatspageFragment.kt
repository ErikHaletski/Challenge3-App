package de.challenge3.questapp.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import de.challenge3.questapp.R
import androidx.core.view.isGone

class StatspageFragment : Fragment() {

    private lateinit var statsGrid: GridLayout
    private lateinit var detailView: LinearLayout
    private lateinit var textMainStat: TextView

    private lateinit var mightSubstats: LinearLayout
    private lateinit var mindSubstats: LinearLayout
    private lateinit var heartSubstats: LinearLayout
    private lateinit var spiritSubstats: LinearLayout
    private lateinit var strengthFoundation: LinearLayout
    private lateinit var enduranceFoundation: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_statspage, container, false)

        statsGrid = view.findViewById(R.id.statsGrid)
        detailView = view.findViewById(R.id.detailView)
        textMainStat = view.findViewById(R.id.textMainStat)

        mightSubstats = view.findViewById(R.id.mightSubstats)
        mindSubstats = view.findViewById(R.id.mindSubstats)
        heartSubstats = view.findViewById(R.id.heartSubstats)
        spiritSubstats = view.findViewById(R.id.spiritSubstats)
        strengthFoundation = view.findViewById(R.id.strengthFoundation)
        enduranceFoundation = view.findViewById(R.id.enduranceFoundation)

        // Main stat buttons
        view.findViewById<Button>(R.id.buttonMight).setOnClickListener {
            showSubstats("Might")
        }
        view.findViewById<Button>(R.id.buttonMind).setOnClickListener {
            showSubstats("Mind")
        }
        view.findViewById<Button>(R.id.buttonHeart).setOnClickListener {
            showSubstats("Heart")
        }
        view.findViewById<Button>(R.id.buttonSpirit).setOnClickListener {
            showSubstats("Spirit")
        }

        // Back button
        view.findViewById<Button>(R.id.buttonBack).setOnClickListener {
            showMainStats()
        }

        // Substat buttons (optional: replace with your own logic)
        view.findViewById<Button>(R.id.buttonStrength).setOnClickListener {
            toggleFoundation("Strength")
        }
        view.findViewById<Button>(R.id.buttonEndurance).setOnClickListener {
            toggleFoundation("Endurance")
        }
        view.findViewById<Button>(R.id.buttonIntelligence).setOnClickListener {
            showSubstats("Intelligence")
        }
        view.findViewById<Button>(R.id.buttonWisdom).setOnClickListener {
            showSubstats("Wisdom")
        }
        view.findViewById<Button>(R.id.buttonCompassion).setOnClickListener {
            showSubstats("Compassion")
        }
        view.findViewById<Button>(R.id.buttonCharisma).setOnClickListener {
            showSubstats("Charisma")
        }
        view.findViewById<Button>(R.id.buttonWillpower).setOnClickListener {
            showSubstats("Willpower")
        }
        view.findViewById<Button>(R.id.buttonResilience).setOnClickListener {
            showSubstats("Resilience")
        }

        return view
    }

    private fun showSubstats(statName: String) {
        statsGrid.visibility = View.GONE
        detailView.visibility = View.VISIBLE
        textMainStat.text = statName

        // Hide all substat groups
        mightSubstats.visibility = View.GONE
        mindSubstats.visibility = View.GONE
        heartSubstats.visibility = View.GONE
        spiritSubstats.visibility = View.GONE

        // Show only the relevant one
        when (statName) {
            "Might" -> mightSubstats.visibility = View.VISIBLE
            "Mind" -> mindSubstats.visibility = View.VISIBLE
            "Heart" -> heartSubstats.visibility = View.VISIBLE
            "Spirit" -> spiritSubstats.visibility = View.VISIBLE
        }
    }

    private fun toggleFoundation(statName: String) {
        when (statName) {
            "Strength" -> if (strengthFoundation.isGone) {strengthFoundation.visibility = View.VISIBLE} else {strengthFoundation.visibility = View.GONE}
            "Endurance" -> if (enduranceFoundation.isGone) {enduranceFoundation.visibility = View.VISIBLE} else {enduranceFoundation.visibility = View.GONE}
            else -> null
        }
    }


    private fun showMainStats() {
        detailView.visibility = View.GONE
        statsGrid.visibility = View.VISIBLE
    }
}
