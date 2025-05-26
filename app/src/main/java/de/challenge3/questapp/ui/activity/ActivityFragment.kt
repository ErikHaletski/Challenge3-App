package de.challenge3.questapp.ui.activity

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var friendFilterAdapter: FriendFilterAdapter
    private var isFilterExpanded = false

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
        setupFilterUI()
        setupClickListeners()
        observeViewModel()

        return binding.root
    }

    private fun setupFilterUI() {
        // Setup friend filter RecyclerView
        friendFilterAdapter = FriendFilterAdapter { friendId, isSelected ->
            viewModel.toggleFriendFilter(friendId, isSelected)
        }

        binding.recyclerViewFriendFilters.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendFilterAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.btnCenterLocation.setOnClickListener {
            mapManager?.centerOnUserLocation()
        }

        // Filter toggle
        binding.btnToggleFilter.setOnClickListener {
            toggleFilterVisibility()
        }

        // My quests toggle
        binding.switchMyQuests.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleMyQuests(isChecked)
        }

        // Select/Deselect all buttons
        binding.btnSelectAll.setOnClickListener {
            viewModel.selectAllFriends()
        }

        binding.btnDeselectAll.setOnClickListener {
            viewModel.deselectAllFriends()
        }
    }

    private fun toggleFilterVisibility() {
        isFilterExpanded = !isFilterExpanded
        binding.filterContent.visibility = if (isFilterExpanded) View.VISIBLE else View.GONE

        // Rotate the expand icon
        val rotation = if (isFilterExpanded) 180f else 0f
        binding.btnToggleFilter.animate().rotation(rotation).setDuration(200).start()
    }

    private fun observeViewModel() {
        // Observe friend filter items
        viewModel.friendFilterItems.observe(viewLifecycleOwner) { items ->
            friendFilterAdapter.submitList(items)
            binding.textNoFriends.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe my quest count
        viewModel.myQuestCount.observe(viewLifecycleOwner) { count ->
            binding.textMyQuestCount.text = "$count quests"
        }

        // Observe my quests toggle state
        viewModel.showMyQuests.observe(viewLifecycleOwner) { show ->
            binding.switchMyQuests.setOnCheckedChangeListener(null)
            binding.switchMyQuests.isChecked = show
            binding.switchMyQuests.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleMyQuests(isChecked)
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
