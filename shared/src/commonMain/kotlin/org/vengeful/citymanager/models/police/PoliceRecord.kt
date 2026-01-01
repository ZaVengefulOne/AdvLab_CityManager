package org.vengeful.citymanager.models.police

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class PoliceRecord @OptIn(ExperimentalTime::class) constructor(
    val id: Int = 0,
    val personId: Int,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: Long,
    val workplace: String,
    val photoUrl: String? = null,
    val fingerprintNumber: Int? = null,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
) {
    @OptIn(ExperimentalTime::class)
    val age: Int
        get() {
            // Используем текущую дату, но с годом 1950
            val now = Clock.System.now().toEpochMilliseconds()
            
            // Получаем день и месяц из текущей даты (используя простые вычисления)
            // Для этого используем временную метку и вычисляем день/месяц через деление
            // Но проще использовать фиксированную базовую дату 1950 года с текущим днем и месяцем
            // Для упрощения используем текущую дату как базовую, но вычисляем возраст относительно 1950 года
            
            // Вычисляем разницу в миллисекундах
            val diffMillis = now - dateOfBirth
            
            // Конвертируем в годы (приблизительно, используя среднее количество дней в году)
            // 365.25 дней в году * 24 часа * 60 минут * 60 секунд * 1000 миллисекунд
            val millisecondsPerYear = 365.25 * 24 * 60 * 60 * 1000
            val years = (diffMillis / millisecondsPerYear).toInt()
            
            return years.coerceAtLeast(0)
        }
}

