package de.challenge3.questapp.ui.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.challenge3.questapp.databinding.ItemFriendCheckboxBinding
import de.challenge3.questapp.models.Friend

data class FriendCheckboxItem(
    val friend: Friend,
    val isSelected: Boolean,
    val questCount: Int
)

class FriendCheckboxAdapter(
    private val onFriendToggle: (String, Boolean) -> Unit
) : ListAdapter<FriendCheckboxItem, FriendCheckboxAdapter.CheckboxViewHolder>(CheckboxDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckboxViewHolder {
        val binding = ItemFriendCheckboxBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CheckboxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CheckboxViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CheckboxViewHolder(
        private val binding: ItemFriendCheckboxBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FriendCheckboxItem) {
            binding.apply {
                textFriendName.text = item.friend.displayName
                textFriendQuestCount.text = "${item.questCount} quests"

                // Set online indicator
                viewOnlineIndicator.visibility = if (item.friend.isOnline) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Set checkbox state without triggering listener
                checkboxFriend.setOnCheckedChangeListener(null)
                checkboxFriend.isChecked = item.isSelected

                // Set listener after setting state
                checkboxFriend.setOnCheckedChangeListener { _, isChecked ->
                    onFriendToggle(item.friend.id, isChecked)
                }

                // Make the whole item clickable
                root.setOnClickListener {
                    checkboxFriend.toggle()
                }
            }
        }
    }

    private class CheckboxDiffCallback : DiffUtil.ItemCallback<FriendCheckboxItem>() {
        override fun areItemsTheSame(oldItem: FriendCheckboxItem, newItem: FriendCheckboxItem): Boolean {
            return oldItem.friend.id == newItem.friend.id
        }

        override fun areContentsTheSame(oldItem: FriendCheckboxItem, newItem: FriendCheckboxItem): Boolean {
            return oldItem == newItem
        }
    }
}
