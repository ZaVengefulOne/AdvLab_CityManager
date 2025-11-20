package org.vengeful.citymanager.models

import kotlinx.serialization.Serializable

@Serializable
enum class Rights {
    Police,
    Medic,
    Court,
    Administration,
    Club,
    Bank,
    Any,
    Joker
}