package org.vengeful.citymanager

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import org.vengeful.citymanager.adminPanel.configurations.configureAdminApi
import org.vengeful.citymanager.auth.JWTConfig
import org.vengeful.citymanager.configurations.configureDatabase
import org.vengeful.citymanager.configurations.configureSerialization
import org.vengeful.citymanager.personService.db.PersonRepository

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    println("All config keys: ${environment.config.keys().joinToString()}")
    val jwtConfig = JWTConfig(this.environment.config)

    install(ContentNegotiation) {
        json()
    }
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                val username = credential.payload.getClaim("username").asString()
                val rights = credential.payload.getClaim("rights").asString()

                if (username != null && rights != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token is invalid or expired"))
            }
        }
    }
    val repository = PersonRepository()
    configureSerialization(repository = repository)
    configureDatabase(repository = repository)
    configureAdminApi(repository = repository)
}