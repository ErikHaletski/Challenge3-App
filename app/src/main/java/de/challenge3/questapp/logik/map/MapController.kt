package de.challenge3.questapp.logik.map

import org.maplibre.android.geometry.LatLng

interface MapController {
    fun initializeMap(onMapReady: () -> Unit)
    fun centerOnUserLocation()
    fun enableUserLocation(): Boolean
    fun animateToLocation(location: LatLng, zoom: Double = 10.5)
    fun onDestroy()
}
