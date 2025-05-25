package de.challenge3.questapp.ui.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.challenge3.questapp.databinding.FragmentDebugFriendBinding
import de.challenge3.questapp.utils.DebugFriendHelper
import kotlinx.coroutines.launch

class DebugFriendFragment : Fragment() {

    private var _binding: FragmentDebugFriendBinding? = null
    private val binding get() = _binding!!

    private lateinit var debugHelper: DebugFriendHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDebugFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        debugHelper = DebugFriendHelper(requireContext())

        setupButtons()
        loadCurrentUserInfo()
    }

    private fun setupButtons() {
        binding.buttonSimulateRequest.setOnClickListener {
            lifecycleScope.launch {
                debugHelper.simulateIncomingFriendRequest("TestFriend${(1..999).random()}")
                Toast.makeText(context, "Simulated incoming friend request!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonCreateSearchableUser.setOnClickListener {
            lifecycleScope.launch {
                debugHelper.createSearchableTestUser("Searchable${(1..999).random()}")
                Toast.makeText(context, "Created searchable test user!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonClearDebugData.setOnClickListener {
            lifecycleScope.launch {
                debugHelper.clearDebugData()
                Toast.makeText(context, "Cleared debug data!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonRefreshUserInfo.setOnClickListener {
            loadCurrentUserInfo()
        }
    }

    private fun loadCurrentUserInfo() {
        lifecycleScope.launch {
            val userInfo = debugHelper.getCurrentUserInfo()
            binding.textViewUserInfo.text = userInfo
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
