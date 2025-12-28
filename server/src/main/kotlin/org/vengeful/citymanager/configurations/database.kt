package org.vengeful.citymanager.configurations

import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.bankService.db.BankAccounts
import org.vengeful.citymanager.libraryService.db.Articles
import org.vengeful.citymanager.medicService.db.MedicalRecords
import org.vengeful.citymanager.medicService.db.MedicineOrders
import org.vengeful.citymanager.medicService.db.Medicines
import org.vengeful.citymanager.newsService.db.NewsTable
import org.vengeful.citymanager.personService.db.PersonRepository
import org.vengeful.citymanager.personService.db.PersonRights
import org.vengeful.citymanager.personService.db.Persons
import org.vengeful.citymanager.personService.db.RightsTable
import org.vengeful.citymanager.severiteService.db.Severites
import org.vengeful.citymanager.stockSerivce.db.Stocks
import org.vengeful.citymanager.userService.db.UserRights
import org.vengeful.citymanager.userService.db.Users

fun Application.configureDatabase(repository: PersonRepository) {
    val dbUrl = environment.config.propertyOrNull("database.url")?.getString()
        ?: throw IllegalStateException("DATABASE_URL environment variable is required")
    val dbUser = environment.config.propertyOrNull("database.user")?.getString()
        ?: throw IllegalStateException("DATABASE_USER environment variable is required")
    val dbPassword = environment.config.propertyOrNull("database.password")?.getString()
        ?: throw IllegalStateException("DATABASE_PASSWORD environment variable is required")
    val dbDriver = environment.config.propertyOrNull("database.driver")?.getString()
        ?: "org.postgresql.Driver"

    Database.connect(
        url = dbUrl,
        driver = dbDriver,
        user = dbUser,
        password = dbPassword
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            Persons,
            RightsTable,
            PersonRights,
            Users,
            UserRights,
            BankAccounts,
            MedicalRecords,
            Medicines,
            MedicineOrders,
            Stocks,
            Articles,
            NewsTable,
            Severites
        )
        repository.initializeRights()
    }
}
