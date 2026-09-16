package com.example.kp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработка кликов по кнопкам
        view.findViewById<View>(R.id.btn_basics).setOnClickListener {
            // Переход к основам шахмат
            navigateToFragment(BasicsFragment())
        }

        view.findViewById<View>(R.id.btn_practice).setOnClickListener {
            // Переход к практике
            navigateToFragment(PracticeFragment())
        }

        view.findViewById<View>(R.id.btn_progress).setOnClickListener {
            // Переход к прогрессу
            navigateToFragment(ProgressFragment())
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
            addToBackStack(null) // Добавляем в back stack для возможности возврата
        }
    }
}