package com.example.kp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.kp.databinding.FragmentPracticeBinding

class PracticeFragment : Fragment() {

    private var _binding: FragmentPracticeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPracticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Кнопка сброса доски
        binding.btnResetBoard.setOnClickListener {
            binding.chessBoard.resetBoard()
        }

        // Защита короля (5 упражнений)
        binding.btnDefense.setOnClickListener {
            startActivity(Intent(requireContext(), DefenseExerciseActivity::class.java))
        }

        // Атака на короля (5 упражнений)
        binding.btnAttack.setOnClickListener {
            startActivity(Intent(requireContext(), AttackExerciseActivity::class.java))
        }

        // Матовые практики (5 упражнений)
        binding.btnCheckmate.setOnClickListener {
            startActivity(Intent(requireContext(), CheckmateExerciseActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}