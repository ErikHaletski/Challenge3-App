package de.challenge3.questapp.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import de.challenge3.questapp.databinding.FragmentActivityBinding
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback

// a fragment represents a part of the UI
// OnMapReadyCallback -> called onMapReady(map) wenn the map initialisiert und ready ist
class ActivityFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private lateinit var mapLibreMap: MapLibreMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return binding.root
    }

    override fun onMapReady(map: MapLibreMap) {
        mapLibreMap = map
        val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=aOkQL7uU6Vrzota1sb7B"
        mapLibreMap.setStyle(styleUrl) {
            enableUserLocation()
        }
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        val locationComponent = mapLibreMap.locationComponent
        val activationOptions = LocationComponentActivationOptions.builder(requireContext(), mapLibreMap.style!!)
            .useDefaultLocationEngine(true)
            .build()

        locationComponent.activateLocationComponent(activationOptions)
        locationComponent.isLocationComponentEnabled = true

        val lastLocation: Location? = locationComponent.lastKnownLocation
        lastLocation?.let {
            val userLatLng = LatLng(it.latitude, it.longitude)
            val cameraPosition = CameraPosition.Builder()
                .target(userLatLng)
                .zoom(10.5) // Adjust as needed
                .build()
            mapLibreMap.cameraPosition = cameraPosition
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
        mapView.onDestroy()
        _binding = null
    }
}
