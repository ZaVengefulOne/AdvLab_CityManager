package org.vengeful.citymanager.models.medicine

import kotlinx.serialization.Serializable

@Serializable
data class Medicine(
    val id: Int = 0,
    val name: String,
    val price: Double
)
