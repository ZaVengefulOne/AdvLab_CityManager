package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable

@Serializable
enum class Enterprise {
    POLICE,    // Полиция
    MEDIC,     // Больница
    BANK,      // Банк
    COURT,      // Суд
    ADMINISTRATION, // Администрация
    NIIS
}

fun Enterprise.getDisplayName(): String {
    return when (this) {
        Enterprise.POLICE -> "Полиция"
        Enterprise.MEDIC -> "Больница"
        Enterprise.BANK -> "Банк"
        Enterprise.COURT -> "Суд"
        Enterprise.ADMINISTRATION -> "Администрация"
        Enterprise.NIIS -> "НИИС"
    }
}
