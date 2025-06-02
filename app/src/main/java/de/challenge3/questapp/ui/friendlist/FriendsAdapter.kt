package de.challenge3.questapp.ui.friendlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.challenge3.questapp.databinding.ItemFriendBinding
import de.challenge3.questapp.models.Friend

class FriendsAdapter(
    private val onRemoveFriend: (Friend) -> Unit
) : ListAdapter<Friend, FriendsAdapter.FriendViewHolder>(FriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FriendViewHolder(
        private val binding: ItemFriendBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: Friend) {
            binding.apply {
                textViewUsername.text = friend.displayName
                textViewLevel.text = "Level ${friend.level}"

                // Show current level XP, not total XP
                textViewExperience.text = "${friend.currentLevelExperience} XP"

                textViewQuestCount.text = "${friend.completedQuestsCount} quests"
                textViewStatus.text = friend.lastSeenFormatted

                // Set online indicator
                viewOnlineIndicator.visibility = if (friend.isOnline) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Set level progress (0-100% within current level)
                progressBarLevel.progress = (friend.levelProgress * 100).toInt()

                buttonRemoveFriend.setOnClickListener {
                    onRemoveFriend(friend)
                }
            }
        }
    }

    private class FriendDiffCallback : DiffUtil.ItemCallback<Friend>() {
        override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem == newItem
        }
    }
}
