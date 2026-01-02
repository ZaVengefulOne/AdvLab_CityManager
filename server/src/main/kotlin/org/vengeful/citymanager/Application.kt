package org.vengeful.citymanager

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.vengeful.citymanager.adminPanel.configurations.configureAdminApi
import org.vengeful.citymanager.auth.EmergencyShutdownConfig
import org.vengeful.citymanager.auth.JWTConfig
import org.vengeful.citymanager.auth.SessionLockManager
import org.vengeful.citymanager.bankService.db.BankRepository
import org.vengeful.citymanager.configurations.configureDatabase
import org.vengeful.citymanager.configurations.configureRouting
import org.vengeful.citymanager.libraryService.db.LibraryRepository
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.newsService.NewsRepository
import org.vengeful.citymanager.personService.db.PersonRepository
import org.vengeful.citymanager.severiteService.db.SeveriteRepository
import org.vengeful.citymanager.stockSerivce.db.StockRepository
import org.vengeful.citymanager.userService.db.UserRepository

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val jwtConfig = JWTConfig(this.environment.config)
    val emergencyShutdownConfig = EmergencyShutdownConfig(this.environment.config)
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            isLenient = true
            encodeDefaults = true
        })
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
        allowNonSimpleContentTypes = true
        allowCredentials = true
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
                val userIdClaim = credential.payload.getClaim("userId")
                val rightsClaim = credential.payload.getClaim("rights")
                val rightsList = if (rightsClaim.isNull) {
                    emptyList<String>()
                } else {
                    rightsClaim.asArray(String::class.java)?.toList() ?: emptyList()
                }
                val rights = try {
                    rightsList.map { Rights.valueOf(it) }
                } catch (e: IllegalArgumentException) {
                    null
                }

                if (username != null && rights != null && rights.isNotEmpty()) {
                    val userId = userIdClaim.asInt()
                    // Проверяем блокировку сессий
                    val token = credential.payload.getClaim("token").asString()
                    if (SessionLockManager.isSessionBlocked(userId, token)) {
                        null // Блокируем аутентификацию
                    } else {
                        JWTPrincipal(credential.payload)
                    }
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token is invalid or expired"))
            }
        }
    }

    val personRepository = PersonRepository()
    val userRepository = UserRepository()
    val bankRepository = BankRepository(personRepository)
    val stockRepository = StockRepository()
    val libraryRepository = LibraryRepository()
    val newsRepository = NewsRepository()
    val severiteRepository = SeveriteRepository()

    configureRouting(
        personRepository = personRepository,
        userRepository = userRepository,
        bankRepository = bankRepository,
        emergencyShutdownConfig = emergencyShutdownConfig,
        libraryRepository = libraryRepository,
        newsRepository = newsRepository,
        severiteRepository = severiteRepository
        )
    configureDatabase(repository = personRepository)
    configureAdminApi(
        repository = personRepository,
        bankRepository = bankRepository,
        userRepository = userRepository,
        stockRepository = stockRepository,
        newsRepository = newsRepository,
    )
}
