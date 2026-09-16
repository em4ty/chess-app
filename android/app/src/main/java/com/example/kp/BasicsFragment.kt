package com.example.kp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

class BasicsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_basics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btn_pieces_details).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PiecesFragment())
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<View>(R.id.btn_rules_quiz).setOnClickListener {
            resetTestResults()
            val intent = Intent(requireContext(), QuizActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<View>(R.id.btn_board_details).setOnClickListener {
            Toast.makeText(requireContext(), "Раздел в разработке", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetTestResults() {
        val userPrefs = requireContext().getSharedPreferences("chess_prefs", Context.MODE_PRIVATE)
        val userId = userPrefs.getLong("user_id", 0)

        val quizPrefs = if (userId > 0) {
            requireContext().getSharedPreferences("quiz_results_$userId", Context.MODE_PRIVATE)
        } else {
            requireContext().getSharedPreferences("quiz_results_guest", Context.MODE_PRIVATE)
        }

        // Сбрасываем ВСЕ результаты теста
        with(quizPrefs.edit()) {
            putInt("last_correct", 0)
            putInt("last_total", 0)
            putInt("last_score", 0)
            putLong("last_date", 0)
            apply()
        }

        // Также очищаем в общем прогрессе
        val progressPrefs = requireContext().getSharedPreferences("chess_progress", Context.MODE_PRIVATE)
        with(progressPrefs.edit()) {
            putInt("quiz_last_correct", 0)
            putInt("quiz_last_total", 0)
            putInt("quiz_last_score", 0)
            putLong("quiz_last_date", 0)
            apply()
        }
    }
}