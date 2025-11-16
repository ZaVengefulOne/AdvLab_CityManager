package org.vengeful.citymanager.utilities

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalTime::class)
object DateFormatter {
    /**
     * Форматирует текущую дату в формат "dd/MM/yyyy" с заменой года на 1950
     */
    fun formatTo1950Date(): String {
        val instant = Clock.System.now()
        return formatInstantTo1950Date(instant)
    }

    /**
     * Форматирует Instant в формат "dd/MM/yyyy" с заменой года на 1950
     */
    @Suppress("NewApi")
    fun formatInstantTo1950Date(instant: kotlin.time.Instant): String {
        val epochMillis = instant.toEpochMilliseconds()
        val javaInstant = Instant.ofEpochMilli(epochMillis)
        val localDate = javaInstant.atZone(ZoneId.systemDefault()).toLocalDate()

        // Создаем новую дату с годом 1950, сохраняя день и месяц
        val date1950 = LocalDate.of(1950, localDate.monthValue, localDate.dayOfMonth)

        return date1950.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    /**
     * Форматирует epoch milliseconds в формат "dd/MM/yyyy" с заменой года на 1950
     */
    @Suppress("NewApi")
    fun formatEpochMillisTo1950Date(epochMillis: Long): String {
        val javaInstant = Instant.ofEpochMilli(epochMillis)
        val localDate = javaInstant.atZone(ZoneId.systemDefault()).toLocalDate()

        // Создаем новую дату с годом 1950, сохраняя день и месяц
        val date1950 = LocalDate.of(1950, localDate.monthValue, localDate.dayOfMonth)

        return date1950.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
}