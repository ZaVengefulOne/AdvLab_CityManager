package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable

@Serializable
data class Person(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val rights: List<Rights>,
)
