package de.challenge3.questapp.ui.friendlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.challenge3.questapp.databinding.ItemFriendRequestBinding
import de.challenge3.questapp.models.FriendRequest

class FriendRequestsAdapter(
    private val onAcceptRequest: (FriendRequest) -> Unit,
    private val onDeclineRequest: (FriendRequest) -> Unit
) : ListAdapter<FriendRequest, FriendRequestsAdapter.RequestViewHolder>(RequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemFriendRequestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RequestViewHolder(
        private val binding: ItemFriendRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(request: FriendRequest) {
            binding.apply {
                val fromUser = request.fromUser
                if (fromUser != null) {
                    textViewUsername.text = fromUser.displayName
                    textViewLevel.text = "Level ${fromUser.level}"

                    textViewQuestCount.text = "${fromUser.currentLevelExperience} XP • ${fromUser.completedQuestsCount} quests"
                } else {
                    textViewUsername.text = "Unknown User"
                    textViewLevel.text = ""
                    textViewQuestCount.text = ""
                }

                textViewTimeAgo.text = request.timeAgo

                buttonAccept.setOnClickListener {
                    onAcceptRequest(request)
                }

                buttonDecline.setOnClickListener {
                    onDeclineRequest(request)
                }
            }
        }
    }

    private class RequestDiffCallback : DiffUtil.ItemCallback<FriendRequest>() {
        override fun areItemsTheSame(oldItem: FriendRequest, newItem: FriendRequest): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FriendRequest, newItem: FriendRequest): Boolean {
            return oldItem == newItem
        }
    }
}
