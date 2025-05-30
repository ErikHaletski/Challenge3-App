package de.challenge3.questapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Zentrale Klasse für Location-Handling in der App.
 * Vereinfacht den Zugriff auf Location und Permission-Handling.
 */
class LocationHelper(private val context: Context) {

    companion object {
        // Fallback-Koordinaten (Berlin)
        const val DEFAULT_LAT = 52.5200
        const val DEFAULT_LNG = 13.4050
    }

    /**
     * Prüft ob Location-Permissions vorhanden sind
     */
    fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Holt die letzte bekannte Location oder gibt Fallback zurück
     */
    fun getLastKnownLocation(): Pair<Double, Double> {
        if (!hasLocationPermissions()) {
            return Pair(DEFAULT_LAT, DEFAULT_LNG)
        }

        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            return if (lastKnown != null) {
                Pair(lastKnown.latitude, lastKnown.longitude)
            } else {
                Pair(DEFAULT_LAT, DEFAULT_LNG)
            }
        } catch (e: SecurityException) {
            return Pair(DEFAULT_LAT, DEFAULT_LNG)
        }
    }

    /**
     * Asynchrone Version mit Callback
     */
    fun getLocationAsync(callback: (Double, Double) -> Unit) {
        val location = getLastKnownLocation()
        callback(location.first, location.second)
    }

    /**
     * Erstellt einen Permission Launcher für ein Fragment
     */
    fun createPermissionLauncher(
        fragment: Fragment,
        onPermissionResult: (Boolean) -> Unit = {}
    ): androidx.activity.result.ActivityResultLauncher<Array<String>> {
        return fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            onPermissionResult(granted)
        }
    }

    /**
     * Fordert Permissions an wenn nötig
     */
    fun requestPermissionsIfNeeded(
        permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
    ) {
        if (!hasLocationPermissions()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}
