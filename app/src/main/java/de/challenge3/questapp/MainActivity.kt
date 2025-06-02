package de.challenge3.questapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.challenge3.questapp.databinding.ActivityMainBinding
import de.challenge3.questapp.ui.setup.UserSetupActivity
import de.challenge3.questapp.utils.UserManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userManager = UserManager(this)

        // Check if user setup is needed
        lifecycleScope.launch {
            val needsSetup = userManager.ensureUserExists()
            if (needsSetup) {
                // Redirect to setup activity
                val intent = Intent(this@MainActivity, UserSetupActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return@launch
            }

            // User setup is complete, continue with normal flow
            setupMainActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        // Set user as online when app comes to foreground
        lifecycleScope.launch {
            userManager.setUserOnline()
                .onFailure { error ->
                    println("Failed to set user online: ${error.message}")
                }
        }
    }

    override fun onPause() {
        super.onPause()
        // Set user as offline when app goes to background
        lifecycleScope.launch {
            userManager.setUserOffline()
                .onFailure { error ->
                    println("Failed to set user offline: ${error.message}")
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Set user as offline when app is destroyed
        lifecycleScope.launch {
            userManager.setUserOffline()
        }
    }

    private fun setupMainActivity() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()

        // Debug: Long press to show user info
        binding.root.setOnLongClickListener {
            showUserInfo()
            true
        }
    }

    private fun showUserInfo() {
        val debugInfo = userManager.getDebugInfo()
        Toast.makeText(this, "User Info:\n$debugInfo", Toast.LENGTH_LONG).show()
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