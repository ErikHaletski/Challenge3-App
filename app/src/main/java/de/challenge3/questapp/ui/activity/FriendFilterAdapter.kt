package de.challenge3.questapp.ui.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.challenge3.questapp.databinding.ItemFriendFilterBinding
import de.challenge3.questapp.models.Friend

data class FriendFilterItem(
    val friend: Friend,
    val isSelected: Boolean,
    val questCount: Int
)

class FriendFilterAdapter(
    private val onFilterToggle: (String, Boolean) -> Unit
) : ListAdapter<FriendFilterItem, FriendFilterAdapter.FilterViewHolder>(FilterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemFriendFilterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FilterViewHolder(
        private val binding: ItemFriendFilterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FriendFilterItem) {
            binding.apply {
                textFriendName.text = item.friend.displayName
                textFriendQuestCount.text = "${item.questCount} quests"

                // Set online indicator
                viewOnlineIndicator.visibility = if (item.friend.isOnline) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Set switch state without triggering listener
                switchFriendFilter.setOnCheckedChangeListener(null)
                switchFriendFilter.isChecked = item.isSelected

                // Set listener after setting state
                switchFriendFilter.setOnCheckedChangeListener { _, isChecked ->
                    onFilterToggle(item.friend.id, isChecked)
                }

                // Make the whole item clickable
                root.setOnClickListener {
                    switchFriendFilter.toggle()
                }
            }
        }
    }

    private class FilterDiffCallback : DiffUtil.ItemCallback<FriendFilterItem>() {
        override fun areItemsTheSame(oldItem: FriendFilterItem, newItem: FriendFilterItem): Boolean {
            return oldItem.friend.id == newItem.friend.id
        }

        override fun areContentsTheSame(oldItem: FriendFilterItem, newItem: FriendFilterItem): Boolean {
            return oldItem == newItem
        }
    }
}
