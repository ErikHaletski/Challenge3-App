package de.challenge3.questapp.ui.friendlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import de.challenge3.questapp.databinding.FragmentFriendlistBinding
import de.challenge3.questapp.repository.FirebaseFriendRepository
import kotlinx.coroutines.launch

// friend management screen
// -> displays freinds, shows friend requests, provides user search, handels friend request actions
class FriendlistFragment : Fragment() {

    private var _binding: FragmentFriendlistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendlistViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FriendlistViewModel(requireContext()) as T
            }
        }
    }

    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var friendRequestsAdapter: FriendRequestsAdapter
    private lateinit var searchResultsAdapter: SearchResultsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupSearchFunctionality()
        setupDebugButton()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        // Friends RecyclerView
        friendsAdapter = FriendsAdapter(
            onRemoveFriend = { friend ->
                viewModel.removeFriend(friend.id, friend.displayName)
            }
        )
        binding.recyclerViewFriends.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendsAdapter
        }

        // Friend Requests RecyclerView
        friendRequestsAdapter = FriendRequestsAdapter(
            onAcceptRequest = { request ->
                viewModel.acceptFriendRequest(request.id)
            },
            onDeclineRequest = { request ->
                viewModel.declineFriendRequest(request.id)
            }
        )
        binding.recyclerViewFriendRequests.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendRequestsAdapter
        }

        // Search Results RecyclerView
        searchResultsAdapter = SearchResultsAdapter(
            onSendFriendRequest = { user ->
                viewModel.sendFriendRequest(user.email)
            }
        )
        binding.recyclerViewSearchResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchResultsAdapter
        }
    }

    private fun setupSearchFunctionality() {
        binding.editTextSearch.addTextChangedListener { text ->
            val query = text.toString().trim()
            viewModel.searchUsers(query)
        }

        binding.buttonClearSearch.setOnClickListener {
            binding.editTextSearch.text?.clear()
            viewModel.clearSearchResults()
        }
    }

    private fun setupDebugButton() {
        // Add a debug button (you can remove this later)
        binding.buttonClearSearch.setOnLongClickListener {
            lifecycleScope.launch {
                val repository = FirebaseFriendRepository(requireContext())
                println("DEBUG: Current user ID: ${repository.getDebugUserId()}")
                val requests = repository.debugCheckFriendRequests()
                Toast.makeText(context, "Found ${requests.size} requests. Check logs.", Toast.LENGTH_LONG).show()
            }
            true
        }
    }

    private fun observeViewModel() {
        viewModel.friends.observe(viewLifecycleOwner) { friends ->
            friendsAdapter.submitList(friends)
            binding.textViewFriendsCount.text = "Friends (${friends.size})"
            binding.groupNoFriends.visibility = if (friends.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.friendRequests.observe(viewLifecycleOwner) { requests ->
            println("DEBUG: Fragment received ${requests.size} friend requests")
            friendRequestsAdapter.submitList(requests)
            binding.textViewRequestsCount.text = "Friend Requests (${requests.size})"
            binding.groupFriendRequests.visibility = if (requests.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchResultsAdapter.submitList(results)
            binding.groupSearchResults.visibility = if (results.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isSearching.observe(viewLifecycleOwner) { isSearching ->
            binding.progressBarSearch.visibility = if (isSearching) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
