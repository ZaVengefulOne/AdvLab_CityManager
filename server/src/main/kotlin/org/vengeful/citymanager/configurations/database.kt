package org.vengeful.citymanager.configurations

import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.personService.db.PersonRepository
import org.vengeful.citymanager.personService.db.PersonRights
import org.vengeful.citymanager.personService.db.Persons
import org.vengeful.citymanager.personService.db.RightsTable

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