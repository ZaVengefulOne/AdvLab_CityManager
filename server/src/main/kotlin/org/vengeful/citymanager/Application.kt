package org.vengeful.citymanager

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.personService.FakePersonRepository
import org.vengeful.citymanager.personService.IPersonRepository
import java.util.Locale

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.configureSerialization(repository: IPersonRepository) {
    install(ContentNegotiation) {
        json()
    }
    routing {
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

            // Get by Rights
            get("/byRights/{rights}") {
                val rightsAsText = call.parameters["rights"]
                if (rightsAsText == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val rights = Rights.valueOf(rightsAsText.uppercase(Locale.getDefault()))
                    val persons = repository.personsByRights(rights)
                    if (persons.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(persons)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "")
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

fun Application.module() {
    val fakeRepository = FakePersonRepository()
    configureSerialization(repository = fakeRepository)
    routing {
        get("/") {
            call.respondText("Vengeful Server: ${Greeting().greet()}")
        }
    }
}