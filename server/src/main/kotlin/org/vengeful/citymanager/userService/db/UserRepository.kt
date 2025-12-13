package org.vengeful.citymanager.userService.db

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.users.User
import org.vengeful.citymanager.personService.db.PersonDao
import org.vengeful.citymanager.personService.db.RightDao
import org.vengeful.citymanager.personService.db.RightsTable
import org.vengeful.citymanager.userService.db.UserDao
import org.vengeful.citymanager.userService.db.UserRights
import org.vengeful.citymanager.userService.db.Users
import org.vengeful.citymanager.userService.IUserRepository
import org.vengeful.citymanager.userService.PasswordHasher
import org.vengeful.citymanager.userService.db.UserRights.userId
import org.vengeful.citymanager.userService.db.UserRights.rightId
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class UserRepository : IUserRepository {

    override fun findByUsername(username: String): User? = transaction {
        UserDao.find { Users.username eq username }
            .firstOrNull()
            ?.toUser()
    }

    override fun findById(id: Int): User? = transaction {
        UserDao.findById(id)?.toUser()
    }

    override fun createUser(user: User, password: String): User = transaction {
        val hashedPassword = PasswordHasher.hashPassword(password)
        val userDao = UserDao.new {
            username = user.username
            this.passwordHash = hashedPassword
            isActive = user.isActive
            createdAt = user.createdAt
        }

        // Добавляем права
        user.rights.forEach { right ->
            val rightDao = RightDao.getOrCreate(right)
            UserRights.insert {
                it[userId] = userDao.id
                it[rightId] = rightDao.id
            }
        }

        userDao.toUser()
    }

    override fun updateUser(user: User): User? = transaction {
        UserDao.findById(user.id)?.apply {
            username = user.username
            isActive = user.isActive

            // Обновляем права
            val currentRightsSet = rights.map { it.right }.toSet()
            val newRightsSet = user.rights.toSet()

            // Права для удаления
            val rightsToRemove = currentRightsSet - newRightsSet
            rightsToRemove.forEach { rightToRemove ->
                val rightDao = RightDao.find { RightsTable.name eq rightToRemove.name }.firstOrNull()
                rightDao?.let {
                    UserRights.deleteWhere {
                        (UserRights.userId eq id) and (UserRights.rightId eq rightDao.id)
                    }
                }
            }

            // Права для добавления
            val rightsToAdd = newRightsSet - currentRightsSet
            rightsToAdd.forEach { rightToAdd ->
                val rightDao = RightDao.getOrCreate(rightToAdd)
                UserRights.insert {
                    it[userId] = id
                    it[rightId] = rightDao.id
                }
            }
        }?.toUser()
    }

    override fun deleteUser(id: Int): Boolean = transaction {
        UserDao.findById(id)?.apply {
            // Отвязываем Person перед удалением
            personId = null
            // Удаляем права
            UserRights.deleteWhere { UserRights.userId eq id }
            // Удаляем пользователя
            delete()
        } != null
    }

    override fun authenticateUser(username: String, password: String): User? = transaction {
        val userDao = UserDao.find {
            (Users.username eq username) and (Users.isActive eq true)
        }.firstOrNull()

        userDao?.takeIf {
            PasswordHasher.verifyPassword(password, it.passwordHash)
        }?.toUser()
    }

    override fun getAllUsers(): List<User> = transaction {
        UserDao.all().map { it.toUser() }
    }

    override fun userExists(username: String): Boolean = transaction {
        UserDao.find { Users.username eq username }.count() > 0
    }

    override fun updateUser(
        user: User,
        password: String?,
        personId: Int?
    ): User? = transaction {
        UserDao.findById(user.id)?.apply {
            username = user.username
            isActive = user.isActive

            // Обновляем пароль, если передан
            password?.let {
                passwordHash = PasswordHasher.hashPassword(it)
            }

            // Обновляем связь с Person
            this.personId = personId?.let {
                PersonDao.findById(it)?.id
            } ?: null // TODO: Переделать

            // Обновляем права (существующая логика)
            val currentRightsSet = rights.map { it.right }.toSet()
            val newRightsSet = user.rights.toSet()

            // Права для удаления
            val rightsToRemove = currentRightsSet - newRightsSet
            rightsToRemove.forEach { rightToRemove ->
                val rightDao = RightDao.find { RightsTable.name eq rightToRemove.name }.firstOrNull()
                rightDao?.let {
                    UserRights.deleteWhere {
                        (UserRights.userId eq id) and (UserRights.rightId eq rightDao.id)
                    }
                }
            }

            // Права для добавления
            val rightsToAdd = newRightsSet - currentRightsSet
            rightsToAdd.forEach { rightToAdd ->
                val rightDao = RightDao.getOrCreate(rightToAdd)
                UserRights.insert {
                    it[userId] = id
                    it[rightId] = rightDao.id
                }
            }
        }?.toUser()
    }



    @OptIn(ExperimentalTime::class)
    override fun registerUser(
        username: String,
        password: String,
        personId: Int?,
        rights: List<Rights>
    ): User = transaction {
        if (userExists(username)) {
            throw IllegalArgumentException("User $username already exists")
        }

        val personDao = personId?.let { id ->
            PersonDao.findById(id) ?: throw IllegalArgumentException("Person $id not found")
        }

        personDao?.let { person ->
            val exitingUser = UserDao.find { Users.personId eq person.id.value }.firstOrNull()
            if (exitingUser != null) {
                throw IllegalArgumentException("Person with id ${person.id.value} is already linked to user ${exitingUser.username}")
            }
        }

        // Определяем права: если есть Person, используем его права, иначе используем переданные
        val userRights = personDao?.rights?.map { rightDao -> rightDao.right }?.toList() ?: rights

        val hashedPassword = PasswordHasher.hashPassword(password)
        val userDao = UserDao.new {
            this.username = username
            this.passwordHash = hashedPassword
            this.isActive = true
            this.createdAt = Clock.System.now().toEpochMilliseconds()
            this.personId = personDao?.id
        }

        // Добавляем права
        userRights.forEach { right ->
            val rightDao = RightDao.getOrCreate(right)
            UserRights.insert {
                it[userId] = userDao.id
                it[rightId] = rightDao.id
            }
        }

        userDao.toUser()

    }

    override fun updateUserClicks(userId: Int, severiteClicks: Int): Boolean = transaction {
        UserDao.findById(userId)?.apply {
            this.severiteClicks = severiteClicks
        } != null
    }

    override fun purchaseSaveProgressUpgrade(userId: Int): Boolean = transaction {
        UserDao.findById(userId)?.apply {
            this.hasSaveProgressUpgrade = true
        } != null
    }

    override fun purchaseClickMultiplierUpgrade(userId: Int): Boolean = transaction {
        UserDao.findById(userId)?.apply {
            this.clickMultiplier += 1
        } != null
    }

    override fun getCount(): Int = transaction {
        UserDao.all().count().toInt()
    }
}
