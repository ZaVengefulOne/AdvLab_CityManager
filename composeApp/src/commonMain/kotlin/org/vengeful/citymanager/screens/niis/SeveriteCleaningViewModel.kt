package org.vengeful.citymanager.screens.niis
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vengeful.citymanager.base.BaseViewModel
import kotlin.random.Random

class SeveritCleaningViewModel : BaseViewModel() {

    private val _targetSequence = MutableStateFlow<List<Int>>(emptyList())
    val targetSequence: StateFlow<List<Int>> = _targetSequence.asStateFlow()

    private val _currentValues = MutableStateFlow(List(7) { 0 })
    val currentValues: StateFlow<List<Int>> = _currentValues.asStateFlow()

    private val _guessedIndices = MutableStateFlow<Set<Int>>(emptySet())
    val guessedIndices: StateFlow<Set<Int>> = _guessedIndices.asStateFlow()

    private val _showSuccessDialog = MutableStateFlow(false)
    val showSuccessDialog: StateFlow<Boolean> = _showSuccessDialog.asStateFlow()

    init {
        generateSequence()
    }

    fun generateSequence() {
        _targetSequence.value = List(7) { index ->
            when (index) {
                1 -> Random.nextInt(0, 1000) // Комбинационный замок: 0-999
                2 -> Random.nextInt(1, 8) * 125 // Головоломка с сегментами: кратно 125, исключая 0 и 1000 (125, 250, 375, 500, 625, 750, 875)
                4 -> Random.nextInt(0, 26) * 4 // Головоломка с переключателями: кратно 4 (0, 4, 8, ..., 100)
                else -> Random.nextInt(0, 101) // Остальные: 0-100
            }
        }
        _currentValues.value = List(7) { 0 }
        _guessedIndices.value = emptySet()
        _showSuccessDialog.value = false
    }

    fun updateValue(index: Int, value: Int) {
        val newValues = _currentValues.value.toMutableList()
        // Для комбинационного замка (индекс 1) разрешаем значения 0-999, для головоломки с сегментами (индекс 2) 0-1000, для остальных 0-100
        newValues[index] = when (index) {
            1 -> value.coerceIn(0, 999)
            2 -> value.coerceIn(0, 1000)
            else -> value.coerceIn(0, 100)
        }
        _currentValues.value = newValues

        checkGuess(index)
    }

    private fun checkGuess(index: Int) {
        val target = _targetSequence.value[index]
        val current = _currentValues.value[index]

        // Для комбинационного замка сравниваем полное значение, а не остаток
        if (target == current && !_guessedIndices.value.contains(index)) {
            val newGuessed = _guessedIndices.value.toMutableSet()
            newGuessed.add(index)
            _guessedIndices.value = newGuessed

            if (newGuessed.size == 7) {
                _showSuccessDialog.value = true
            }
        } else if (target != current && _guessedIndices.value.contains(index)) {
            val newGuessed = _guessedIndices.value.toMutableSet()
            newGuessed.remove(index)
            _guessedIndices.value = newGuessed
        }
    }

    // Функция для получения подсказок для комбинационного замка
    fun getDialLockHints(): DialLockHints? {
        val target = _targetSequence.value.getOrNull(1) ?: return null
        val hundreds = (target / 100) % 10
        val tens = (target / 10) % 10
        val ones = target % 10

        return DialLockHints(
            sumOfDigits = hundreds + tens + ones,
            productOfFirstTwo = hundreds * tens,
            maxMinDifference = maxOf(hundreds, tens, ones) - minOf(hundreds, tens, ones)
        )
    }


    fun dismissSuccessDialog() {
        _showSuccessDialog.value = false
    }

    fun reset() {
        generateSequence()
    }
}

data class DialLockHints(
    val sumOfDigits: Int,           // Сумма всех цифр
    val productOfFirstTwo: Int,      // Произведение сотен и десятков
    val maxMinDifference: Int        // Разность между максимальной и минимальной цифрой
)
