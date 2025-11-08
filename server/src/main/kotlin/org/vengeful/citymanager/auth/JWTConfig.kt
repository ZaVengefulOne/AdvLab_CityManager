package org.vengeful.citymanager.auth

import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.config.ApplicationConfig

class JWTConfig(config: ApplicationConfig) {
    val audience = config.property("jwt.audience").getString()
    val issuer = config.property("jwt.issuer").getString()
    val secret = config.property("jwt.secret").getString()
    val realm = config.property("jwt.realm").getString()
    val expirationTime = config.property("jwt.expiration_time").getString().toLong()
}