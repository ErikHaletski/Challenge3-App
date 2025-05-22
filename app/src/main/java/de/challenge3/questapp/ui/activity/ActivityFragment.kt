package de.challenge3.questapp.ui.activity

import QuestCompletion
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import de.challenge3.questapp.R
import de.challenge3.questapp.databinding.FragmentActivityBinding
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

class ActivityFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var mapLibreMap: MapLibreMap
    private var symbolManager: SymbolManager? = null

    private lateinit var questPopup: CardView
    private lateinit var questPopupText: TextView
    private lateinit var closePopupBtn: ImageView

    private val completedQuests = listOf(
        QuestCompletion(52.5200, 13.4050, System.currentTimeMillis(), "Defeat the Goblin King", "might"),
        QuestCompletion(48.8566, 2.3522, System.currentTimeMillis() - 3600000, "Solve the ancient riddle", "mind"),
        QuestCompletion(51.5074, -0.1278, System.currentTimeMillis() - 7200000, "Heal the wounded traveler", "heart")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        questPopup = binding.root.findViewById(R.id.questPopup)
        questPopupText = binding.root.findViewById(R.id.questPopupText)
        closePopupBtn = binding.root.findViewById(R.id.closePopupBtn)

        closePopupBtn.setOnClickListener {
            questPopup.visibility = View.GONE
        }

        return binding.root
    }

    override fun onMapReady(map: MapLibreMap) {
        mapLibreMap = map
        val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=aOkQL7uU6Vrzota1sb7B"
        mapLibreMap.setStyle(styleUrl) { style ->
            val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.map_marker)
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 64, 64, false)
            style.addImage("quest-marker-icon", scaledBitmap)

            enableUserLocation()

            symbolManager?.onDestroy()
            symbolManager = SymbolManager(mapView, mapLibreMap, style).apply {
                iconAllowOverlap = true
                textAllowOverlap = true
            }

            addQuestMarkers()

            // Hide popup when tapping on the map
            mapLibreMap.addOnMapClickListener {
                questPopup.visibility = View.GONE
                true
            }
        }
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
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

        locationComponent.lastKnownLocation?.let {
            val userLatLng = LatLng(it.latitude, it.longitude)
            val cameraPosition = CameraPosition.Builder()
                .target(userLatLng)
                .zoom(10.5)
                .build()
            mapLibreMap.cameraPosition = cameraPosition
        }
    }

    private fun addQuestMarkers() {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        symbolManager?.deleteAll()
        val questSymbolMap = mutableMapOf<Symbol, QuestCompletion>()

        for (quest in completedQuests) {
            val symbol = symbolManager!!.create(
                SymbolOptions()
                    .withLatLng(LatLng(quest.lat, quest.lng))
                    .withIconImage("quest-marker-icon")
                    .withIconSize(1.0f)
            )
            questSymbolMap[symbol] = quest
        }

        symbolManager!!.addClickListener { symbol ->
            val quest = questSymbolMap[symbol]
            quest?.let {
                val formattedTime = dateFormat.format(java.util.Date(it.timestamp))
                val text = "${it.tag.uppercase()}\n${it.questText}\nFinished at: $formattedTime"
                questPopupText.text = text

                val point = mapLibreMap.projection.toScreenLocation(symbol.latLng)

                questPopup.post {
                    questPopup.x = point.x - questPopup.width / 2
                    questPopup.y = point.y - questPopup.height - 30
                    questPopup.visibility = View.VISIBLE

                    // Hide after 20 seconds
                    questPopup.removeCallbacks(hidePopupRunnable)
                    questPopup.postDelayed(hidePopupRunnable, 20000)
                }
            }
            true
        }
    }

    private val hidePopupRunnable = Runnable {
        questPopup.visibility = View.GONE
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
        symbolManager?.onDestroy()
        mapView.onDestroy()
        questPopup.removeCallbacks(hidePopupRunnable)
        _binding = null
    }
}
