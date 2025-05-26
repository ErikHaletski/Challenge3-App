package de.challenge3.questapp.ui.friendlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.challenge3.questapp.databinding.FragmentFriendlistBinding
import de.challenge3.questapp.ui.home.HomeViewModel

class FriendlistFragment : Fragment() {

    private var _binding: FragmentFriendlistBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendlistBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textFriendlist
        textView.text = "Freunde kommen bald!"

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
