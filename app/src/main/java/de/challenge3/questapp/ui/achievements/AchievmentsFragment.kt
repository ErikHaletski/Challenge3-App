package de.challenge3.questapp.ui.achievements

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.challenge3.questapp.QuestApp
import de.challenge3.questapp.databinding.FragmentAchievmentsBinding
import de.challenge3.questapp.entities.AchievementsEntity

class AchievmentsFragment : Fragment() {

    private var _binding: FragmentAchievmentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AchievmentsViewModel
    private val achievementsDao = QuestApp.database?.achievementsDao()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievmentsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[AchievmentsViewModel::class.java]
        val layout = binding.rootLayout

        // Beobachte die Achievements
        viewModel.achievements.observe(viewLifecycleOwner) { items ->
            layout.removeAllViews()

            // Titel ganz oben
            val mainTitle = TextView(requireContext()).apply {
                text = "Achievements"
                textSize = 24f
                setPadding(0, 0, 0, 24)
            }
            layout.addView(mainTitle)

            // Gruppiere nach Kategorie
            items.groupBy { it.achievement.category }.forEach { (category, achievements) ->
                val categoryLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(24, 24, 24, 24)
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 0, 32)
                    layoutParams = params
                }

                val categoryTitle = TextView(requireContext()).apply {
                    text = category
                    textSize = 20f
                    setPadding(0, 0, 0, 16)
                }
                categoryLayout.addView(categoryTitle)

                achievements.forEach { item ->
                    val checkbox = CheckBox(requireContext()).apply {
                        text = item.achievement.title
                        isChecked = item.unlocked
                        alpha = if (item.unlocked) 0.5f else 1f
                        setOnClickListener {
                            viewModel.toggleAchievement(item.achievement)
                            achievementsDao?.insertAll(AchievementsEntity(item.achievement.title, item.achievement.category))
                        }
                    }

                    val description = TextView(requireContext()).apply {
                        text = item.achievement.description
                        textSize = 14f
                        setPadding(64, 0, 0, 16)
                        alpha = if (item.unlocked) 0.5f else 1f
                    }

                    categoryLayout.addView(checkbox)
                    categoryLayout.addView(description)
                }

                layout.addView(categoryLayout)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
