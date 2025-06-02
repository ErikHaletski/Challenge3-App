package de.challenge3.questapp.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import de.challenge3.questapp.utils.LocationHelper
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
    private lateinit var locationHelper: LocationHelper

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

    private var filterStates = FilterStates()
    private lateinit var locationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        locationHelper = LocationHelper(requireContext())
        locationPermissionLauncher = locationHelper.createPermissionLauncher(this) { granted ->
            if (granted) mapManager?.enableUserLocation()
        }

        questCompletionPopupHandler = QuestCompletionPopUpHandler(binding)
        setupUI()
        setupClickListeners()
        observeViewModel()

        println("ActivityFragment: onCreateView completed")
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        println("ActivityFragment: onResume - refreshing data")
        viewModel.refreshData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("ActivityFragment: onViewCreated - starting initial data load")

        viewModel.refreshData()
    }

    fun requestLocationPermission() {
        locationHelper.requestPermissionsIfNeeded(locationPermissionLauncher)
    }

    private fun setupUI() {
        setupRecyclerViews()
        setupSortSpinner()
    }

    private fun setupRecyclerViews() {
        friendCheckboxAdapter = FriendCheckboxAdapter { friendId, isSelected ->
            viewModel.toggleFriendInFilter(friendId, isSelected)
        }

        binding.recyclerViewFriendCheckboxes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendCheckboxAdapter
            isNestedScrollingEnabled = false
        }

        val currentUserId = FirebaseFriendRepository(requireContext()).getCurrentUserId()
        questCompletionListAdapter = QuestCompletionListAdapter(
            onQuestClick = { quest ->
                viewModel.selectQuest(quest)
                questCompletionPopupHandler.showPopup(
                    viewModel.getQuestInfoText(quest),
                    android.graphics.PointF(binding.mapView.width / 2f, binding.mapView.height / 2f)
                )
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
    }

    private fun setupSortSpinner() {
        val sortAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            QuestCompletionSortOption.getDisplayNames()
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerSortBy.adapter = sortAdapter
        binding.spinnerSortBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setSortOption(QuestCompletionSortOption.values()[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupClickListeners() {
        binding.btnCenterLocation.setOnClickListener {
            mapManager?.centerOnUserLocation()
        }

        setupFilterClickListeners()
        setupTagCheckboxListeners()
        setupFriendCheckboxListeners()
    }

    private fun setupFilterClickListeners() {
        binding.btnToggleUnifiedFilter.setOnClickListener {
            filterStates.toggleUnifiedFilter()
            updateFilterVisibility()
        }

        binding.tagFilterHeader.setOnClickListener {
            filterStates.toggleTagFilter()
            updateFilterVisibility()
        }

        binding.friendFilterHeader.setOnClickListener {
            filterStates.toggleFriendFilter()
            updateFilterVisibility()
        }
    }

    private fun setupTagCheckboxListeners() {
        binding.checkboxAllTags.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAllTagsFilter(isChecked)
        }

        mapOf(
            binding.checkboxMight to QuestTag.MIGHT,
            binding.checkboxMind to QuestTag.MIND,
            binding.checkboxHeart to QuestTag.HEART,
            binding.checkboxSpirit to QuestTag.SPIRIT
        ).forEach { (checkbox, tag) ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleTagFilter(tag, isChecked)
            }
        }
    }

    private fun setupFriendCheckboxListeners() {
        binding.checkboxEveryone.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEveryoneFilter(isChecked)
        }

        binding.checkboxMe.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setMeFilter(isChecked)
        }
    }

    private fun updateFilterVisibility() {
        with(binding) {
            if (filterStates.isUnifiedFilterExpanded) {
                unifiedFilterContent.visibility = View.VISIBLE
                textFiltersCollapsed.visibility = View.GONE
                expandedHeader.visibility = View.VISIBLE
                btnToggleUnifiedFilter.rotation = 180f
            } else {
                unifiedFilterContent.visibility = View.GONE
                textFiltersCollapsed.visibility = View.VISIBLE
                expandedHeader.visibility = View.GONE
                btnToggleUnifiedFilter.rotation = 0f
                filterStates.collapseSubFilters()
            }

            tagFilterOptions.visibility = if (filterStates.isTagFilterExpanded) View.VISIBLE else View.GONE
            iconTagFilterExpand.rotation = if (filterStates.isTagFilterExpanded) 180f else 0f

            friendFilterOptions.visibility = if (filterStates.isFriendFilterExpanded) View.VISIBLE else View.GONE
            iconFriendFilterExpand.rotation = if (filterStates.isFriendFilterExpanded) 180f else 0f
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
        viewModel.filteredAndSortedQuests.observe(viewLifecycleOwner) { quests ->
            println("ActivityFragment: Received ${quests.size} filtered quests")
            questCompletionListAdapter.submitList(quests)
            binding.emptyStateLayout.visibility = if (quests.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewQuestList.visibility = if (quests.isEmpty()) View.GONE else View.VISIBLE
            binding.textQuestCount.text = "${quests.size} quests"
        }

        viewModel.friendCheckboxItems.observe(viewLifecycleOwner) { items ->
            friendCheckboxAdapter.submitList(items)
            binding.textNoFriendsInFilter.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }

        observeTagFilters()
        observeFriendFilters()
        observeSortOptions()
    }

    private fun observeTagFilters() {
        viewModel.isAllTagsSelected.observe(viewLifecycleOwner) { isSelected ->
            binding.checkboxAllTags.setOnCheckedChangeListener(null)
            binding.checkboxAllTags.isChecked = isSelected
            binding.checkboxAllTags.setOnCheckedChangeListener { _, checked ->
                viewModel.setAllTagsFilter(checked)
            }
        }

        viewModel.selectedTags.observe(viewLifecycleOwner) { selectedTags ->
            val isAllTags = viewModel.isAllTagsSelected.value ?: false
            updateTagCheckboxes(selectedTags, isAllTags)
        }

        viewModel.tagFilterSummary.observe(viewLifecycleOwner) { summary ->
            binding.textTagFilterSummary.text = summary
        }
    }

    private fun updateTagCheckboxes(selectedTags: Set<QuestTag>, isAllTags: Boolean) {
        mapOf(
            binding.checkboxMight to QuestTag.MIGHT,
            binding.checkboxMind to QuestTag.MIND,
            binding.checkboxHeart to QuestTag.HEART,
            binding.checkboxSpirit to QuestTag.SPIRIT
        ).forEach { (checkbox, tag) ->
            checkbox.setOnCheckedChangeListener(null)
            checkbox.isChecked = !isAllTags && tag in selectedTags
            checkbox.setOnCheckedChangeListener { _, checked ->
                viewModel.toggleTagFilter(tag, checked)
            }
        }
    }

    private fun observeFriendFilters() {
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

        viewModel.friendFilterSummary.observe(viewLifecycleOwner) { summary ->
            binding.textFriendFilterSummary.text = summary
        }
    }

    private fun observeSortOptions() {
        viewModel.sortOption.observe(viewLifecycleOwner) { option ->
            val position = QuestCompletionSortOption.values().indexOf(option)
            if (binding.spinnerSortBy.selectedItemPosition != position) {
                binding.spinnerSortBy.setSelection(position)
            }
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        println("ActivityFragment: Map is ready")
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
            locationHelper = locationHelper,
            onMapClick = {
                questCompletionPopupHandler.hidePopup()
                viewModel.clearSelectedQuest()
            }
        )

        mapManager?.initializeMap {
            println("ActivityFragment: Map initialization completed")
        }
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

    private data class FilterStates(
        var isUnifiedFilterExpanded: Boolean = false,
        var isTagFilterExpanded: Boolean = false,
        var isFriendFilterExpanded: Boolean = false
    ) {
        fun toggleUnifiedFilter() {
            isUnifiedFilterExpanded = !isUnifiedFilterExpanded
            if (!isUnifiedFilterExpanded) collapseSubFilters()
        }

        fun toggleTagFilter() {
            isTagFilterExpanded = !isTagFilterExpanded
        }

        fun toggleFriendFilter() {
            isFriendFilterExpanded = !isFriendFilterExpanded
        }

        fun collapseSubFilters() {
            isTagFilterExpanded = false
            isFriendFilterExpanded = false
        }
    }
}
