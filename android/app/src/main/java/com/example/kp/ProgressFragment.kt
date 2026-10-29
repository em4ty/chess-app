package com.example.kp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.kp.databinding.FragmentProgressBinding
import kotlinx.coroutines.launch

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProgressFromServer()

        // Кнопка обновления
        binding.btnRefresh.setOnClickListener {
            loadProgressFromServer()
        }

        // Убираем кнопку синхронизации
        binding.btnSync.visibility = View.GONE
    }

    private fun loadProgressFromServer() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvSyncStatus.text = "Загрузка..."

                // Получаем ID пользователя из SharedPreferences
                val userId = AuthStore.userId()
                val token = AuthStore.token()

                if (userId == 0L || token == null) {
                    showLoginRequired()
                    return@launch
                }

                // Получаем статистику с сервера
                val response = RetrofitClient.instance.getStats(userId)

                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!

                    updateUI(
                        testCorrect = stats.correctAnswers,
                        practiceCompleted = stats.exercisesCompleted,
                        totalProgress = stats.totalProgressPercent
                    )

                    binding.tvSyncStatus.text = "✓ Данные загружены"
                } else if (response.code() == 401) {
                    showLoginRequired()
                } else {
                    showError()
                }
            } catch (e: Exception) {
                showNetworkError()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateUI(testCorrect: Int, practiceCompleted: Int, totalProgress: Int) {
        // Тест (Основы шахмат)
        binding.tvLessonsCompleted.text = "$testCorrect"
        binding.tvBasicsProgress.text = "$testCorrect/15"
        val testPercentage = if (15 > 0) (testCorrect * 100) / 15 else 0
        binding.progressBasics.progress = testPercentage
        binding.tvBasicsDetails.text = "Правильных ответов: $testCorrect"

        // Практика (Упражнения)
        binding.tvExercisesCompleted.text = "$practiceCompleted"
        binding.tvPracticeProgress.text = "$practiceCompleted/15"
        val practicePercentage = if (15 > 0) (practiceCompleted * 100) / 15 else 0
        binding.progressPractice.progress = practicePercentage
        binding.tvPracticeDetails.text = "Выполнено упражнений: $practiceCompleted"

        // Общий прогресс
        binding.tvTotalProgress.text = "$totalProgress%"

        // Детали

    }

    private fun showLoginRequired() {
        binding.tvSyncStatus.text = "⚠ Требуется вход"

        binding.tvLessonsCompleted.text = "0"
        binding.tvExercisesCompleted.text = "0"
        binding.tvTotalProgress.text = "0%"
        binding.progressBasics.progress = 0
        binding.progressPractice.progress = 0
    }

    private fun showError() {
        binding.tvSyncStatus.text = "⚠ Ошибка загрузки"
        Toast.makeText(requireContext(), "Не удалось загрузить статистику", Toast.LENGTH_SHORT).show()
    }

    private fun showNetworkError() {
        binding.tvSyncStatus.text = "⚠ Нет подключения"
        Toast.makeText(requireContext(), "Проверьте подключение к интернету", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // При возвращении на экран обновляем данные
        loadProgressFromServer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}