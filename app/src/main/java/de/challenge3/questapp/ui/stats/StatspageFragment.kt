package de.challenge3.questapp.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.challenge3.questapp.databinding.FragmentHomeBinding
import de.challenge3.questapp.databinding.FragmentStatspageBinding
import de.challenge3.questapp.ui.home.HomeViewModel

class StatspageFragment : Fragment() {

    private var _binding: FragmentStatspageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentStatspageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textStatspage
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}