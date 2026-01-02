package org.vengeful.citymanager.models.court

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class Hearing @OptIn(ExperimentalTime::class) constructor(
    val id: Int = 0,
    val caseId: Int,                    // Ссылка на дело из полиции
    val plaintiffPersonId: Int? = null, // ID истца (null если "Город")
    val plaintiffName: String,          // Имя истца или "Город"
    val protocol: String = "",          // Текст протокола слушания
    val verdict: String = "",           // Вердикт суда
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
)



