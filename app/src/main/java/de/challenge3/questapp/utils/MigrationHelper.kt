package de.challenge3.questapp.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MigrationHelper(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("migration_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FRIENDS_MIGRATED = "friends_migrated"
        private const val KEY_QUESTS_MIGRATED = "quests_migrated"
    }

    fun runMigrationsIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            if (!isFriendsMigrated()) {
                migrateFriends()
            }

            if (!isQuestsMigrated()) {
                migrateQuests()
            }
        }
    }

    private fun isFriendsMigrated(): Boolean {
        return prefs.getBoolean(KEY_FRIENDS_MIGRATED, false)
    }

    private fun isQuestsMigrated(): Boolean {
        return prefs.getBoolean(KEY_QUESTS_MIGRATED, false)
    }

    private suspend fun migrateFriends() {
        try {
            val migration = FriendDataMigration(context)
            migration.migrateSampleDataToFirebase()

            prefs.edit().putBoolean(KEY_FRIENDS_MIGRATED, true).apply()
            println("Friends migration completed successfully")
        } catch (e: Exception) {
            println("Friends migration failed: ${e.message}")
        }
    }

    private fun migrateQuests() {
        try {
            val migration = DataMigration()
            migration.migrateSampleDataToFirebase()
            // You can implement quest migration here if needed
            prefs.edit().putBoolean(KEY_QUESTS_MIGRATED, true).apply()
            println("Quests migration completed successfully")
        } catch (e: Exception) {
            println("Quests migration failed: ${e.message}")
        }
    }

    fun resetMigrations() {
        prefs.edit()
            .putBoolean(KEY_FRIENDS_MIGRATED, false)
            .putBoolean(KEY_QUESTS_MIGRATED, false)
            .apply()
    }
}
