package org.vengeful.citymanager.models.police

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
enum class CaseStatus {
    OPEN,              // Открыто
    SENT_TO_COURT,    // Передано в суд
    VERDICT_PRONOUNCED, // Вынесен приговор
    CLOSED             // Закрыто
}

@Serializable
data class Case @OptIn(ExperimentalTime::class) constructor(
    val id: Int = 0,
    val complainantPersonId: Int? = null,  // ID заявителя из БД (если выбран из списка)
    val complainantName: String,            // Имя заявителя
    val investigatorPersonId: Int,         // ID следователя (автоматически из текущего пользователя)
    val suspectPersonId: Int? = null,       // ID подозреваемого из БД (если выбран из списка)
    val suspectName: String,                 // Имя подозреваемого
    val statementText: String,               // Текст заявления
    val violationArticle: String,           // Статья правонарушения
    val status: CaseStatus,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
)

