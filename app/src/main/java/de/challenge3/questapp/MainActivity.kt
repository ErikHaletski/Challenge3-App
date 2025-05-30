package de.challenge3.questapp

import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import de.challenge3.questapp.databinding.ActivityMainBinding
import de.challenge3.questapp.utils.DataMigration
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()

        // Debug: Long press to show device info and regenerate dummy data
        binding.root.setOnLongClickListener {
            showDeviceInfoAndRegenerateData()
            true
        }
    }

    private fun showDeviceInfoAndRegenerateData() {
        lifecycleScope.launch {
            try {
                val migration = DataMigration(this@MainActivity)
                val deviceInfo = migration.getDeviceInfo()

                // Show device info first
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Device Info:\n$deviceInfo",
                        Toast.LENGTH_LONG
                    ).show()
                }

                // Then regenerate data
                val success = migration.migrateSampleDataToFirebase()

                runOnUiThread {
                    if (success) {
                        Toast.makeText(
                            this@MainActivity,
                            "Dummy data regenerated successfully!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to regenerate dummy data",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setupNavigation() {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_achievements,
                R.id.navigation_activity,
                R.id.navigation_statspage,
                R.id.navigation_friendlist
            )
        )

        navView.setupWithNavController(navController)
    }
}
