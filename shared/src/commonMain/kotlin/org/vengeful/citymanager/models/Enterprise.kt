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

fun Enterprise.toRights(): Rights {
    return when (this) {
        Enterprise.POLICE -> Rights.Police
        Enterprise.MEDIC -> Rights.Medic
        Enterprise.BANK -> Rights.Bank
        Enterprise.COURT -> Rights.Court
        Enterprise.NIIS -> Rights.Uni
        Enterprise.ADMINISTRATION -> Rights.Administration
    }
}
