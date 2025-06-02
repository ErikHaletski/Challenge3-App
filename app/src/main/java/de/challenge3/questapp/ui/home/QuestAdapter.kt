// Adapter für den Recyclerview
// Verbindet die Daten (ViewModel) mit der UI (Fragment).
// Verwaltet, wie Quests dargestellt und interaktiv gemacht werd

package de.challenge3.questapp.ui.home

import android.app.AlertDialog
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.challenge3.questapp.R
import de.challenge3.questapp.ui.quest.Quest
import de.challenge3.questapp.ui.quest.QuestListItem

class QuestAdapter(
    private val items: List<QuestListItem>,
    private val onCheckboxChanged: (Quest, Boolean) -> Unit,
    private val onHeaderClicked: (QuestListItem.HeaderType) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_QUEST = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is QuestListItem.Header -> TYPE_HEADER
            is QuestListItem.QuestItem -> TYPE_QUEST
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_quest_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_quest, parent, false)
                QuestViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is QuestListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is QuestListItem.QuestItem -> (holder as QuestViewHolder).bind(item.quest)
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTitle: TextView = itemView.findViewById(R.id.headerTitle)

        fun bind(header: QuestListItem.Header) {
            headerTitle.text = header.title

            itemView.setOnClickListener {
                onHeaderClicked(header.type)
            }
        }
    }

    inner class QuestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.questTitle)
        private val description: TextView = itemView.findViewById(R.id.questDescription)
        private val xp: TextView = itemView.findViewById(R.id.questXP)
        private val checkbox: CheckBox = itemView.findViewById(R.id.questCheckbox)
        private val timer: TextView = itemView.findViewById(R.id.questTimer)


        fun bind(quest: Quest) {
            title.text = quest.title
            description.text = quest.description
            xp.text = "XP: ${quest.xpReward}"

            // Timer bei Daily Quests anzeigen
            if (quest.type == Quest.QuestType.DAILY && quest.expiresAt > 0) {
                val remainingMs = quest.expiresAt - System.currentTimeMillis()
                if (remainingMs > 0) {
                    val hours = (remainingMs / 1000 / 60 / 60)
                    val minutes = (remainingMs / 1000 / 60) % 60
                    timer.visibility = View.VISIBLE
                    timer.text = "⏰ ${hours}h ${minutes}m"
                } else {
                    itemView.visibility = View.GONE
                }
            } else {
                timer.visibility = View.GONE
            }

            checkbox.setOnCheckedChangeListener(null)
            checkbox.isChecked = quest.isCompleted

            // Durchstreichen + Transparenz bei erledigt
            if (quest.isCompleted) {
                title.paintFlags = title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                itemView.alpha = 0.5f
                itemView.visibility = View.GONE
            } else {
                title.paintFlags = title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                itemView.alpha = 1f
            }

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckboxChanged(quest, isChecked)
            }

            itemView.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle(quest.title)
                    .setMessage("${quest.description}\n\nXP: ${quest.xpReward}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }
}
