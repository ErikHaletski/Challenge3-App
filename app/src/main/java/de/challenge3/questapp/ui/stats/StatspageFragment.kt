package de.challenge3.questapp.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.challenge3.questapp.databinding.FragmentStatspageBinding
import de.challenge3.questapp.ui.home.HomeViewModel

class StatspageFragment : Fragment() {

    private var _binding: FragmentStatspageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatspageBinding.inflate(inflater, container, false)

        val StatspageViewModel = ViewModelProvider(this)[StatspageViewModel::class.java]
        StatspageViewModel.text.observe(viewLifecycleOwner) { value ->
            binding.textStatspage.text = value
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
