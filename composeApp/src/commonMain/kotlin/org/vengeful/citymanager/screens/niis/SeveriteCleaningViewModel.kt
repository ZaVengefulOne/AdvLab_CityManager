package org.vengeful.citymanager.screens.niis
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.base.BaseViewModel
import org.vengeful.citymanager.data.severite.ISeveriteInteractor
import org.vengeful.citymanager.models.severite.SeveritePurity
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

enum class CleaningMode {
    SIMPLE,      // 3 простых элемента (префикс "100")
    MEDIUM,      // 5 элементов, один сложный (префикс "110")
    FULL         // 7 элементов, все доступные (префикс "200")
}

class SeveritCleaningViewModel(
    private val severiteInteractor: ISeveriteInteractor
) : BaseViewModel() {

    private val _cleaningMode = MutableStateFlow<CleaningMode>(CleaningMode.FULL)
    val cleaningMode: StateFlow<CleaningMode> = _cleaningMode.asStateFlow()

    private val _activeIndices = MutableStateFlow<List<Int>>(emptyList())
    val activeIndices: StateFlow<List<Int>> = _activeIndices.asStateFlow()

    private val _targetSequence = MutableStateFlow<List<Int>>(emptyList())
    val targetSequence: StateFlow<List<Int>> = _targetSequence.asStateFlow()

    private val _currentValues = MutableStateFlow<List<Int>>(emptyList())
    val currentValues: StateFlow<List<Int>> = _currentValues.asStateFlow()

    private val _guessedIndices = MutableStateFlow<Set<Int>>(emptySet())
    val guessedIndices: StateFlow<Set<Int>> = _guessedIndices.asStateFlow()

    private val _showSuccessDialog = MutableStateFlow(false)
    val showSuccessDialog: StateFlow<Boolean> = _showSuccessDialog.asStateFlow()

    // Состояние перегрева (0.0f - 1.0f)
    private val _overheatProgress = MutableStateFlow(0f)
    val overheatProgress: StateFlow<Float> = _overheatProgress.asStateFlow()

    // Оставшееся время в секундах
    private val _remainingTime = MutableStateFlow(0L)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()

    // Показывать ли диалог ошибки
    private val _showErrorDialog = MutableStateFlow(false)
    val showErrorDialog: StateFlow<Boolean> = _showErrorDialog.asStateFlow()

    // Время последнего изменения значения
    @OptIn(ExperimentalTime::class)
    private var lastChangeTime: Instant? = null

    // Время начала очистки
    @OptIn(ExperimentalTime::class)
    private var startTime: Instant? = null

    // Job для таймера
    private var timerJob: Job? = null

    // Job для постепенного уменьшения перегрева
    private var overheatCoolingJob: Job? = null

    // Константы
    companion object {
        private const val FAST_CHANGE_THRESHOLD_MS = 50000L // Порог быстрого изменения (мс)
        private const val OVERHEAT_INCREASE = 0.05f // Увеличение перегрева при быстром изменении
        private const val OVERHEAT_COOLING_RATE = 0.05f // Скорость охлаждения за 100мс
        private const val OVERHEAT_COOLING_INTERVAL_MS = 50L // Интервал охлаждения
        private const val MAX_OVERHEAT = 1.0f // Максимальный перегрев

        // Время на очистку в зависимости от сложности (в секундах)
        private const val TIME_SIMPLE = 120L // 2 минуты
        private const val TIME_MEDIUM = 180L // 3 минуты
        private const val TIME_FULL = 240L // 4 минуты
    }

    fun initializeMode(sampleNumber: String) {
        val mode = when {
            sampleNumber.startsWith("100") -> CleaningMode.SIMPLE
            sampleNumber.startsWith("110") -> CleaningMode.MEDIUM
            sampleNumber.startsWith("200") -> CleaningMode.FULL
            else -> CleaningMode.FULL // По умолчанию полный режим
        }

        _cleaningMode.value = mode

        // Определяем активные индексы в зависимости от режима
        _activeIndices.value = when (mode) {
            CleaningMode.SIMPLE -> listOf(0, 3, 5) // 3 простых: Slider, Dial, Wheel
            CleaningMode.MEDIUM -> listOf(0, 1, 3, 5, 6) // 5 элементов, один сложный (DialLock)
            CleaningMode.FULL -> listOf(0, 1, 2, 3, 4, 5, 6) // Все 7 элементов
        }

        generateSequence()
    }

    @OptIn(ExperimentalTime::class)
    fun generateSequence() {
        val activeIndices = _activeIndices.value
        val sequence = MutableList(7) { 0 }

        activeIndices.forEach { index ->
            sequence[index] = when (index) {
                1 -> Random.nextInt(0, 1000) // Комбинационный замок: 0-999
                2 -> Random.nextInt(1, 8) * 125 // Головоломка с сегментами: кратно 125, исключая 0 и 1000
                4 -> Random.nextInt(0, 26) * 4 // Головоломка с переключателями: кратно 4 (0, 4, 8, ..., 100)
                else -> Random.nextInt(1, 100) // Остальные: 1-99
            }
        }

        _targetSequence.value = sequence
        _currentValues.value = List(7) { 0 }
        _guessedIndices.value = emptySet()
        _showSuccessDialog.value = false
        _overheatProgress.value = 0f
        _showErrorDialog.value = false
        lastChangeTime = null

        // Останавливаем предыдущие корутины
        timerJob?.cancel()
        overheatCoolingJob?.cancel()

        // Запускаем таймер и охлаждение
        startTimer()
        startOverheatCooling()
    }

    @OptIn(ExperimentalTime::class)
    fun updateValue(index: Int, value: Int) {
        if (!_activeIndices.value.contains(index)) return

        // Проверяем на быстрые изменения
        val currentTime = Clock.System.now()
        lastChangeTime?.let { lastTime ->
            val timeSinceLastChange = (currentTime - lastTime).inWholeMilliseconds
            if (timeSinceLastChange < FAST_CHANGE_THRESHOLD_MS) {
                // Слишком быстрое изменение - увеличиваем перегрев
                increaseOverheat()
            }
        }
        lastChangeTime = currentTime

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

    private fun increaseOverheat() {
        val newOverheat = (_overheatProgress.value + OVERHEAT_INCREASE).coerceAtMost(MAX_OVERHEAT)
        _overheatProgress.value = newOverheat

        // Если перегрев достиг максимума - показываем ошибку
        if (newOverheat >= MAX_OVERHEAT) {
            triggerError()
        }
    }

    private fun startOverheatCooling() {
        overheatCoolingJob?.cancel()
        overheatCoolingJob = viewModelScope.launch {
            while (true) {
                delay(OVERHEAT_COOLING_INTERVAL_MS)
                if (_overheatProgress.value > 0f) {
                    val newOverheat = (_overheatProgress.value - OVERHEAT_COOLING_RATE).coerceAtLeast(0f)
                    _overheatProgress.value = newOverheat
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun startTimer() {
        timerJob?.cancel()
        startTime = Clock.System.now()

        val timeLimit = when (_cleaningMode.value) {
            CleaningMode.SIMPLE -> TIME_SIMPLE
            CleaningMode.MEDIUM -> TIME_MEDIUM
            CleaningMode.FULL -> TIME_FULL
        }

        // Устанавливаем начальное значение сразу
        _remainingTime.value = timeLimit

        timerJob = viewModelScope.launch {
            var remaining = timeLimit
            while (remaining > 0 && !_showErrorDialog.value && !_showSuccessDialog.value) {
                delay(1000)
                remaining--
                _remainingTime.value = remaining
            }

            // Если время истекло и не было успеха - показываем ошибку
            if (remaining == 0L && !_showSuccessDialog.value) {
                triggerError()
            }
        }
    }

    private fun triggerError() {
        timerJob?.cancel()
        overheatCoolingJob?.cancel()
        _showErrorDialog.value = true
    }

    fun dismissErrorDialog() {
        _showErrorDialog.value = false
    }

    private fun checkGuess(index: Int) {
        val target = _targetSequence.value[index]
        val current = _currentValues.value[index]

        // Для комбинационного замка сравниваем полное значение, а не остаток
        if (target == current && !_guessedIndices.value.contains(index)) {
            val newGuessed = _guessedIndices.value.toMutableSet()
            newGuessed.add(index)
            _guessedIndices.value = newGuessed

            // Проверяем, угаданы ли все активные элементы
            if (newGuessed.size == _activeIndices.value.size) {
                // Останавливаем таймер и охлаждение при успешном завершении
                timerJob?.cancel()
                overheatCoolingJob?.cancel()
                // Сохраняем северит в зависимости от режима очистки
                saveSeverite()
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
        timerJob?.cancel()
        overheatCoolingJob?.cancel()
        generateSequence()
    }

    private fun saveSeverite() {
        viewModelScope.launch {
            try {
                val purity = when (_cleaningMode.value) {
                    CleaningMode.SIMPLE -> SeveritePurity.CONTAMINATED
                    CleaningMode.MEDIUM -> SeveritePurity.NORMAL
                    CleaningMode.FULL -> SeveritePurity.CRYSTAL_CLEAR
                }
                severiteInteractor.addSeverite(purity)
            } catch (e: Exception) {
                println("Failed to save severite: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}

data class DialLockHints(
    val sumOfDigits: Int,           // Сумма всех цифр
    val productOfFirstTwo: Int,      // Произведение сотен и десятков
    val maxMinDifference: Int        // Разность между максимальной и минимальной цифрой
)
