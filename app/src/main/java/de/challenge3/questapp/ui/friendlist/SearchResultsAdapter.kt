package de.challenge3.questapp.ui.friendlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.challenge3.questapp.databinding.ItemSearchResultBinding
import de.challenge3.questapp.models.Friend

class SearchResultsAdapter(
    private val onSendFriendRequest: (Friend) -> Unit
) : ListAdapter<Friend, SearchResultsAdapter.SearchResultViewHolder>(SearchResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SearchResultViewHolder(
        private val binding: ItemSearchResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: Friend) {
            binding.apply {
                textViewUsername.text = user.displayName
                textViewEmail.text = user.email
                textViewLevel.text = "Level ${user.level}"
                textViewQuestCount.text = "${user.completedQuestsCount} quests"

                buttonSendRequest.setOnClickListener {
                    onSendFriendRequest(user)
                }
            }
        }
    }

    private class SearchResultDiffCallback : DiffUtil.ItemCallback<Friend>() {
        override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem == newItem
        }
    }
}
