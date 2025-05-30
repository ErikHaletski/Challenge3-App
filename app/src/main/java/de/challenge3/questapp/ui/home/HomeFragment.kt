package de.challenge3.questapp.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import de.challenge3.questapp.databinding.FragmentHomeBinding
import de.challenge3.questapp.repository.FirebaseFriendRepository
import de.challenge3.questapp.repository.FirebaseQuestCompletionRepository
import de.challenge3.questapp.ui.quest.Quest
import de.challenge3.questapp.ui.quest.QuestListItem
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var questAdapter: QuestAdapter

    private var showDaily = true
    private var showPermanent = true

    // Permission Launcher als Klassenvariable
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            println("Location permission granted - Quest completions will now use real location")
        } else {
            println("Location permission denied - Quest completions will use fallback location")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        val recyclerView = binding.questRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Permission beim Start anfordern
        if (!hasLocationPermissions()) {
            requestLocationPermissions()
        }

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

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
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

        // Get location (should work now since we requested permission at start)
        getSimpleLocation { lat, lng ->
            val questTag = when (quest.statType.lowercase()) {
                "strength", "might" -> QuestTag.MIGHT
                "intelligence", "wisdom", "mind" -> QuestTag.MIND
                "charisma", "compassion", "heart" -> QuestTag.HEART
                "willpower", "resilience", "spirit" -> QuestTag.SPIRIT
                else -> QuestTag.MIGHT
            }

            val questCompletion = QuestCompletion(
                id = "",
                lat = lat,
                lng = lng,
                timestamp = System.currentTimeMillis(),
                questText = quest.title,
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

    private fun getSimpleLocation(callback: (Double, Double) -> Unit) {
        if (!hasLocationPermissions()) {
            println("No location permission - using fallback location")
            callback(52.5200, 13.4050) // Fallback
            return
        }

        try {
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            val lastKnown = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)

            if (lastKnown != null) {
                println("Using real location: ${lastKnown.latitude}, ${lastKnown.longitude}")
                callback(lastKnown.latitude, lastKnown.longitude)
            } else {
                println("No cached location available - using fallback")
                callback(52.5200, 13.4050) // Fallback
            }
        } catch (e: SecurityException) {
            println("SecurityException getting location - using fallback")
            callback(52.5200, 13.4050)
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}