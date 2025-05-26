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
import de.challenge3.questapp.logik.map.QuestMarkerManager
import de.challenge3.questapp.logik.map.QuestPopUpHandler
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
    private lateinit var questPopupHandler: QuestPopUpHandler
    private lateinit var friendCheckboxAdapter: FriendCheckboxAdapter
    private lateinit var questListAdapter: QuestListAdapter
    private var isUnifiedFilterExpanded = false
    private var isFriendFilterExpanded = false

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

        questPopupHandler = QuestPopUpHandler(binding)
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
        questListAdapter = QuestListAdapter(
            onQuestClick = { quest ->
                viewModel.selectQuest(quest)
                questPopupHandler.showPopup(viewModel.getQuestInfoText(quest),
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
            adapter = questListAdapter
        }

        // Setup tag filter spinner
        val tagOptions = mutableListOf("All Tags").apply {
            addAll(QuestTag.values().map { it.displayName })
        }
        val tagAdapter = createThemedSpinnerAdapter(tagOptions.toTypedArray())
        binding.spinnerTagFilter.adapter = tagAdapter

        binding.spinnerTagFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedTag = if (position == 0) null else QuestTag.values()[position - 1]
                viewModel.setTagFilter(selectedTag)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup sort spinner
        val sortAdapter = createThemedSpinnerAdapter(QuestSortOption.getDisplayNames())
        binding.spinnerSortBy.adapter = sortAdapter

        binding.spinnerSortBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = QuestSortOption.values()[position]
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

        // Friend filter toggle
        binding.friendFilterHeader.setOnClickListener {
            toggleFriendFilterVisibility()
        }

        // Everyone checkbox
        binding.checkboxEveryone.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEveryoneFilter(isChecked)
        }

        // Me checkbox
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

            // Also collapse friend filter when main filter is collapsed
            isFriendFilterExpanded = false
            binding.friendFilterOptions.visibility = View.GONE
            binding.iconFriendFilterExpand.rotation = 0f
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

        // Observe filter states
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

        // Observe filter summary
        viewModel.friendFilterSummary.observe(viewLifecycleOwner) { summary ->
            binding.textFriendFilterSummary.text = summary
        }

        // Observe unified filtered and sorted quest list
        viewModel.filteredAndSortedQuests.observe(viewLifecycleOwner) { quests ->
            questListAdapter.submitList(quests)
            binding.emptyStateLayout.visibility = if (quests.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewQuestList.visibility = if (quests.isEmpty()) View.GONE else View.VISIBLE

            // Update quest count
            binding.textQuestCount.text = "${quests.size} quests"
        }

        // Observe sort option
        viewModel.sortOption.observe(viewLifecycleOwner) { option ->
            val position = QuestSortOption.values().indexOf(option)
            if (binding.spinnerSortBy.selectedItemPosition != position) {
                binding.spinnerSortBy.setSelection(position)
            }
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        mapLibreMap = map

        val markerManager = QuestMarkerManager(
            map = mapLibreMap,
            mapView = mapView,
            iconId = "quest-marker-icon",
            onQuestClick = { quest, point ->
                viewModel.selectQuest(quest)
                questPopupHandler.showPopup(viewModel.getQuestInfoText(quest), point)

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
                questPopupHandler.hidePopup()
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
