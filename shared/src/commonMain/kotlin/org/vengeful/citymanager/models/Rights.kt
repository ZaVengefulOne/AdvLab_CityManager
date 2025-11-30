package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable

@Serializable
enum class Rights {
    Police,
    Medic,
    Court,
    Administration,
    Bank,
    Any,
    Joker
}

fun Rights.getDisplayName(): String {
    return when (this) {
        Rights.Administration -> "Администрация"
        Rights.Court -> "Суд"
        Rights.Joker -> "Джокер"
        Rights.Police -> "Полиция"
        Rights.Medic -> "Больница"
        Rights.Bank -> "Банк"
        Rights.Any -> "Общий"
    }
}
