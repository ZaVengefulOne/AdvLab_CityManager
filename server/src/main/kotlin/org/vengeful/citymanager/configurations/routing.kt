package org.vengeful.citymanager.configurations

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.vengeful.citymanager.Greeting
import org.vengeful.citymanager.auth.JWTConfig
import org.vengeful.citymanager.models.users.LoginRequest
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.users.AuthResponse
import org.vengeful.citymanager.models.users.User
import org.vengeful.citymanager.personService.IPersonRepository
import java.util.Date
import io.ktor.server.config.*

fun Application.configureSerialization(repository: IPersonRepository) {

    routing { // Публичный роутинг
        get("/") {
            call.respondText("Vengeful Server: ${Greeting().greet()}")
        }
        get("/library") {
            call.respondText(text = "Здесь будет библиотека г. Лабтауна. В разработке.")
        }
    }

    routing { // Приватный роутинг

        // Аутентификация и регистрация
        route("/auth") {
            post("/login") {
                try {
                    val loginRequest = call.receive<LoginRequest>()
                    val user = authenticateUser(loginRequest.username, loginRequest.password)

                    if (user != null) {
                        val token = generateJwtToken(user)
                        call.respond(HttpStatusCode.OK, AuthResponse(token, user, user.rights))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
                }
            }

            post("/register") {
                // Ваша логика регистрации TODO
                call.respond(HttpStatusCode.OK, mapOf("message" to "Registration endpoint"))
            }
        }

        authenticate("auth-jwt") {
            route("/persons") {
                // Get all
                get {
                    val persons = repository.allPersons()
                    call.respond(HttpStatusCode.OK, persons)
                }

                // Get by Id
                get("/byId/{id}") {
                    val id = call.parameters["id"]?.toInt()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }
                    val person = repository.personById(id)
                    if (person == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(person)
                }

                // Get by Name
                get("/byName/{name}_{lastName}") {
                    val name = call.parameters["name"]
                    val lastName = call.parameters["lastName"]
                    if (name == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }
                    val person = repository.personByName(name, lastName)
                    if (person == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(person)
                }

                // Get by Rights
                get("/byRights") {
                    val rights = call.queryParameters["rights"]
                    if (rights.isNullOrBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest
                        )
                        return@get
                    }
                    try {
                        val rightsList = rights.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .map { Rights.valueOf(it) }

                        val persons = repository.personsByRights(rightsList)
                        if (persons.isEmpty()) {
                            call.respond(
                                HttpStatusCode.NotFound,
                                mapOf("error" to "No persons found with rights: $rights")
                            )
                        } else {
                            call.respond(persons)
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid rights value. Available: ${Rights.entries.joinToString { it.name }}")
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error: ${e.message}")
                        )
                    }
                }

                // Add person
                post {
                    try {
                        val person = call.receive<Person>()
                        repository.addPerson(person)
                        call.respond(HttpStatusCode.OK)
                    } catch (e: IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                    } catch (e: JsonConvertException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                    }
                }

                // Update person
                post("/update") {
                    try {
                        val person = call.receive<Person>()
                        repository.updatePerson(person)
                        call.respond(HttpStatusCode.OK)
                    } catch (e: IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                    } catch (e: JsonConvertException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                    }
                }

                // Delete person
                delete("/{personId}") {
                    val id = call.parameters["personId"]?.toInt()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@delete
                    }
                    if (repository.removePerson(id)) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }
    }
}

// Функция для генерации JWT токена
private fun Application.generateJwtToken(user: User): String {
    val jwtIssuer = environment.config.property("ktor.jwt.issuer").getString()
    val jwtAudience = environment.config.property("ktor.jwt.audience").getString()
    val jwtSecret = environment.config.property("ktor.jwt.secret").getString()
    val expirationTime = environment.config.property("ktor.jwt.expiration_time").getString().toLong()

    return JWT.create()
        .withAudience(jwtAudience)
        .withIssuer(jwtIssuer)
        .withClaim("userId", user.id)
        .withClaim("username", user.username)
        .withClaim("rights", user.rights)
        .withExpiresAt(Date(System.currentTimeMillis() + expirationTime * 1000))
        .sign(Algorithm.HMAC256(jwtSecret))
}