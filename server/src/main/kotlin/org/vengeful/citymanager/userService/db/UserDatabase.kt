package org.vengeful.citymanager.userService.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.vengeful.citymanager.models.users.User
import org.vengeful.citymanager.personService.db.PersonDao
import org.vengeful.citymanager.personService.db.Persons
import org.vengeful.citymanager.personService.db.RightDao
import org.vengeful.citymanager.personService.db.RightsTable

object Users : IntIdTable("users") {
    val username = varchar("username", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val isActive = bool("is_active").default(true)
    val createdAt = long("created_at")
    val personId = reference("person_id", Persons.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val severiteClicks = integer("severite_clicks").default(0)
}

object UserRights : Table("user_rights") {
    val userId = reference("user_id", Users.id, onDelete = ReferenceOption.CASCADE)
    val rightId = reference("right_id", RightsTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(userId, rightId)
}

class UserDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDao>(Users)

    var username by Users.username
    var passwordHash by Users.passwordHash
    var isActive by Users.isActive
    var createdAt by Users.createdAt
    var personId by Users.personId
    var severiteClicks by Users.severiteClicks

    // Связь многие-ко-многим с правами
    val rights by RightDao via UserRights

    //Такая же с Person
    var person by PersonDao optionalReferencedOn Users.personId

    fun toUser() = User(
        id = id.value,
        username = username,
        passwordHash = passwordHash,
        rights = rights.map { it.right }.toList(),
        isActive = isActive,
        createdAt = createdAt,
        severiteClicks = severiteClicks,
        personId = personId?.value
    )
}
