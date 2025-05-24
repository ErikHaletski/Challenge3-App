package de.challenge3.questapp.logik.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.core.app.ActivityCompat
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import de.challenge3.questapp.R
import de.challenge3.questapp.ui.activity.ActivityFragment
import de.challenge3.questapp.ui.activity.ActivityViewModel
import de.challenge3.questapp.ui.activity.MapState
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

class MapManager(
    private val fragment: Fragment,
    private val map: MapLibreMap,
    private val viewModel: ActivityViewModel,
    private val markerController: MarkerController,
    private val onMapClick: () -> Unit
) : MapController {

    private var isInitialized = false

    override fun initializeMap(onMapReady: () -> Unit) {
        if (isInitialized) return

        val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=aOkQL7uU6Vrzota1sb7B"
        map.setStyle(styleUrl) { style ->
            setupMapStyle(style)
            setupLocationIfPermitted()
            setupMarkers(style)
            setupMapClickListener()

            isInitialized = true
            onMapReady()
        }
    }

    private fun setupMapStyle(style: Style) {
        val bitmap = BitmapFactory.decodeResource(fragment.resources, R.drawable.map_marker)
            .scale(64, 64, false)
        style.addImage("quest-marker-icon", bitmap)
    }

    private fun setupLocationIfPermitted() {
        val hasPermission = enableUserLocation()
        if (!hasPermission) {
            (fragment as? ActivityFragment)?.requestLocationPermission()
        }
    }

    private fun setupMarkers(style: Style) {
        markerController.initialize(style)
        viewModel.completedQuests.observe(fragment.viewLifecycleOwner) { quests ->
            markerController.updateMarkers(quests)
        }
    }

    private fun setupMapClickListener() {
        map.addOnMapClickListener {
            onMapClick()
            viewModel.clearSelectedQuest()
            true
        }
    }

    override fun centerOnUserLocation() {
        val locationComponent = map.locationComponent
        if (!locationComponent.isLocationComponentEnabled) return

        locationComponent.lastKnownLocation?.let { location ->
            animateToLocation(LatLng(location.latitude, location.longitude), 14.0)
            viewModel.updateMapState(
                MapState(
                    isLocationEnabled = true,
                    currentLocation = LatLng(location.latitude, location.longitude)
                )
            )
        }
    }

    override fun animateToLocation(location: LatLng, zoom: Double) {
        val cameraPosition = CameraPosition.Builder()
            .target(location)
            .zoom(zoom)
            .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }
    @SuppressLint("MissingPermission")
    override fun enableUserLocation(): Boolean {
        val context = fragment.requireContext()

        if (!hasLocationPermissions(context)) return false

        val locationComponent = map.locationComponent
        val activationOptions = LocationComponentActivationOptions
            .builder(context, map.style!!)
            .useDefaultLocationEngine(true)
            .build()

        locationComponent.activateLocationComponent(activationOptions)
        locationComponent.isLocationComponentEnabled = true

        locationComponent.lastKnownLocation?.let { location ->
            animateToLocation(LatLng(location.latitude, location.longitude))
        }

        return true
    }

    private fun hasLocationPermissions(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        markerController.onDestroy()
    }
}
