package org.vengeful.citymanager.auth

import io.ktor.server.config.ApplicationConfig

class EmergencyShutdownConfig(config: ApplicationConfig) {
    val password: String = config.property("emergency_shutdown.password").getString()
}
