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


class StatspageFragment : Fragment() {

    private lateinit var statsGrid: GridLayout
    private lateinit var detailView: LinearLayout
    private lateinit var textMainStat: TextView
    private lateinit var substatsContainer: LinearLayout

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_statspage, container, false)

        statsGrid = view.findViewById(R.id.statsGrid)
        detailView = view.findViewById(R.id.detailView)
        textMainStat = view.findViewById(R.id.textMainStat)
        substatsContainer = view.findViewById(R.id.substatsContainer)

        view.findViewById<Button>(R.id.buttonBack).setOnClickListener {
            showMainStats()
        }

        view.findViewById<Button>(R.id.buttonMight).setOnClickListener {
            showSubstats("Might", listOf("Strength", "Endurance"))
        }

        view.findViewById<Button>(R.id.buttonMind).setOnClickListener {
            showSubstats("Mind", listOf("Intelligence", "Wisdom"))
        }

        view.findViewById<Button>(R.id.buttonHeart).setOnClickListener {
            showSubstats("Heart", listOf("Compassion", "Charisma"))
        }

        view.findViewById<Button>(R.id.buttonSpirit).setOnClickListener {
            showSubstats("Spirit", listOf("Willpower", "Resilience"))
        }

        return view
    }

    private fun showSubstats(statName: String, substats: List<String>) {
        statsGrid.visibility = View.GONE
        detailView.visibility = View.VISIBLE

        textMainStat.text = statName
        substatsContainer.removeAllViews()

        for (sub in substats) {
            val subText = TextView(requireContext())
            subText.text = "- $sub"
            subText.textSize = 18f
            subText.setPadding(0, 8, 0, 8)
            substatsContainer.addView(subText)
        }
    }

    private fun showMainStats() {
        detailView.visibility = View.GONE
        statsGrid.visibility = View.VISIBLE
    }
}

/*
class StatspageFragment : Fragment() {

    private var _binding: FragmentStatspageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(StatspageViewModel::class.java)

        _binding = FragmentStatspageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mightStat = binding.mightStat
        val mightSubstats = binding.mightSubstats
        val mindStat = binding.mindStat
        val mindSubstats = binding.mindSubstats

        mightStat.setOnClickListener {
            toggleVisibility(mightSubstats)
        }

        mindStat.setOnClickListener {
            toggleVisibility(mindSubstats)
        }



        /*
                val textView: TextView = binding.textStatspage
                homeViewModel.text.observe(viewLifecycleOwner) {
                    textView.text = it
                }

                binding.Mind.setOnClickListener {
                    textView.text = "test click mind"
                }
        */
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun toggleVisibility(view: View) {
        view.visibility = if (view.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }
}
*/