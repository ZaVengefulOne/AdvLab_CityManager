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
            val javaInstant = java.time.Instant.ofEpochMilli(now)
            val localDate = javaInstant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            
            // Создаем дату с текущим днем и месяцем, но годом 1950
            val baseDate = java.time.LocalDate.of(1950, localDate.monthValue, localDate.dayOfMonth)
            val baseDateMillis = baseDate
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            
            // Вычисляем возраст от базовой даты (1950 год с текущим днем и месяцем)
            val years = (baseDateMillis - dateOfBirth) / (365L * 24 * 60 * 60 * 1000)
            return years.toInt().coerceAtLeast(0)
        }
}

