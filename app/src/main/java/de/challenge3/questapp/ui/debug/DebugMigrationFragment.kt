package de.challenge3.questapp.ui.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.challenge3.questapp.R
import de.challenge3.questapp.utils.DataMigration
import de.challenge3.questapp.utils.MigrationHelper
import kotlinx.coroutines.launch

class DebugMigrationFragment : Fragment() {

    private lateinit var statusText: TextView
    private lateinit var deviceInfoText: TextView
    private lateinit var resetButton: Button
    private lateinit var generateButton: Button
    private lateinit var showDeviceInfoButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_debug_migration, container, false)

        statusText = view.findViewById(R.id.textMigrationStatus)
        deviceInfoText = view.findViewById(R.id.textDeviceInfo)
        resetButton = view.findViewById(R.id.buttonResetMigration)
        generateButton = view.findViewById(R.id.buttonGenerateDummyData)
        showDeviceInfoButton = view.findViewById(R.id.buttonShowDeviceInfo)

        resetButton.setOnClickListener { resetMigrations() }
        generateButton.setOnClickListener { generateDummyData() }
        showDeviceInfoButton.setOnClickListener { showDeviceInfo() }

        updateStatus()
        showDeviceInfo()

        return view
    }

    private fun updateStatus() {
        val prefs = requireContext().getSharedPreferences("migration_prefs", 0)
        val questsMigrated = prefs.getBoolean("quests_migrated", false)
        val attemptCount = prefs.getInt("migration_attempt_count", 0)

        statusText.text = "Quests migrated: $questsMigrated\nAttempt count: $attemptCount"
    }

    private fun showDeviceInfo() {
        val migration = DataMigration(requireContext())
        deviceInfoText.text = migration.getDeviceInfo()
    }

    private fun resetMigrations() {
        lifecycleScope.launch {
            val helper = MigrationHelper(requireContext())
            helper.resetMigrations()
            Toast.makeText(context, "Migration flags reset", Toast.LENGTH_SHORT).show()
            updateStatus()
        }
    }

    private fun generateDummyData() {
        lifecycleScope.launch {
            try {
                val migration = DataMigration(requireContext())
                val success = migration.migrateSampleDataToFirebase()

                if (success) {
                    Toast.makeText(context, "Dummy data generated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to generate dummy data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            updateStatus()
        }
    }
}
