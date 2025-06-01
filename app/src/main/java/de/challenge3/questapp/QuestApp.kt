package de.challenge3.questapp

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.google.firebase.FirebaseApp
import de.challenge3.questapp.database.AppDatabase
import org.maplibre.android.MapLibre

class QuestApp : Application() {

    companion object DatabaseSetup {
        var database: AppDatabase? = null
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        MapLibre.getInstance(this)
        QuestApp.database = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "stats-db"
        ).allowMainThreadQueries().fallbackToDestructiveMigration(false).build()

        Log.d("QuestApp", "QuestApp initialized")
    }
}
