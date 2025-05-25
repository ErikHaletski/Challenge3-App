package de.challenge3.questapp

import android.app.Application
import com.google.firebase.FirebaseApp
import de.challenge3.questapp.utils.MigrationHelper
import org.maplibre.android.MapLibre

class QuestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        MapLibre.getInstance(this)

        MigrationHelper(this).runMigrationsIfNeeded()
    }
}
