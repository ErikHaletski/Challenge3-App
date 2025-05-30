package de.challenge3.questapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseApp
import de.challenge3.questapp.utils.MigrationHelper
import de.challenge3.questapp.utils.UserInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre

class QuestApp : Application() {
    // Application-level coroutine scope that won't be canceled on errors
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        MapLibre.getInstance(this)

        // Initialize user first
        UserInitializer(this).initializeUserIfNeeded()

        // Then run migrations in the application scope
        appScope.launch {
            try {
                MigrationHelper(this@QuestApp).runMigrationsIfNeeded()
            } catch (e: Exception) {
                Log.e("QuestApp", "Error running migrations", e)
            }
        }
    }
}