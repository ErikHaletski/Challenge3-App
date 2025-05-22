package de.challenge3.questapp.logik.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.core.app.ActivityCompat
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import de.challenge3.questapp.R
import de.challenge3.questapp.ui.activity.ActivityFragment
import de.challenge3.questapp.ui.activity.ActivityViewModel
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

class MapManager(
    private val fragment: Fragment,
    private val map: MapLibreMap,
    private val viewModel: ActivityViewModel,
    private val markerHandler: QuestMarkerHandler,
    private val onMapClick: () -> Unit
) {
    fun initializeMap() {
        val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=aOkQL7uU6Vrzota1sb7B"
        map.setStyle(styleUrl) { style ->

            // Add marker icon
            val bitmap = BitmapFactory.decodeResource(fragment.resources, R.drawable.map_marker)
                .scale(64, 64, false)
            style.addImage("quest-marker-icon", bitmap)

            val hasPermission = enableUserLocation()
            if (!hasPermission) {
                (fragment as? ActivityFragment)?.requestLocationPermission()
            }

            (markerHandler as? QuestMarkerManager)?.initialize(style)
            markerHandler.addQuestMarkers(viewModel.completedQuests.value ?: emptyList())

            map.addOnMapClickListener {
                onMapClick()
                true
            }
        }
    }

    fun centerOnUserLocation() {
        val locationComponent = map.locationComponent

        if (!locationComponent.isLocationComponentEnabled) {
            return // Not enabled, probably no permission
        }

        locationComponent.lastKnownLocation?.let {
            val position = CameraPosition.Builder()
                .target(LatLng(it.latitude, it.longitude))
                .zoom(14.0) // Or adjust zoom to your preference
                .build()
            map.cameraPosition = position
        }
    }


    fun enableUserLocation(): Boolean {
        val context = fragment.requireContext()

        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val locationComponent = map.locationComponent
        val activationOptions = LocationComponentActivationOptions.builder(context, map.style!!)
            .useDefaultLocationEngine(true)
            .build()
        locationComponent.activateLocationComponent(activationOptions)
        locationComponent.isLocationComponentEnabled = true

        locationComponent.lastKnownLocation?.let {
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(it.latitude, it.longitude))
                .zoom(10.5)
                .build()
        }

        return true
    }

    fun onDestroy() {
        (markerHandler as? QuestMarkerManager)?.onDestroy()
    }
}
