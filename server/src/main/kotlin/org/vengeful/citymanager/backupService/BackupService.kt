package org.vengeful.citymanager.backupService

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.bankService.IBankRepository
import org.vengeful.citymanager.bankService.db.BankAccountDao
import org.vengeful.citymanager.bankService.db.BankAccounts
import org.vengeful.citymanager.models.backup.GameBackup
import org.vengeful.citymanager.models.backup.GameBackupBankAccount
import org.vengeful.citymanager.models.backup.GameBackupEntry
import org.vengeful.citymanager.models.backup.GameBackupPerson
import org.vengeful.citymanager.models.backup.GameBackupUser
import org.vengeful.citymanager.models.backup.MasterBackup
import org.vengeful.citymanager.models.backup.MasterBackupBankAccount
import org.vengeful.citymanager.models.backup.MasterBackupPerson
import org.vengeful.citymanager.models.backup.MasterBackupPersonRight
import org.vengeful.citymanager.models.backup.MasterBackupRight
import org.vengeful.citymanager.models.backup.MasterBackupUser
import org.vengeful.citymanager.models.backup.MasterBackupUserRight
import org.vengeful.citymanager.personService.IPersonRepository
import org.vengeful.citymanager.personService.db.PersonDao
import org.vengeful.citymanager.personService.db.PersonRights
import org.vengeful.citymanager.personService.db.Persons
import org.vengeful.citymanager.personService.db.RightDao
import org.vengeful.citymanager.personService.db.RightsTable
import org.vengeful.citymanager.userService.IUserRepository
import org.vengeful.citymanager.userService.db.UserDao
import org.vengeful.citymanager.userService.db.UserRights
import org.vengeful.citymanager.userService.db.Users
import java.time.Instant

class BackupService(
    private val personRepository: IPersonRepository,
    private val userRepository: IUserRepository,
    private val bankRepository: IBankRepository
) {

    fun createGameBackup(): GameBackup {
        val persons = personRepository.allPersons()
        val users = userRepository.getAllUsers()
        val bankAccounts = bankRepository.getAllBankAccounts()

        val personMap = persons.associateBy { it.id }
        val userMap = users.associateBy { it.id }
        val bankAccountMap = bankAccounts.associateBy { it.personId }

        val entries = mutableListOf<GameBackupEntry>()

        // Создаем записи для всех persons
        persons.forEach { person ->
            val user = users.find { it.id == person.id } // Предполагаем, что user.id может совпадать с person.id
            val bankAccount = bankAccounts.find { it.personId == person.id }

            entries.add(
                GameBackupEntry(
                    person = GameBackupPerson(
                        id = person.id,
                        firstName = person.firstName,
                        lastName = person.lastName,
                        health = person.health,
                        balance = person.balance,
                        rights = person.rights.map { it.name }
                    ),
                    user = user?.let {
                        GameBackupUser(
                            id = it.id,
                            username = it.username,
                            clicks = it.severiteClicks,
                            rights = it.rights.map { it.name }
                        )
                    },
                    bankAccount = bankAccount?.let {
                        GameBackupBankAccount(
                            personId = it.personId,
                            enterpriseName = it.enterpriseName,
                            creditAmount = it.creditAmount
                        )
                    }
                )
            )
        }

        // Добавляем users без связанных persons
        users.forEach { user ->
            if (user.id !in personMap.keys) {
                val bankAccount = bankAccounts.find { it.personId == user.id }
                entries.add(
                    GameBackupEntry(
                        person = null,
                        user = GameBackupUser(
                            id = user.id,
                            username = user.username,
                            clicks = user.severiteClicks,
                            rights = user.rights.map { it.name }
                        ),
                        bankAccount = bankAccount?.let {
                            GameBackupBankAccount(
                                personId = it.personId,
                                enterpriseName = it.enterpriseName,
                                creditAmount = it.creditAmount
                            )
                        }
                    )
                )
            }
        }

        return GameBackup(
            entries = entries,
            createdAt = Instant.now().toEpochMilli()
        )
    }

    fun createGameBackupHtml(): String {
        val backup = createGameBackup()

        val html = StringBuilder()
        html.append(
            """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Игровой бэкап - ${java.time.Instant.ofEpochMilli(backup.createdAt)}</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:nth-child(even) { background-color: #f2f2f2; }
        .header { background-color: #333; color: white; padding: 10px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Игровой бэкап базы данных</h1>
        <p>Создан: ${java.time.Instant.ofEpochMilli(backup.createdAt)}</p>
    </div>
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Имя</th>
                <th>Фамилия</th>
                <th>Здоровье</th>
                <th>Права</th>
                <th>Баланс</th>
                <th>Username</th>
                <th>Клики</th>
                <th>Права User</th>
                <th>Кредит</th>
                <th>Предприятие</th>
            </tr>
        </thead>
        <tbody>
""".trimIndent()
        )

        backup.entries.forEach { entry ->
            html.append("<tr>")
            html.append("<td>${entry.person?.id ?: "-"}</td>")
            html.append("<td>${entry.person?.firstName ?: "-"}</td>")
            html.append("<td>${entry.person?.lastName ?: "-"}</td>")
            html.append("<td>${entry.person?.health ?: "-"}</td>")
            html.append("<td>${entry.person?.rights?.joinToString(", ") ?: "-"}</td>")
            html.append("<td>${entry.person?.balance ?: "-"}</td>")
            html.append("<td>${entry.user?.username ?: "-"}</td>")
            html.append("<td>${entry.user?.clicks ?: "-"}</td>")
            html.append("<td>${entry.user?.rights?.joinToString(", ") ?: "-"}</td>")
            html.append("<td>${entry.bankAccount?.creditAmount ?: "-"}</td>")
            html.append("<td>${entry.bankAccount?.enterpriseName ?: "-"}</td>")
            html.append("</tr>")
        }

        html.append(
            """
        </tbody>
    </table>
</body>
</html>
""".trimIndent()
        )

        return html.toString()
    }

    fun createGameBackupMarkdown(): String {
        val backup = createGameBackup()

        val md = StringBuilder()
        md.append("# Игровой бэкап базы данных\n\n")
        md.append("**Создан:** ${java.time.Instant.ofEpochMilli(backup.createdAt)}\n\n")
        md.append("| ID | Имя | Фамилия | Права Person | Username | Клики | Права User | Кредит | Предприятие |\n")
        md.append("|----|-----|---------|--------------|----------|-------|------------|--------|-------------|\n")

        backup.entries.forEach { entry ->
            md.append("| ")
            md.append("${entry.person?.id ?: "-"} | ")
            md.append("${entry.person?.firstName ?: "-"} | ")
            md.append("${entry.person?.lastName ?: "-"} | ")
            md.append("${entry.person?.rights?.joinToString(", ") ?: "-"} | ")
            md.append("${entry.user?.username ?: "-"} | ")
            md.append("${entry.user?.clicks ?: "-"} | ")
            md.append("${entry.user?.rights?.joinToString(", ") ?: "-"} | ")
            md.append("${entry.bankAccount?.creditAmount ?: "-"} | ")
            md.append("${entry.bankAccount?.enterpriseName ?: "-"} |\n")
        }

        return md.toString()
    }

    fun createMasterBackup(): MasterBackup = transaction {
        val persons = PersonDao.all().map { dao ->
            MasterBackupPerson(
                id = dao.id.value,
                firstName = dao.firstName,
                lastName = dao.lastName,
                registrationPlace = dao.registrationPlace,
                health = dao.health // ДОБАВИТЬ
            )
        }

        val rights = RightDao.all().map { dao ->
            MasterBackupRight(
                id = dao.id.value,
                name = dao.name
            )
        }

        val personRights = PersonRights.selectAll().map { row ->
            MasterBackupPersonRight(
                personId = row[PersonRights.personId].value,
                rightId = row[PersonRights.rightId].value
            )
        }

        val users = UserDao.all().map { dao ->
            MasterBackupUser(
                id = dao.id.value,
                username = dao.username,
                passwordHash = dao.passwordHash,
                isActive = dao.isActive,
                createdAt = dao.createdAt,
                personId = dao.personId?.value,
                severiteClicks = dao.severiteClicks
            )
        }

        val userRights = UserRights.selectAll().map { row ->
            MasterBackupUserRight(
                userId = row[UserRights.userId].value,
                rightId = row[UserRights.rightId].value
            )
        }

        val bankAccounts = BankAccountDao.all().map { dao ->
            MasterBackupBankAccount(
                id = dao.id.value,
                personId = dao.personId?.value,
                enterpriseName = dao.enterpriseName,
                creditAmount = dao.creditAmount
            )
        }

        MasterBackup(
            persons = persons,
            rights = rights,
            personRights = personRights,
            users = users,
            userRights = userRights,
            bankAccounts = bankAccounts,
            createdAt = Instant.now().toEpochMilli()
        )
    }

    fun restoreFromMasterBackup(backup: MasterBackup) = transaction {
        // Очистка в правильном порядке (с учетом foreign keys)
        UserRights.deleteWhere { UserRights.userId.isNotNull() }
        Users.deleteWhere { Users.id.isNotNull() }
        PersonRights.deleteWhere { PersonRights.personId.isNotNull() }
        BankAccounts.deleteWhere { BankAccounts.id.isNotNull() }
        PersonDao.all().forEach { it.delete() }
        RightDao.all().forEach { it.delete() }

        // Загрузка в правильном порядке
        // 1. Rights
        val rightsMap = backup.rights.associate { right ->
            val dao = RightDao.new {
                this.name = right.name
            }
            right.id to dao.id
        }

        // 2. Persons
        val personsMap = backup.persons.associate { person ->
            val dao = PersonDao.new(person.id) {
                this.firstName = person.firstName
                this.lastName = person.lastName
            }
            person.id to dao.id
        }

        // 3. PersonRights
        backup.personRights.forEach { pr ->
            val personId = personsMap[pr.personId] ?: return@forEach
            val rightId = rightsMap[pr.rightId] ?: return@forEach
            PersonRights.insert {
                it[PersonRights.personId] = personId
                it[PersonRights.rightId] = rightId
            }
        }

        // 4. BankAccounts
        backup.bankAccounts.forEach { ba ->
            BankAccountDao.new(ba.id) {
                this.personId = ba.personId?.let { personsMap[it] }
                this.enterpriseName = ba.enterpriseName
                this.creditAmount = ba.creditAmount
            }
        }

        // 5. Users
        val usersMap = backup.users.associate { user ->
            val dao = UserDao.new(user.id) {
                this.username = user.username
                this.passwordHash = user.passwordHash
                this.isActive = user.isActive
                this.createdAt = user.createdAt
                this.personId = user.personId?.let { personsMap[it] }
                this.severiteClicks = user.severiteClicks
            }
            user.id to dao.id
        }

        // 6. UserRights
        backup.userRights.forEach { ur ->
            val userId = usersMap[ur.userId] ?: return@forEach
            val rightId = rightsMap[ur.rightId] ?: return@forEach
            UserRights.insert {
                it[UserRights.userId] = userId
                it[UserRights.rightId] = rightId
            }
        }
    }
}
