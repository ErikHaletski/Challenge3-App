package de.challenge3.questapp.ui.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.challenge3.questapp.R
import de.challenge3.questapp.databinding.ItemQuestListBinding
import de.challenge3.questapp.ui.home.QuestCompletion
import de.challenge3.questapp.ui.home.QuestTag
import java.text.SimpleDateFormat
import java.util.*

class QuestCompletionListAdapter(
    private val onQuestClick: (QuestCompletion) -> Unit,
    private val onShowOnMapClick: (QuestCompletion) -> Unit,
    private val currentUserId: String
) : ListAdapter<QuestCompletion, QuestCompletionListAdapter.QuestViewHolder>(QuestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val binding = ItemQuestListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return QuestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuestViewHolder(
        private val binding: ItemQuestListBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(quest: QuestCompletion) {
            binding.apply {
                // Set quest tag
                textQuestTag.text = quest.tag.displayName.uppercase()

                // Set tag color based on quest type using your defined colors
                val tagColor = getTagColor(quest.tag)
                questTagIndicator.setBackgroundColor(tagColor)
                textQuestTag.setTextColor(tagColor)

                // Show title if existing, else questText
                if (quest.questTitle.isNotEmpty()) {
                    textQuestText.text = "✅ ${quest.questTitle}"
                } else {
                    textQuestText.text = quest.questText
                }

                // Set experience points with your theme colors
                textExperiencePoints.text = "+${quest.experiencePoints} XP"
                textExperiencePoints.setTextColor(
                    ContextCompat.getColor(root.context, R.color.teal_200)
                )

                // Set username (show "You" for current user)
                textUsername.text = if (quest.userId == currentUserId) {
                    "You"
                } else {
                    quest.username.ifEmpty { "Friend" }
                }

                // Set timestamp
                textTimestamp.text = getRelativeTimeString(quest.timestamp)

                // Set click listeners
                root.setOnClickListener {
                    onQuestClick(quest)
                }

                btnShowOnMap.setOnClickListener {
                    onShowOnMapClick(quest)
                }
            }
        }

        private fun getTagColor(tag: QuestTag): Int {
            val context = binding.root.context
            return when (tag) {
                QuestTag.MIGHT -> ContextCompat.getColor(context, R.color.tag_might)
                QuestTag.MIND -> ContextCompat.getColor(context, R.color.tag_mind)
                QuestTag.HEART -> ContextCompat.getColor(context, R.color.tag_heart)
                QuestTag.SPIRIT -> ContextCompat.getColor(context, R.color.tag_spirit)
            }
        }

        private fun getRelativeTimeString(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> "${diff / 3600000}h ago"
                diff < 604800000 -> "${diff / 86400000}d ago"
                else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

    private class QuestDiffCallback : DiffUtil.ItemCallback<QuestCompletion>() {
        override fun areItemsTheSame(oldItem: QuestCompletion, newItem: QuestCompletion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: QuestCompletion, newItem: QuestCompletion): Boolean {
            return oldItem == newItem
        }
    }
}
