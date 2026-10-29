package com.example.kp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Устанавливаем тулбар
        setSupportActionBar(findViewById(R.id.toolbar))

        // Настройка edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Настройка FAB (плавающей кнопки)
        val fabHome = findViewById<FloatingActionButton>(R.id.fab_home)
        fabHome.setOnClickListener {
            loadFragment(HomeFragment())
            Toast.makeText(this, "Возврат на главную", Toast.LENGTH_SHORT).show()
        }

        // Загружаем HomeFragment при первом запуске
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    // Функция для загрузки фрагментов
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
            setReorderingAllowed(true)
            // Можно добавить в back stack если нужна навигация назад
            // addToBackStack(fragment::class.java.simpleName)
        }
    }

    // Создаем меню
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Обработка нажатий меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_home -> {
                loadFragment(HomeFragment())
                Toast.makeText(this, "Главная страница", Toast.LENGTH_SHORT).show()
                return true
            }


            R.id.action_logout -> {
                Toast.makeText(this, "Выход из системы", Toast.LENGTH_SHORT).show()
                AuthStore.clear()
                startActivity(
                    Intent(this, LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }



    private var doubleBackToExitPressedOnce = false
}