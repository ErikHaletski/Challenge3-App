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
import androidx.lifecycle.ViewModelProvider
import de.challenge3.questapp.logik.stats.Attributes
import de.challenge3.questapp.ui.SharedStatsViewModel

class StatspageFragment : Fragment() {

    private lateinit var statsGrid: GridLayout
    private lateinit var detailView: LinearLayout
    private lateinit var textMainStat: TextView

    private var layoutMap: HashMap<String, LinearLayout> = HashMap<String, LinearLayout>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statspage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedStatsViewModel = ViewModelProvider(requireActivity())[SharedStatsViewModel::class.java]

        statsGrid = view.findViewById(R.id.statsGrid)
        detailView = view.findViewById(R.id.detailView)
        textMainStat = view.findViewById(R.id.textMainStat)

        for (attribute in Attributes.entries){
            //add all layoutIDs
            layoutMap.put(attribute.name, view.findViewById(attribute.layout))
            //add proper function to button depending on attType
            when (attribute.attType) {
                1 -> view.findViewById<Button>(attribute.button).setOnClickListener {
                    showSubstats(attribute.name)
                }
                2 -> view.findViewById<Button>(attribute.button).setOnClickListener {
                    toggleFoundation(attribute.name)
                }
                3 -> view.findViewById<Button>(attribute.button).text =
                    getString(attribute.string,
                        sharedStatsViewModel.getLevelOf(attribute.name),
                        sharedStatsViewModel.getExperienceOf(attribute.name)
                    )
            }
        }

        // Back button
        view.findViewById<Button>(R.id.buttonBack).setOnClickListener {
            showMainStats()
        }

    }

    private fun showSubstats(statName: String) {
        statsGrid.visibility = View.GONE
        detailView.visibility = View.VISIBLE
        textMainStat.text = statName

        // Hide all substat groups
        for(attribute in Attributes.entries) {
            if (attribute.attType == 1) {
                layoutMap[attribute.name]?.visibility = View.GONE
            }
        }

        //Show relevant group
        layoutMap[statName]?.visibility = View.VISIBLE
    }

    private fun toggleFoundation(statName: String) {
        //toggle clicked group
        if (layoutMap[statName]?.isGone == true) {
            layoutMap[statName]?.visibility = View.VISIBLE
        } else {
            layoutMap[statName]?.visibility = View.GONE
        }
    }

    private fun showMainStats() {
        detailView.visibility = View.GONE
        statsGrid.visibility = View.VISIBLE
    }
}
