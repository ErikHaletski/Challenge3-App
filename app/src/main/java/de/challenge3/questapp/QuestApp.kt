package de.challenge3.questapp

import android.app.Application
import org.maplibre.android.MapLibre

class QuestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
    }
}
