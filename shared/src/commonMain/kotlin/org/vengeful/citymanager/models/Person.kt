package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable
import org.vengeful.citymanager.models.police.Case

@Serializable
data class Person(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val registrationPlace: String = "",
    val health: String = "здоров",
    val rights: List<Rights>,
    val balance: Double = 0.0,
    val casesAsSuspect: List<Case>? = null,  // Дела, где житель является подозреваемым
)
