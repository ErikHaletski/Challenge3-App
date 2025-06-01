package de.challenge3.questapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import de.challenge3.questapp.databinding.FragmentHomeBinding
import de.challenge3.questapp.repository.FirebaseQuestCompletionRepository
import de.challenge3.questapp.logik.stats.StatsManager
import de.challenge3.questapp.ui.SharedStatsViewModel
import de.challenge3.questapp.ui.quest.Quest
import de.challenge3.questapp.ui.quest.QuestListItem
import de.challenge3.questapp.utils.LocationHelper
import de.challenge3.questapp.utils.UserManager
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var questAdapter: QuestAdapter
    private lateinit var locationHelper: LocationHelper
    private lateinit var userManager: UserManager

    private var showDaily = true
    private var showPermanent = true

    private lateinit var locationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root

        locationHelper = LocationHelper(requireContext())
        userManager = UserManager(requireContext())
        locationPermissionLauncher = locationHelper.createPermissionLauncher(this)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        val sharedStatsViewModel = ViewModelProvider(requireActivity())[SharedStatsViewModel::class.java]
        val recyclerView = binding.questRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        locationHelper.requestPermissionsIfNeeded(locationPermissionLauncher)

        homeViewModel.questList.observe(viewLifecycleOwner) { quests ->

            val dailyQuests = quests.filter { it.type == Quest.QuestType.DAILY }
            val permanentQuests = quests.filter { it.type == Quest.QuestType.NORMAL }
            val totalCount = dailyQuests.size + permanentQuests.size
            binding.questCounter.text = "Quests: $totalCount / 10"

            val items = mutableListOf<QuestListItem>()

            items.add(QuestListItem.Header("🎯 Daily Quests", QuestListItem.HeaderType.DAILY))
            if (showDaily) {
                items.addAll(dailyQuests.map { QuestListItem.QuestItem(it) })
            }

            items.add(QuestListItem.Header("📘 Permanente Quests", QuestListItem.HeaderType.PERMANENT))
            if (showPermanent) {
                items.addAll(permanentQuests.map { QuestListItem.QuestItem(it) })
            }

            questAdapter = QuestAdapter(
                items,
                onCheckboxChanged = { quest, isChecked ->
                    quest.isCompleted = isChecked
                    questAdapter.notifyItemChanged(items.indexOfFirst {
                        it is QuestListItem.QuestItem && it.quest.id == quest.id
                    })
                    if (isChecked) {
                        completeQuest(quest)
                    }
                    sharedStatsViewModel.addExperience(quest.statType, quest.statReward)
                },
                onHeaderClicked = { type ->
                    when (type) {
                        QuestListItem.HeaderType.DAILY -> showDaily = !showDaily
                        QuestListItem.HeaderType.PERMANENT -> showPermanent = !showPermanent
                    }
                    homeViewModel.triggerUpdate()
                }
            )

            recyclerView.adapter = questAdapter
        }

        return root
    }

    private fun completeQuest(quest: Quest) {
        val currentUserId = userManager.getCurrentUserId()
        val username = userManager.getStoredUsername() ?: "Unknown User"

        locationHelper.getLocationAsync { lat, lng ->
            val questTag = mapQuestTypeToTag(quest.statType)

            val questCompletion = QuestCompletion(
                id = "",
                lat = lat,
                lng = lng,
                timestamp = System.currentTimeMillis(),
                questText = quest.description,
                questTitle = quest.title,
                tag = questTag,
                experiencePoints = quest.xpReward,
                userId = currentUserId,
                username = username
            )

            val questRepository = FirebaseQuestCompletionRepository()
            lifecycleScope.launch {
                try {
                    println("HomeFragment: Saving quest completion: ${quest.title}")
                    questRepository.addCompletedQuest(questCompletion)
                    println("HomeFragment: Quest completion saved successfully")

                    // FIXED: Force refresh the repository to ensure immediate updates
                    questRepository.forceRefresh()
                } catch (e: Exception) {
                    println("HomeFragment: Error saving quest completion: ${e.message}")
                }
            }
        }
    }

    private fun mapQuestTypeToTag(statType: String): QuestTag {
        return when (statType.lowercase()) {
            "strength", "might" -> QuestTag.MIGHT
            "intelligence", "wisdom", "mind" -> QuestTag.MIND
            "charisma", "compassion", "heart" -> QuestTag.HEART
            "willpower", "resilience", "spirit" -> QuestTag.SPIRIT
            else -> QuestTag.MIGHT
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
