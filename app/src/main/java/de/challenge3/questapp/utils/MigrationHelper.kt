package de.challenge3.questapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MigrationHelper(private val context: Context) {
    private val TAG = "MigrationHelper"
    private val prefs: SharedPreferences = context.getSharedPreferences("migration_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FRIENDS_MIGRATED = "friends_migrated"
        private const val KEY_QUESTS_MIGRATED = "quests_migrated"
        private const val KEY_MIGRATION_ATTEMPT_COUNT = "migration_attempt_count"
    }

    // Make this a suspend function to properly handle async operations
    suspend fun runMigrationsIfNeeded() = withContext(Dispatchers.IO) {
        try {
            // Always run quest migration on fresh install or if previous attempts failed
            val attemptCount = prefs.getInt(KEY_MIGRATION_ATTEMPT_COUNT, 0)
            val questsMigrated = prefs.getBoolean(KEY_QUESTS_MIGRATED, false)

            Log.d(TAG, "Migration status: quests=$questsMigrated, attempts=$attemptCount")

            // Force migration for testing or if previous attempts were unsuccessful
            if (!questsMigrated || attemptCount < 3) {
                // Increment attempt counter
                prefs.edit().putInt(KEY_MIGRATION_ATTEMPT_COUNT, attemptCount + 1).apply()

                // Run migration
                val success = migrateQuests()

                // Only mark as migrated if successful
                if (success) {
                    prefs.edit().putBoolean(KEY_QUESTS_MIGRATED, true).apply()
                    Log.d(TAG, "Quest migration marked as completed")
                } else {
                    Log.w(TAG, "Quest migration failed, will retry on next app start")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during migration", e)
        }
    }

    // Return success/failure status
    private suspend fun migrateQuests(): Boolean {
        return try {
            Log.d(TAG, "Starting quest migration...")
            val migration = DataMigration(context)
            val success = migration.migrateSampleDataToFirebase()

            if (success) {
                Log.d(TAG, "Quest migration completed successfully")
            } else {
                Log.w(TAG, "Quest migration returned false")
            }

            return success
        } catch (e: Exception) {
            Log.e(TAG, "Quest migration failed with exception", e)
            return false
        }
    }

    // Reset migration flags for testing
    fun resetMigrations() {
        prefs.edit()
            .putBoolean(KEY_FRIENDS_MIGRATED, false)
            .putBoolean(KEY_QUESTS_MIGRATED, false)
            .putInt(KEY_MIGRATION_ATTEMPT_COUNT, 0)
            .apply()
        Log.d(TAG, "Migration flags reset")
    }
}
