package de.challenge3.questapp.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import de.challenge3.questapp.databinding.FragmentHomeBinding
import de.challenge3.questapp.repository.FirebaseFriendRepository
import de.challenge3.questapp.repository.FirebaseQuestCompletionRepository
import de.challenge3.questapp.ui.quest.Quest
import de.challenge3.questapp.ui.quest.QuestListItem
import de.challenge3.questapp.utils.LocationHelper
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var questAdapter: QuestAdapter
    private lateinit var locationHelper: LocationHelper

    private var showDaily = true
    private var showPermanent = true

    // Permission Launcher mit LocationHelper
    private lateinit var locationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root

        // LocationHelper initialisieren
        locationHelper = LocationHelper(requireContext())

        // Permission Launcher erstellen
        locationPermissionLauncher = locationHelper.createPermissionLauncher(this)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        val recyclerView = binding.questRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Permission beim Start anfordern
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
        // Get current user info
        val friendRepository = FirebaseFriendRepository(requireContext())
        val currentUserId = friendRepository.getCurrentUserId()

        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val deviceId = android.provider.Settings.Secure.getString(
            requireContext().contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        val username = "User_${deviceId.takeLast(6)}"

        // Location mit LocationHelper holen
        locationHelper.getLocationAsync { lat, lng ->
            val questTag = mapQuestTypeToTag(quest.statType)

            val questCompletion = QuestCompletion(
                id = "",
                lat = lat,
                lng = lng,
                timestamp = System.currentTimeMillis(),
                questText = quest.description,
                tag = questTag,
                experiencePoints = quest.xpReward,
                userId = currentUserId,
                username = username
            )

            val questRepository = FirebaseQuestCompletionRepository()
            lifecycleScope.launch {
                questRepository.addCompletedQuest(questCompletion)
                println("Quest completion saved: ${quest.title} at location ($lat, $lng)")
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
