package de.challenge3.questapp.ui.activity

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

    private val viewModel: ActivityViewModel by viewModels()
    private var mapManager: MapManager? = null
    private lateinit var questPopupHandler: QuestPopUpHandler

    // Modern permission request launcher
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
        setupClickListeners()

        return binding.root
    }

    private fun setupClickListeners() {
        binding.btnCenterLocation.setOnClickListener {
            mapManager?.centerOnUserLocation()
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        mapLibreMap = map

        val markerManager = QuestMarkerManager(
            map = mapLibreMap,
            mapView = mapView,
            iconId = "quest-marker-icon",
            onQuestClick = { quest, point ->
                // Update ViewModel with selected quest
                viewModel.selectQuest(quest)

                // Show popup
                questPopupHandler.showPopup(viewModel.getQuestInfoText(quest), point)

                // Center and zoom to marker
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
            // Map is ready, you can perform additional setup here
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
