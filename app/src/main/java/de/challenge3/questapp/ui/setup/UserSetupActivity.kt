package de.challenge3.questapp.ui.setup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import de.challenge3.questapp.MainActivity
import de.challenge3.questapp.databinding.ActivityUserSetupBinding

class UserSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserSetupBinding
    private val viewModel: UserSetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Username input validation
        binding.editTextUsername.addTextChangedListener { text ->
            val username = text?.toString() ?: ""
            viewModel.validateUsername(username)
        }

        // Continue button
        binding.buttonContinue.setOnClickListener {
            val username = binding.editTextUsername.text?.toString()?.trim() ?: ""
            if (username.isNotEmpty()) {
                viewModel.completeSetup(username)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isUsernameValid.observe(this) { isValid ->
            binding.buttonContinue.isEnabled = isValid

            val currentText = binding.editTextUsername.text?.toString() ?: ""
            if (currentText.isNotEmpty()) {
                binding.textInputLayoutUsername.error = if (isValid) {
                    null
                } else {
                    "Username must be 2-20 characters, letters, numbers, and underscores only"
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.buttonContinue.isEnabled = !isLoading && (viewModel.isUsernameValid.value == true)
            binding.progressBar.visibility = if (isLoading) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }

        viewModel.setupComplete.observe(this) { isComplete ->
            if (isComplete) {
                // Navigate to main activity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }
}
