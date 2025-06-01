package de.challenge3.questapp.logik.map

import android.graphics.PointF
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import de.challenge3.questapp.databinding.FragmentActivityBinding
import de.challenge3.questapp.R

class QuestCompletionPopUpHandler(binding: FragmentActivityBinding) {

    private val questPopup: CardView = binding.root.findViewById(R.id.questPopup)
    private val questPopupText: TextView = binding.root.findViewById(R.id.questPopupText)
    private val closePopupBtn: ImageView = binding.root.findViewById(R.id.closePopupBtn)

    private val hidePopupRunnable = Runnable { hidePopup() }

    init {
        closePopupBtn.setOnClickListener { hidePopup() }
    }

    fun showPopup(text: String, point: PointF) {
        questPopupText.text = text
        questPopup.post {
            questPopup.x = point.x - questPopup.width / 2
            questPopup.y = point.y - questPopup.height - 30
            questPopup.visibility = View.VISIBLE
            questPopup.removeCallbacks(hidePopupRunnable)
            questPopup.postDelayed(hidePopupRunnable, 20000)
        }
    }

    fun hidePopup() {
        questPopup.visibility = View.GONE
        questPopup.removeCallbacks(hidePopupRunnable)
    }
}
