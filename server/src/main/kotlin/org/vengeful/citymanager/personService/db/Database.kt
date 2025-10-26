package org.vengeful.citymanager.personService.db

import io.ktor.server.application.Application
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.personService.db.Persons.id

object Persons : IntIdTable("persons") {
    val firstName = varchar("first_name", 255)
    val lastName = varchar("last_name", 255)
}

object RightsTable : IntIdTable("rights") {
    val name = varchar("name", 50).uniqueIndex()
}

object PersonRights : Table("person_rights") {
    val personId = reference("person_id", Persons.id, onDelete = ReferenceOption.CASCADE)
    val rightId  = reference("right_id", RightsTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(personId, rightId)
}

class PersonDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PersonDao>(Persons)

    var firstName by Persons.firstName
    var lastName by Persons.lastName

    // Связь многие-ко-многим с правами
    val rights by RightDao via PersonRights

    fun toPerson() = Person(
        id = id.value,
        firstName = firstName,
        lastName = lastName,
        rights = rights.map { it.right }.toList()
    )
}

class RightDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RightDao>(RightsTable) {
        fun getOrCreate(right: Rights): RightDao {
            return find { RightsTable.name eq right.name }.firstOrNull()
                ?: new {
                    this.name = right.name
                }
        }

        // Вспомогательный метод для получения всех прав
        fun getAllRights(): List<RightDao> = all().toList()
    }

    var name by RightsTable.name

    val right: Rights
        get() = Rights.valueOf(name)

    // Связь с персонажами - теперь будет работать
    val persons by PersonDao via PersonRights
}