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
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.personService.IPersonRepository
import org.vengeful.citymanager.personService.db.PersonDao
import org.vengeful.citymanager.personService.db.PersonRepository
import org.vengeful.citymanager.personService.db.PersonRights
import org.vengeful.citymanager.personService.db.Persons
import org.vengeful.citymanager.personService.db.RightsTable
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

fun Application.configureDatabase(repository: PersonRepository) {
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/vengeful_db",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "password"
    )

    transaction {
//        SchemaUtils.drop(PersonRights, RightsTable, Persons) // На случай миграции если будет впадлу её писать
        SchemaUtils.create(Persons, RightsTable, PersonRights)
        repository.initializeRights()
    }
}

fun Application.module() {
    val repository = PersonRepository()
    configureSerialization(repository = repository)
    configureDatabase(repository)
    routing {
        get("/") {
            call.respondText("Vengeful Server: ${Greeting().greet()}")
        }
    }
}