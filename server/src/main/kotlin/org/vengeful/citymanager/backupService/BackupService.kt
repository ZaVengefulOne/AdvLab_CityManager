package org.vengeful.citymanager.backupService

import io.ktor.server.util.toGMTDate
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
import org.vengeful.citymanager.models.backup.LimitedMasterBackup
import org.vengeful.citymanager.models.backup.MasterBackup
import org.vengeful.citymanager.models.backup.MasterBackupArticle
import org.vengeful.citymanager.models.backup.MasterBackupBankAccount
import org.vengeful.citymanager.models.backup.MasterBackupCase
import org.vengeful.citymanager.models.backup.MasterBackupHearing
import org.vengeful.citymanager.models.backup.MasterBackupMedicalRecord
import org.vengeful.citymanager.models.backup.MasterBackupMedicine
import org.vengeful.citymanager.models.backup.MasterBackupMedicineOrder
import org.vengeful.citymanager.models.backup.MasterBackupNews
import org.vengeful.citymanager.models.backup.MasterBackupPerson
import org.vengeful.citymanager.models.backup.MasterBackupPersonRight
import org.vengeful.citymanager.models.backup.MasterBackupPoliceRecord
import org.vengeful.citymanager.models.backup.MasterBackupRight
import org.vengeful.citymanager.models.backup.MasterBackupSeverite
import org.vengeful.citymanager.models.backup.MasterBackupStock
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
import org.vengeful.citymanager.models.users.User
import org.vengeful.citymanager.medicService.db.MedicalRecordDao
import org.vengeful.citymanager.medicService.db.MedicalRecords
import org.vengeful.citymanager.medicService.db.MedicineDao
import org.vengeful.citymanager.medicService.db.Medicines
import org.vengeful.citymanager.medicService.db.MedicineOrderDao
import org.vengeful.citymanager.medicService.db.MedicineOrders
import org.vengeful.citymanager.policeService.db.PoliceRecordDao
import org.vengeful.citymanager.policeService.db.PoliceRecords
import org.vengeful.citymanager.policeService.db.CaseDao
import org.vengeful.citymanager.policeService.db.Cases
import org.vengeful.citymanager.courtService.db.HearingDao
import org.vengeful.citymanager.courtService.db.Hearings
import org.vengeful.citymanager.stockSerivce.db.StockDao
import org.vengeful.citymanager.stockSerivce.db.Stocks
import org.vengeful.citymanager.libraryService.db.ArticleDao
import org.vengeful.citymanager.libraryService.db.Articles
import org.vengeful.citymanager.newsService.db.NewsDao
import org.vengeful.citymanager.newsService.db.NewsTable
import org.vengeful.citymanager.severiteService.db.SeveriteDao
import org.vengeful.citymanager.severiteService.db.Severites
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class BackupService(
    private val personRepository: IPersonRepository,
    private val userRepository: IUserRepository,
    private val bankRepository: IBankRepository
) {

    private fun formatBackupDate(epochMillis: Long): String {
        val instant = Instant.ofEpochMilli(epochMillis)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        
        // Создаем новую дату с годом 1950, сохраняя день, месяц, час и минуту
        val date1950 = LocalDateTime.of(
            1950,
            localDateTime.monthValue,
            localDateTime.dayOfMonth,
            localDateTime.hour,
            localDateTime.minute
        )
        
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale("ru"))
        return date1950.format(formatter)
    }

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

    fun createGameBackupHtml(): String = transaction {
        val backup = createGameBackup()

        // Получаем дополнительные данные для статистики
        val users = UserDao.all().map { it.toUser() }
        val userMap = users.associateBy { it.id }

        val medicalRecordsCount = MedicalRecordDao.all().count()
        val policeRecordsCount = PoliceRecordDao.all().count()
        val casesCount = CaseDao.all().count()
        val hearingsCount = HearingDao.all().count()
        val stocksCount = StockDao.all().count()
        val newsCount = NewsDao.all().count()
        val severitesCount = SeveriteDao.all().count()
        val medicinesCount = MedicineDao.all().count()
        val articlesCount = ArticleDao.all().count()

        val formattedDate = formatBackupDate(backup.createdAt)

        val html = StringBuilder()
        html.append(
            """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Игровой бэкап - $formattedDate</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:nth-child(even) { background-color: #f2f2f2; }
        .header { background-color: #333; color: white; padding: 10px; }
        .stats { background-color: #f9f9f9; padding: 15px; margin-top: 20px; border: 1px solid #ddd; }
        .stats h2 { margin-top: 0; color: #333; }
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 10px; }
        .stat-item { padding: 10px; background-color: white; border: 1px solid #ddd; border-radius: 4px; }
        .stat-label { font-weight: bold; color: #666; }
        .stat-value { font-size: 1.2em; color: #4CAF50; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Игровой бэкап базы данных</h1>
        <p>Создан: $formattedDate</p>
    </div>

    <div class="stats">
        <h2>Статистика базы данных</h2>
        <div class="stats-grid">
            <div class="stat-item">
                <div class="stat-label">Персоны</div>
                <div class="stat-value">${backup.entries.count { it.person != null }}</div>
            </div>
            <div class="stat-item">
                <div class="stat-label">Пользователи</div>
                <div class="stat-value">${backup.entries.count { it.user != null }}</div>
            </div>
            <div class="stat-item">
                <div class="stat-label">Банковские счета</div>
                <div class="stat-value">${backup.entries.count { it.bankAccount != null }}</div>
            </div>
            <div class="stat-item">
                <div class="stat-label">Медицинские записи</div>
                <div class="stat-value">$medicalRecordsCount</div>
            </div>
            <div class="stat-item">
                <div class="stat-label">Полицейские записи</div>
                <div class="stat-value">$policeRecordsCount</div>
            </div>
            <div class="stat-item">
                <div class="stat-label">Дела</div>
                <div class="stat-value">$casesCount</div>
            </div>
            <div class="stat-item">
                <div class="stat-label">Слушанья</div>
                <div class="stat-value">$hearingsCount</div>
            </div>
            <div class="stat-item">
                <div class="stat-label">Акции</div>
                <div class="stat-value">$stocksCount</div>
            </div>
            <div class="stat-item">
                <div class="stat-label">Новости</div>
                <div class="stat-value">$newsCount</div>
            </div>
            <div class="stat-item">
                <div class="stat-label">Северит</div>
                <div class="stat-value">$severitesCount</div>
            </div>
            <div class="stat-item">
                <div class="stat-label">Лекарства</div>
                <div class="stat-value">$medicinesCount</div>
            </div>
            <div class="stat-item">
                <div class="stat-label">Статьи в библиотеке</div>
                <div class="stat-value">$articlesCount</div>
            </div>
        </div>
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
                <th>Клики (СеверитКоин)</th>
                <th>Множитель (СеверитКоин)</th>
                <th>Сохранение прогресса (СеверитКоин)</th>
                <th>Права User</th>
                <th>Кредит</th>
                <th>Предприятие</th>
            </tr>
        </thead>
        <tbody>
""".trimIndent()
        )

        backup.entries.forEach { entry ->
            val user = entry.user?.let { userMap[it.id] }
            html.append("<tr>")
            html.append("<td>${entry.person?.id ?: "-"}</td>")
            html.append("<td>${entry.person?.firstName ?: "-"}</td>")
            html.append("<td>${entry.person?.lastName ?: "-"}</td>")
            html.append("<td>${entry.person?.health ?: "-"}</td>")
            html.append("<td>${entry.person?.rights?.joinToString(", ") ?: "-"}</td>")
            html.append("<td>${entry.person?.balance ?: "-"}</td>")
            html.append("<td>${entry.user?.username ?: "-"}</td>")
            html.append("<td>${entry.user?.clicks ?: "-"}</td>")
            html.append("<td>${user?.clickMultiplier ?: "-"}</td>")
            html.append("<td>${if (user?.hasSaveProgressUpgrade == true) "Да" else "Нет"}</td>")
            html.append("<td>${entry.user?.rights?.joinToString(", ") ?: "-"}</td>")
            html.append("<td>${entry.bankAccount?.creditAmount ?: "-"}</td>")
            html.append("<td>${entry.bankAccount?.enterpriseName ?: "Нет"}</td>")
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

        html.toString()
    }

    fun createGameBackupMarkdown(): String {
        val backup = createGameBackup()

        val md = StringBuilder()
        md.append("# Игровой бэкап базы данных\n\n")
        md.append("**Создан:** ${Instant.ofEpochMilli(backup.createdAt).toGMTDate()}\n\n")
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
                health = dao.health,
                balance = dao.balance
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
                severiteClicks = dao.severiteClicks,
                hasSaveProgressUpgrade = dao.hasSaveProgressUpgrade,
                clickMultiplier = dao.clickMultiplier
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

        val medicalRecords = MedicalRecordDao.all().map { dao ->
            MasterBackupMedicalRecord(
                id = dao.id.value,
                personId = dao.personId.value,
                firstName = dao.firstName,
                lastName = dao.lastName,
                gender = dao.gender,
                dateOfBirth = dao.dateOfBirth,
                workplace = dao.workplace,
                doctor = dao.doctor,
                prescribedTreatment = dao.prescribedTreatment,
                createdAt = dao.createdAt
            )
        }

        val medicines = MedicineDao.all().map { dao ->
            MasterBackupMedicine(
                id = dao.id.value,
                name = dao.name,
                price = dao.price
            )
        }

        val medicineOrders = MedicineOrderDao.all().map { dao ->
            MasterBackupMedicineOrder(
                id = dao.id.value,
                medicineId = dao.medicineId,
                medicineName = dao.medicineName,
                quantity = dao.quantity,
                totalPrice = dao.totalPrice,
                accountId = dao.accountId,
                orderedByPersonId = dao.orderedByPersonId,
                createdAt = dao.createdAt
            )
        }

        val policeRecords = PoliceRecordDao.all().map { dao ->
            MasterBackupPoliceRecord(
                id = dao.id.value,
                personId = dao.personId.value,
                firstName = dao.firstName,
                lastName = dao.lastName,
                dateOfBirth = dao.dateOfBirth,
                workplace = dao.workplace,
                photoUrl = dao.photoUrl,
                fingerprintNumber = dao.fingerprintNumber,
                createdAt = dao.createdAt
            )
        }

        val cases = CaseDao.all().map { dao ->
            MasterBackupCase(
                id = dao.id.value,
                complainantPersonId = dao.complainantPersonId?.value,
                complainantName = dao.complainantName,
                investigatorPersonId = dao.investigatorPersonId.value,
                suspectPersonId = dao.suspectPersonId?.value,
                suspectName = dao.suspectName,
                statementText = dao.statementText,
                violationArticle = dao.violationArticle,
                status = dao.status,
                photoCompositeUrl = dao.photoCompositeUrl,
                createdAt = dao.createdAt
            )
        }

        val hearings = HearingDao.all().map { dao ->
            MasterBackupHearing(
                id = dao.id.value,
                caseId = dao.caseId.value,
                plaintiffPersonId = dao.plaintiffPersonId?.value,
                plaintiffName = dao.plaintiffName,
                protocol = dao.protocol,
                verdict = dao.verdict,
                createdAt = dao.createdAt,
                updatedAt = dao.updatedAt
            )
        }

        val stocks = StockDao.all().map { dao ->
            MasterBackupStock(
                id = dao.id.value,
                name = dao.name,
                averagePrice = dao.averagePrice
            )
        }

        val articles = ArticleDao.all().map { dao ->
            MasterBackupArticle(
                id = dao.id.value,
                title = dao.title,
                content = dao.content
            )
        }

        val news = NewsDao.all().map { dao ->
            MasterBackupNews(
                id = dao.id.value,
                title = dao.title,
                imageUrl = dao.imageUrl,
                sourceType = dao.sourceType
            )
        }

        val severites = SeveriteDao.all().map { dao ->
            MasterBackupSeverite(
                id = dao.id.value,
                purity = dao.purity,
                createdAt = dao.createdAt
            )
        }

        MasterBackup(
            persons = persons,
            rights = rights,
            personRights = personRights,
            users = users,
            userRights = userRights,
            bankAccounts = bankAccounts,
            medicalRecords = medicalRecords,
            medicines = medicines,
            medicineOrders = medicineOrders,
            policeRecords = policeRecords,
            cases = cases,
            hearings = hearings,
            stocks = stocks,
            articles = articles,
            news = news,
            severites = severites,
            createdAt = Instant.now().toEpochMilli()
        )
    }

    fun restoreFromMasterBackup(backup: MasterBackup) = transaction {
        // Очистка в правильном порядке (с учетом foreign keys)
        // 1. Таблицы, зависящие от других таблиц
        Hearings.deleteWhere { Hearings.id.isNotNull() }
        Cases.deleteWhere { Cases.id.isNotNull() }
        PoliceRecords.deleteWhere { PoliceRecords.id.isNotNull() }
        MedicalRecords.deleteWhere { MedicalRecords.id.isNotNull() }
        MedicineOrders.deleteWhere { MedicineOrders.id.isNotNull() }
        UserRights.deleteWhere { UserRights.userId.isNotNull() }
        Users.deleteWhere { Users.id.isNotNull() }
        PersonRights.deleteWhere { PersonRights.personId.isNotNull() }
        BankAccounts.deleteWhere { BankAccounts.id.isNotNull() }
        // 2. Независимые таблицы
        PersonDao.all().forEach { it.delete() }
        RightDao.all().forEach { it.delete() }
        MedicineDao.all().forEach { it.delete() }
        StockDao.all().forEach { it.delete() }
        ArticleDao.all().forEach { it.delete() }
        NewsDao.all().forEach { it.delete() }
        SeveriteDao.all().forEach { it.delete() }

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
                this.registrationPlace = person.registrationPlace
                this.health = person.health
                this.balance = person.balance
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

        // 4. Независимые таблицы
        backup.medicines.forEach { medicine ->
            MedicineDao.new(medicine.id) {
                this.name = medicine.name
                this.price = medicine.price
            }
        }

        backup.stocks.forEach { stock ->
            StockDao.new(stock.id) {
                this.name = stock.name
                this.averagePrice = stock.averagePrice
            }
        }

        backup.articles.forEach { article ->
            ArticleDao.new(article.id) {
                this.title = article.title
                this.content = article.content
            }
        }

        backup.news.forEach { newsItem ->
            NewsDao.new(newsItem.id) {
                this.title = newsItem.title
                this.imageUrl = newsItem.imageUrl
                this.sourceType = newsItem.sourceType
            }
        }

        backup.severites.forEach { severite ->
            SeveriteDao.new(severite.id) {
                this.purity = severite.purity
                this.createdAt = severite.createdAt
            }
        }

        // 5. Таблицы, зависящие от Persons
        backup.medicalRecords.forEach { record ->
            val personId = personsMap[record.personId] ?: return@forEach
            MedicalRecordDao.new(record.id) {
                this.personId = personId
                this.firstName = record.firstName
                this.lastName = record.lastName
                this.gender = record.gender
                this.dateOfBirth = record.dateOfBirth
                this.workplace = record.workplace
                this.doctor = record.doctor
                this.prescribedTreatment = record.prescribedTreatment
                this.createdAt = record.createdAt
            }
        }

        backup.policeRecords.forEach { record ->
            val personId = personsMap[record.personId] ?: return@forEach
            PoliceRecordDao.new(record.id) {
                this.personId = personId
                this.firstName = record.firstName
                this.lastName = record.lastName
                this.dateOfBirth = record.dateOfBirth
                this.workplace = record.workplace
                this.photoUrl = record.photoUrl
                this.fingerprintNumber = record.fingerprintNumber
                this.createdAt = record.createdAt
            }
        }

        // 6. Cases (зависит от Persons)
        val casesMap = backup.cases.mapNotNull { case ->
            val investigatorPersonId = personsMap[case.investigatorPersonId] ?: return@mapNotNull null
            val dao = CaseDao.new(case.id) {
                this.complainantPersonId = case.complainantPersonId?.let { personsMap[it] }
                this.complainantName = case.complainantName
                this.investigatorPersonId = investigatorPersonId
                this.suspectPersonId = case.suspectPersonId?.let { personsMap[it] }
                this.suspectName = case.suspectName
                this.statementText = case.statementText
                this.violationArticle = case.violationArticle
                this.status = case.status
                this.photoCompositeUrl = case.photoCompositeUrl
                this.createdAt = case.createdAt
            }
            case.id to dao.id
        }.toMap()

        // 7. Hearings (зависит от Cases)
        backup.hearings.forEach { hearing ->
            val caseId = casesMap[hearing.caseId] ?: return@forEach
            HearingDao.new(hearing.id) {
                this.caseId = caseId
                this.plaintiffPersonId = hearing.plaintiffPersonId?.let { personsMap[it] }
                this.plaintiffName = hearing.plaintiffName
                this.protocol = hearing.protocol
                this.verdict = hearing.verdict
                this.createdAt = hearing.createdAt
                this.updatedAt = hearing.updatedAt
            }
        }

        // 8. BankAccounts
        backup.bankAccounts.forEach { ba ->
            BankAccountDao.new(ba.id) {
                this.personId = ba.personId?.let { personsMap[it] }
                this.enterpriseName = ba.enterpriseName
                this.creditAmount = ba.creditAmount
            }
        }

        // 9. Users
        val usersMap = backup.users.associate { user ->
            val dao = UserDao.new(user.id) {
                this.username = user.username
                this.passwordHash = user.passwordHash
                this.isActive = user.isActive
                this.createdAt = user.createdAt
                this.personId = user.personId?.let { personsMap[it] }
                this.severiteClicks = user.severiteClicks
                this.hasSaveProgressUpgrade = user.hasSaveProgressUpgrade
                this.clickMultiplier = user.clickMultiplier
            }
            user.id to dao.id
        }

        // 10. UserRights
        backup.userRights.forEach { ur ->
            val userId = usersMap[ur.userId] ?: return@forEach
            val rightId = rightsMap[ur.rightId] ?: return@forEach
            UserRights.insert {
                it[UserRights.userId] = userId
                it[UserRights.rightId] = rightId
            }
        }

        // 11. MedicineOrders (может ссылаться на accountId, но не через foreign key)
        backup.medicineOrders.forEach { order ->
            MedicineOrderDao.new(order.id) {
                this.medicineId = order.medicineId
                this.medicineName = order.medicineName
                this.quantity = order.quantity
                this.totalPrice = order.totalPrice
                this.accountId = order.accountId
                this.orderedByPersonId = order.orderedByPersonId
                this.createdAt = order.createdAt
            }
        }
    }

    fun createLimitedMasterBackup(): LimitedMasterBackup = transaction {
        val persons = PersonDao.all().map { dao ->
            MasterBackupPerson(
                id = dao.id.value,
                firstName = dao.firstName,
                lastName = dao.lastName,
                registrationPlace = dao.registrationPlace,
                health = dao.health,
                balance = dao.balance
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
                severiteClicks = dao.severiteClicks,
                hasSaveProgressUpgrade = dao.hasSaveProgressUpgrade,
                clickMultiplier = dao.clickMultiplier
            )
        }

        val userRights = UserRights.selectAll().map { row ->
            MasterBackupUserRight(
                userId = row[UserRights.userId].value,
                rightId = row[UserRights.rightId].value
            )
        }

        LimitedMasterBackup(
            persons = persons,
            users = users,
            rights = rights,
            personRights = personRights,
            userRights = userRights,
            createdAt = Instant.now().toEpochMilli()
        )
    }

    fun restoreFromLimitedMasterBackup(backup: LimitedMasterBackup) = transaction {
        // Очистка только таблиц, связанных с жителями и пользователями
        // В правильном порядке (с учетом foreign keys)
        UserRights.deleteWhere { UserRights.userId.isNotNull() }
        Users.deleteWhere { Users.id.isNotNull() }
        PersonRights.deleteWhere { PersonRights.personId.isNotNull() }
        PersonDao.all().forEach { it.delete() }
        // Rights не удаляем, так как они могут использоваться другими таблицами

        // Загрузка в правильном порядке
        // 1. Rights (используем существующие по имени или создаем новые)
        val existingRightsById = RightDao.all().associateBy { it.id.value }
        val existingRightsByName = RightDao.all().associateBy { it.name }
        val rightsMap = backup.rights.associate { right ->
            // Сначала проверяем по ID, затем по имени, иначе создаем новое
            val dao = existingRightsById[right.id] 
                ?: existingRightsByName[right.name]
                ?: RightDao.new {
                    this.name = right.name
                }
            right.id to dao.id
        }

        // 2. Persons
        val personsMap = backup.persons.associate { person ->
            val dao = PersonDao.new(person.id) {
                this.firstName = person.firstName
                this.lastName = person.lastName
                this.registrationPlace = person.registrationPlace
                this.health = person.health
                this.balance = person.balance
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

        // 4. Users
        val usersMap = backup.users.associate { user ->
            val dao = UserDao.new(user.id) {
                this.username = user.username
                this.passwordHash = user.passwordHash
                this.isActive = user.isActive
                this.createdAt = user.createdAt
                this.personId = user.personId?.let { personsMap[it] }
                this.severiteClicks = user.severiteClicks
                this.hasSaveProgressUpgrade = user.hasSaveProgressUpgrade
                this.clickMultiplier = user.clickMultiplier
            }
            user.id to dao.id
        }

        // 5. UserRights
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
