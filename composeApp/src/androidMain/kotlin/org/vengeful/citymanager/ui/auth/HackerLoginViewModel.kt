package org.vengeful.citymanager.ui.auth

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import kotlin.random.Random

class HackerLoginViewModel : BaseViewModel() {

    private val _code = MutableStateFlow<String>("")
    val code: StateFlow<String> = _code.asStateFlow()

    private val _userInput = MutableStateFlow<String>("")
    val userInput: StateFlow<String> = _userInput.asStateFlow()

    private val _attempts = MutableStateFlow(0)
    val attempts: StateFlow<Int> = _attempts.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _isHacked = MutableStateFlow(false)
    val isHacked: StateFlow<Boolean> = _isHacked.asStateFlow()

    private val _terminalOutput = MutableStateFlow<List<String>>(emptyList())
    val terminalOutput: StateFlow<List<String>> = _terminalOutput.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _hint = MutableStateFlow<String?>(null)
    val hint: StateFlow<String?> = _hint.asStateFlow()

    // Накопленная подсказка (массив из 6 символов: либо цифра, либо '_')
    private var accumulatedHint: CharArray = CharArray(CODE_LENGTH) { '_' }

    companion object {
        private const val CODE_LENGTH = 6
        const val MAX_ATTEMPTS = 3
        private const val LOCK_DURATION_MS = 2000L
        const val HINT_THRESHOLD = 3 // Показывать подсказки после 3 попыток
    }

    init {
        generateCode()
        initializeTerminal()
    }

    private fun generateCode() {
        _code.value = (100000..999999).random().toString()
    }

    private fun initializeTerminal() {
        val initialOutput = listOf(
            "> Подключение к Системе Безопасности СГК...",
            "> Подключение установлено.",
            "> Попытка обойти авторизацию...",
            "> Протокол безопасности обнаружен.",
            "> Взлом кода шифрования...",
            "> Код найден. Введите код доступа:"
        )
        _terminalOutput.value = initialOutput
    }

    fun updateInput(input: String) {
        if (_isLocked.value || _isHacked.value) return

        // Only allow digits
        val digitsOnly = input.filter { it.isDigit() }
        if (digitsOnly.length <= CODE_LENGTH) {
            _userInput.value = digitsOnly
        }
    }

    fun submitCode() {
        if (_isLocked.value || _isHacked.value) return
        if (_userInput.value.length != CODE_LENGTH) return

        viewModelScope.launch {
            _isTyping.value = true

            // Simulate processing
            delay(500)

            if (_userInput.value == _code.value) {
                // Success
                _isHacked.value = true
                addTerminalLine("> Система разблокирована.")
                addTerminalLine("> Доступ получен!")
                delay(800)
            } else {
                // Failure
                _attempts.value++
                addTerminalLine("> Доступ запрещён. Неверный код.")

                // Показываем подсказки после HINT_THRESHOLD попыток
                if (_attempts.value >= HINT_THRESHOLD) {
                    updateAccumulatedHint(_userInput.value, _code.value)
                    val hintText = accumulatedHint.joinToString(" ")
                    _hint.value = hintText
                    addTerminalLine("> Подобранный код: $hintText")
                }

                if (_attempts.value >= MAX_ATTEMPTS) {
                    addTerminalLine("> Слишком много неверных попыток. Система заблокирована.")
                    _isLocked.value = true
                    delay(LOCK_DURATION_MS)
                    resetAttempts()
                } else {
                    addTerminalLine("> Попыток осталось: ${MAX_ATTEMPTS - _attempts.value}")
                    addTerminalLine("> Введите код доступа:")
                }
            }

            _isTyping.value = false
        }
    }

    private fun resetAttempts() {
        viewModelScope.launch {
            _attempts.value = 0
            _isLocked.value = false
            _hint.value = null
            _userInput.value = ""
            addTerminalLine("> Блокировка снята. Новая сессия начата.")
            addTerminalLine("> Введите код доступа:")
        }
    }

    private fun addTerminalLine(line: String) {
        _terminalOutput.value = _terminalOutput.value + line
    }

    fun reset() {
        _userInput.value = ""
        _attempts.value = 0
        _isLocked.value = false
        _isHacked.value = false
        _hint.value = null
        accumulatedHint = CharArray(CODE_LENGTH) { '_' }
        generateCode() // При полном сбросе (перезаход в приложение) генерируем новый код
        initializeTerminal()
    }

    /**
     * Обновляет накопленную подсказку, объединяя с новой попыткой
     * Если в новой попытке угадана правильная цифра, она заменяет '_' в накопленной подсказке
     */
    private fun updateAccumulatedHint(userInput: String, correctCode: String) {
        if (userInput.length != correctCode.length) return

        userInput.forEachIndexed { index, char ->
            // Если в текущей попытке угадана правильная цифра на этой позиции
            if (char == correctCode[index]) {
                // Заменяем '_' на правильную цифру в накопленной подсказке
                accumulatedHint[index] = char
            }
            // Если уже есть правильная цифра в накопленной подсказке, оставляем её
            // (не перезаписываем, если уже угадано ранее)
        }
    }
}

