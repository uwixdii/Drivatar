package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация Firebase Authentication и Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        // Слушатель изменений в поле пароля
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePassword(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Обработчик нажатия на кнопку регистрации
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val name = binding.etName.text.toString().trim()

            // Проверка на пустоту полей
            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Проверка email
            if (!validateEmail(email)) {
                Toast.makeText(this, "Введите правильный email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Блокировка кнопки, чтобы предотвратить повторный клик
            binding.btnRegister.isEnabled = false

            // Регистрация через Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: ""
                        val user = mapOf(
                            "name" to name,
                            "email" to email
                        )

                        // Сохранение данных в Realtime Database
                        database.child(userId).setValue(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    // Добавляем дополнительную обработку ошибки записи
                                    Toast.makeText(this, "Ошибка записи в базу данных: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        // Обработка ошибок
                        task.exception?.let { exception ->
                            when (exception) {
                                is FirebaseAuthUserCollisionException -> {
                                    Toast.makeText(this, "Пользователь с таким email уже существует", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    Toast.makeText(this, "Ошибка регистрации: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    // Разблокируем кнопку после завершения операции
                    binding.btnRegister.isEnabled = true
                }
        }
    }

    // Проверка email
    private fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Проверка пароля
    private fun validatePassword(password: String) {
        val minLength = password.length >= 6
        val hasDigit = password.any { it.isDigit() }
        val hasUppercase = password.any { it.isUpperCase() }

        // Определяем текущую тему
        val isDarkTheme = isDarkThemeActive()
        val titleColor = if (isDarkTheme) 0xFFFFFFFF.toInt() else 0xFF000000.toInt() // Белый или чёрный
        val validColor = 0xFF00FF00.toInt() // Зеленый цвет для выполненного условия
        val invalidColor = 0xFFFF0000.toInt() // Красный цвет для невыполненного условия

        // Разделяем заголовок и критерии
        val title = "Пароль должен соответствовать следующим требованиям:"
        val criteria = """
        ${if (minLength) "✅" else "❌"} Минимум 6 символов
        ${if (hasDigit) "✅" else "❌"} Как минимум одна цифра
        ${if (hasUppercase) "✅" else "❌"} Как минимум одна заглавная буква
    """.trimIndent()

        // Создаём объединённый текст
        val combinedText = "$title\n\n$criteria"

        // Создаем SpannableString
        val spannable = SpannableString(combinedText)

        // Устанавливаем цвет заголовка
        spannable.setSpan(
            ForegroundColorSpan(titleColor), // Цвет заголовка
            0,
            title.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Получаем начальные индексы критериев
        val criteriaStartIndices = listOf(
            combinedText.indexOf("Минимум 6 символов"),
            combinedText.indexOf("Как минимум одна цифра"),
            combinedText.indexOf("Как минимум одна заглавная буква")
        )

        // Применяем цвета к каждому критерию
        val criteriaColors = listOf(
            if (minLength) validColor else invalidColor,
            if (hasDigit) validColor else invalidColor,
            if (hasUppercase) validColor else invalidColor
        )

        criteriaStartIndices.forEachIndexed { index, startIndex ->
            val endIndex = startIndex + when (index) {
                0 -> "Минимум 6 символов".length
                1 -> "Как минимум одна цифра".length
                2 -> "Как минимум одна заглавная буква".length
                else -> 0
            }

            spannable.setSpan(
                ForegroundColorSpan(criteriaColors[index]),
                startIndex - 2, // Учитываем символы "✅" или "❌"
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Устанавливаем текст в TextView
        binding.tvPasswordCriteria.text = spannable

        // Активируем кнопку только если все критерии выполнены
        binding.btnRegister.isEnabled = minLength && hasDigit && hasUppercase
    }

    // Функция для определения текущей темы
    private fun isDarkThemeActive(): Boolean {
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}
