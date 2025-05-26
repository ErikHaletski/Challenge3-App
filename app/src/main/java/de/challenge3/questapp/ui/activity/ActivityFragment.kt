package de.challenge3.questapp.ui.activity

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import de.challenge3.questapp.databinding.FragmentActivityBinding
import de.challenge3.questapp.logik.map.MapManager
import de.challenge3.questapp.logik.map.QuestCompletionMarkerManager
import de.challenge3.questapp.logik.map.QuestCompletionPopUpHandler
import de.challenge3.questapp.repository.FirebaseFriendRepository
import de.challenge3.questapp.ui.home.QuestTag
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback

class ActivityFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var mapLibreMap: MapLibreMap

    private val viewModel: ActivityViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ActivityViewModel(requireContext()) as T
            }
        }
    }

    private var mapManager: MapManager? = null
    private lateinit var questCompletionPopupHandler: QuestCompletionPopUpHandler
    private lateinit var friendCheckboxAdapter: FriendCheckboxAdapter
    private lateinit var questCompletionListAdapter: QuestCompletionListAdapter
    private var isUnifiedFilterExpanded = false
    private var isFriendFilterExpanded = false
    private var isTagFilterExpanded = false

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineLocationGranted || coarseLocationGranted) {
            mapManager?.enableUserLocation()
        }
    }

    fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        questCompletionPopupHandler = QuestCompletionPopUpHandler(binding)
        setupUnifiedFilterUI()
        setupClickListeners()
        observeViewModel()

        return binding.root
    }

    private fun setupUnifiedFilterUI() {
        // Setup friend checkbox RecyclerView
        friendCheckboxAdapter = FriendCheckboxAdapter { friendId, isSelected ->
            viewModel.toggleFriendInFilter(friendId, isSelected)
        }

        binding.recyclerViewFriendCheckboxes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendCheckboxAdapter
            isNestedScrollingEnabled = false
        }

        // Get current user ID from repository
        val currentUserId = FirebaseFriendRepository(requireContext()).getCurrentUserId()

        // Setup unified quest list adapter
        questCompletionListAdapter = QuestCompletionListAdapter(
            onQuestClick = { quest ->
                viewModel.selectQuest(quest)
                questCompletionPopupHandler.showPopup(viewModel.getQuestInfoText(quest),
                    android.graphics.PointF(binding.mapView.width / 2f, binding.mapView.height / 2f))
                animateToQuest(quest)
            },
            onShowOnMapClick = { quest ->
                animateToQuest(quest)
                viewModel.selectQuest(quest)
            },
            currentUserId = currentUserId
        )

        binding.recyclerViewQuestList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = questCompletionListAdapter
        }

        // Setup sort spinner
        val sortAdapter = createThemedSpinnerAdapter(QuestCompletionSortOption.getDisplayNames())
        binding.spinnerSortBy.adapter = sortAdapter

        binding.spinnerSortBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = QuestCompletionSortOption.values()[position]
                viewModel.setSortOption(selectedOption)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun createThemedSpinnerAdapter(items: Array<String>): ArrayAdapter<String> {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    private fun setupClickListeners() {
        binding.btnCenterLocation.setOnClickListener {
            mapManager?.centerOnUserLocation()
        }

        // Unified filter toggle
        binding.btnToggleUnifiedFilter.setOnClickListener {
            toggleUnifiedFilterVisibility()
        }

        // Tag filter toggle
        binding.tagFilterHeader.setOnClickListener {
            toggleTagFilterVisibility()
        }

        // Friend filter toggle
        binding.friendFilterHeader.setOnClickListener {
            toggleFriendFilterVisibility()
        }

        // Tag checkboxes
        binding.checkboxAllTags.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAllTagsFilter(isChecked)
        }

        binding.checkboxMight.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleTagFilter(QuestTag.MIGHT, isChecked)
        }

        binding.checkboxMind.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleTagFilter(QuestTag.MIND, isChecked)
        }

        binding.checkboxHeart.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleTagFilter(QuestTag.HEART, isChecked)
        }

        binding.checkboxSpirit.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleTagFilter(QuestTag.SPIRIT, isChecked)
        }

        // Friend checkboxes
        binding.checkboxEveryone.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEveryoneFilter(isChecked)
        }

        binding.checkboxMe.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setMeFilter(isChecked)
        }
    }

    private fun toggleUnifiedFilterVisibility() {
        isUnifiedFilterExpanded = !isUnifiedFilterExpanded

        if (isUnifiedFilterExpanded) {
            // Expanded state - show full content
            binding.unifiedFilterContent.visibility = View.VISIBLE
            binding.textFiltersCollapsed.visibility = View.GONE
            binding.expandedHeader.visibility = View.VISIBLE
            binding.btnToggleUnifiedFilter.rotation = 180f
        } else {
            // Collapsed state - show minimal header
            binding.unifiedFilterContent.visibility = View.GONE
            binding.textFiltersCollapsed.visibility = View.VISIBLE
            binding.expandedHeader.visibility = View.GONE
            binding.btnToggleUnifiedFilter.rotation = 0f

            // Also collapse sub-filters when main filter is collapsed
            isTagFilterExpanded = false
            isFriendFilterExpanded = false
            binding.tagFilterOptions.visibility = View.GONE
            binding.friendFilterOptions.visibility = View.GONE
            binding.iconTagFilterExpand.rotation = 0f
            binding.iconFriendFilterExpand.rotation = 0f
        }
    }

    private fun toggleTagFilterVisibility() {
        isTagFilterExpanded = !isTagFilterExpanded

        if (isTagFilterExpanded) {
            binding.tagFilterOptions.visibility = View.VISIBLE
            binding.iconTagFilterExpand.rotation = 180f
        } else {
            binding.tagFilterOptions.visibility = View.GONE
            binding.iconTagFilterExpand.rotation = 0f
        }
    }

    private fun toggleFriendFilterVisibility() {
        isFriendFilterExpanded = !isFriendFilterExpanded

        if (isFriendFilterExpanded) {
            binding.friendFilterOptions.visibility = View.VISIBLE
            binding.iconFriendFilterExpand.rotation = 180f
        } else {
            binding.friendFilterOptions.visibility = View.GONE
            binding.iconFriendFilterExpand.rotation = 0f
        }
    }

    private fun animateToQuest(quest: de.challenge3.questapp.ui.home.QuestCompletion) {
        if (::mapLibreMap.isInitialized) {
            val cameraPosition = CameraPosition.Builder()
                .target(quest.location)
                .zoom(15.0)
                .build()
            mapLibreMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    private fun observeViewModel() {
        // Observe friend checkbox items
        viewModel.friendCheckboxItems.observe(viewLifecycleOwner) { items ->
            friendCheckboxAdapter.submitList(items)
            binding.textNoFriendsInFilter.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe tag filter states
        viewModel.isAllTagsSelected.observe(viewLifecycleOwner) { isSelected ->
            binding.checkboxAllTags.setOnCheckedChangeListener(null)
            binding.checkboxAllTags.isChecked = isSelected
            binding.checkboxAllTags.setOnCheckedChangeListener { _, checked ->
                viewModel.setAllTagsFilter(checked)
            }
        }

        viewModel.selectedTags.observe(viewLifecycleOwner) { selectedTags ->
            val isAllTags = viewModel.isAllTagsSelected.value ?: false

            // Update individual tag checkboxes
            binding.checkboxMight.setOnCheckedChangeListener(null)
            binding.checkboxMight.isChecked = !isAllTags && QuestTag.MIGHT in selectedTags
            binding.checkboxMight.setOnCheckedChangeListener { _, checked ->
                viewModel.toggleTagFilter(QuestTag.MIGHT, checked)
            }

            binding.checkboxMind.setOnCheckedChangeListener(null)
            binding.checkboxMind.isChecked = !isAllTags && QuestTag.MIND in selectedTags
            binding.checkboxMind.setOnCheckedChangeListener { _, checked ->
                viewModel.toggleTagFilter(QuestTag.MIND, checked)
            }

            binding.checkboxHeart.setOnCheckedChangeListener(null)
            binding.checkboxHeart.isChecked = !isAllTags && QuestTag.HEART in selectedTags
            binding.checkboxHeart.setOnCheckedChangeListener { _, checked ->
                viewModel.toggleTagFilter(QuestTag.HEART, checked)
            }

            binding.checkboxSpirit.setOnCheckedChangeListener(null)
            binding.checkboxSpirit.isChecked = !isAllTags && QuestTag.SPIRIT in selectedTags
            binding.checkboxSpirit.setOnCheckedChangeListener { _, checked ->
                viewModel.toggleTagFilter(QuestTag.SPIRIT, checked)
            }
        }

        // Observe friend filter states
        viewModel.isEveryoneSelected.observe(viewLifecycleOwner) { isSelected ->
            binding.checkboxEveryone.setOnCheckedChangeListener(null)
            binding.checkboxEveryone.isChecked = isSelected
            binding.checkboxEveryone.setOnCheckedChangeListener { _, checked ->
                viewModel.setEveryoneFilter(checked)
            }
        }

        viewModel.isMeSelected.observe(viewLifecycleOwner) { isSelected ->
            binding.checkboxMe.setOnCheckedChangeListener(null)
            binding.checkboxMe.isChecked = isSelected
            binding.checkboxMe.setOnCheckedChangeListener { _, checked ->
                viewModel.setMeFilter(checked)
            }
        }

        // Observe filter summaries
        viewModel.tagFilterSummary.observe(viewLifecycleOwner) { summary ->
            binding.textTagFilterSummary.text = summary
        }

        viewModel.friendFilterSummary.observe(viewLifecycleOwner) { summary ->
            binding.textFriendFilterSummary.text = summary
        }

        // Observe unified filtered and sorted quest list
        viewModel.filteredAndSortedQuests.observe(viewLifecycleOwner) { quests ->
            questCompletionListAdapter.submitList(quests)
            binding.emptyStateLayout.visibility = if (quests.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewQuestList.visibility = if (quests.isEmpty()) View.GONE else View.VISIBLE

            // Update quest count
            binding.textQuestCount.text = "${quests.size} quests"
        }

        // Observe sort option
        viewModel.sortOption.observe(viewLifecycleOwner) { option ->
            val position = QuestCompletionSortOption.values().indexOf(option)
            if (binding.spinnerSortBy.selectedItemPosition != position) {
                binding.spinnerSortBy.setSelection(position)
            }
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        mapLibreMap = map

        val markerManager = QuestCompletionMarkerManager(
            map = mapLibreMap,
            mapView = mapView,
            iconId = "quest-marker-icon",
            onQuestClick = { quest, point ->
                viewModel.selectQuest(quest)
                questCompletionPopupHandler.showPopup(viewModel.getQuestInfoText(quest), point)

                val cameraPosition = CameraPosition.Builder()
                    .target(quest.location)
                    .zoom(10.5)
                    .build()
                mapLibreMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        )

        mapManager = MapManager(
            fragment = this,
            map = mapLibreMap,
            viewModel = viewModel,
            markerController = markerManager,
            onMapClick = {
                questCompletionPopupHandler.hidePopup()
                viewModel.clearSelectedQuest()
            }
        )

        mapManager?.initializeMap {
            // Map is ready
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapManager?.onDestroy()
        mapView.onDestroy()
        _binding = null
    }
}
