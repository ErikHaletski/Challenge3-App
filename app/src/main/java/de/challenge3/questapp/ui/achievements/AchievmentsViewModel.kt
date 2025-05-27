package de.challenge3.questapp.ui.achievements

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.challenge3.questapp.databinding.FragmentAchievmentsBinding

class AchievmentsFragment : Fragment() {

    private var _binding: FragmentAchievmentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AchievmentsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievmentsBinding.inflate(inflater, container, false)
        val root = binding.root

        viewModel = ViewModelProvider(this)[AchievmentsViewModel::class.java]

        val layout = binding.rootLayout
        layout.removeAllViews()

        viewModel.achievements.observe(viewLifecycleOwner) { achievements ->
            layout.removeAllViews()

            if (achievements.isEmpty()) {
                val emptyView = TextView(requireContext()).apply {
                    text = "Keine Achievements verfügbar"
                    textSize = 18f
                    setPadding(16)
                }
                layout.addView(emptyView)
                return@observe
            }

            val groupedByMain = achievements.groupBy { it.mainCategory }

            groupedByMain.forEach { (mainCategory, itemsInMain) ->
                val color = when (mainCategory) {
                    "Stärke" -> Color.RED
                    "Ausdauer" -> Color.parseColor("#4CAF50")
                    "Allgemein" -> Color.DKGRAY
                    "Completion" -> Color.BLUE
                    else -> Color.BLACK
                }

                val box = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setBackgroundColor(Color.parseColor("#F0F0F0"))
                    setPadding(24)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 24, 0, 0)
                    }
                }

                val headerLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                val titleView = TextView(requireContext()).apply {
                    text = mainCategory
                    setTypeface(null, Typeface.BOLD)
                    textSize = 20f
                    setTextColor(color)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val toggleIcon = ImageView(requireContext()).apply {
                    // Startzustand: eingeklappt → Pfeil nach oben
                    setImageResource(android.R.drawable.arrow_up_float)
                }

                headerLayout.addView(titleView)
                headerLayout.addView(toggleIcon)

                val contentLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    visibility = View.GONE  // 🟢 Standardmäßig eingeklappt!
                }

                val groupedBySub = itemsInMain.groupBy { it.category }

                groupedBySub.forEach { (subCategory, subItems) ->
                    val unlocked = subItems.count { it.unlocked }
                    val total = subItems.size
                    val percent = if (total > 0) (unlocked * 100) / total else 0

                    val subHeader = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(0, 16, 0, 8)
                    }

                    val categoryLabel = TextView(requireContext()).apply {
                        text = "$subCategory ($percent%)"
                        textSize = 17f
                        setTypeface(null, Typeface.BOLD_ITALIC)
                    }

                    val progress = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal).apply {
                        max = 100
                        progress = percent
                        isIndeterminate = false
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 4, 0, 4)
                        }
                    }

                    subHeader.addView(categoryLabel)
                    subHeader.addView(progress)
                    contentLayout.addView(subHeader)

                    subItems.forEach { achievement ->
                        val itemView = TextView(requireContext()).apply {
                            text = "• " + achievement.title
                            textSize = 16f
                            alpha = if (achievement.unlocked) 1f else 0.4f
                            setPadding(16, 4, 0, 4)
                        }
                        contentLayout.addView(itemView)
                    }
                }

                // Toggle-Funktion: Ein-/Ausklappen
                headerLayout.setOnClickListener {
                    val isVisible = contentLayout.visibility == View.VISIBLE
                    contentLayout.visibility = if (isVisible) View.GONE else View.VISIBLE
                    toggleIcon.setImageResource(
                        if (isVisible) android.R.drawable.arrow_up_float
                        else android.R.drawable.arrow_down_float
                    )
                }

                box.addView(headerLayout)
                box.addView(contentLayout)
                layout.addView(box)
            }
        }

        return root
    }
<<<<<<< HEAD
    val text: LiveData<String> = _text
=======

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
>>>>>>> feature/achievements
}
