package de.challenge3.questapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.room.Room
import com.google.firebase.FirebaseApp
import de.challenge3.questapp.database.AppDatabase
import de.challenge3.questapp.utils.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre

class QuestApp : Application(), DefaultLifecycleObserver {

    companion object DatabaseSetup {
        var database: AppDatabase? = null
    }

    private lateinit var userManager: UserManager
    private val applicationScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super<Application>.onCreate()
        FirebaseApp.initializeApp(this)
        MapLibre.getInstance(this)
        QuestApp.database = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "stats-db"
        ).allowMainThreadQueries().fallbackToDestructiveMigration(false).build()

        userManager = UserManager(this)

        // Register for app lifecycle events
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        Log.d("QuestApp", "QuestApp initialized")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // App moved to foreground
        applicationScope.launch {
            if (userManager.isUserSetupComplete()) {
                userManager.setUserOnline()
                    .onSuccess {
                        Log.d("QuestApp", "User set to online")
                    }
                    .onFailure { error ->
                        Log.e("QuestApp", "Failed to set user online: ${error.message}")
                    }
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // App moved to background
        applicationScope.launch {
            if (userManager.isUserSetupComplete()) {
                userManager.setUserOffline()
                    .onSuccess {
                        Log.d("QuestApp", "User set to offline")
                    }
                    .onFailure { error ->
                        Log.e("QuestApp", "Failed to set user offline: ${error.message}")
                    }
            }
        }
    }
}