package org.vengeful.citymanager.adminPanel.configurations

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.vengeful.citymanager.adminPanel.AdminStats
import org.vengeful.citymanager.adminPanel.RequestLog
import org.vengeful.citymanager.adminPanel.ServerStats
import org.vengeful.citymanager.bankService.IBankRepository
import org.vengeful.citymanager.models.AdministrationConfig
import org.vengeful.citymanager.models.CallStatus
import org.vengeful.citymanager.models.ChatMessage
import org.vengeful.citymanager.models.Enterprise
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.SalaryPaymentRequest
import org.vengeful.citymanager.models.SalaryPaymentResponse
import org.vengeful.citymanager.models.SendMessageRequest
import org.vengeful.citymanager.models.medicine.MedicineOrderNotification
import org.vengeful.citymanager.models.news.NewsSource
import org.vengeful.citymanager.newsService.INewsRepository
import org.vengeful.citymanager.personService.IPersonRepository
import org.vengeful.citymanager.personService.db.PersonRepository
import org.vengeful.citymanager.stockSerivce.IStockRepository
import org.vengeful.citymanager.userService.IUserRepository
import org.vengeful.citymanager.userService.db.UserRepository
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

private val requestLogs = mutableListOf<RequestLog>()
private val chatMessages = mutableListOf<ChatMessage>()
private val medicineOrderNotifications = mutableListOf<MedicineOrderNotification>()

private val serverStartTime = System.currentTimeMillis()

private val activeConnectionsTracker = java.util.concurrent.ConcurrentHashMap<String, Long>()
private val CONNECTION_TIMEOUT_MS = 5 * 60 * 1000L // 5 минут

private var adminConfig = AdministrationConfig(
    severiteRate = 42.75,
    controlLossThreshold = 75,
    stocks = emptyList(),
)

private fun registerActiveConnection(token: String?) {
    token?.let {
        activeConnectionsTracker[it] = System.currentTimeMillis()
    }
}

private fun getActiveConnectionsCount(): Int {
    val currentTime = System.currentTimeMillis()
    activeConnectionsTracker.entries.removeAll { (currentTime - it.value) > CONNECTION_TIMEOUT_MS }
    return activeConnectionsTracker.size
}

fun getMedicineOrderNotifications(count: Int = 50): List<MedicineOrderNotification> {
    return medicineOrderNotifications.takeLast(count)
}

fun getSeveriteRate(): Double {
    return adminConfig.severiteRate
}

private fun getRecentMessages(count: Int = 5): List<ChatMessage> {
    return chatMessages.takeLast(count)
}

fun Application.configureAdminApi(
    repository: IPersonRepository,
    bankRepository: IBankRepository,
    userRepository: IUserRepository,
    stockRepository: IStockRepository,
    newsRepository: INewsRepository
) {

    val stocksFromDb = stockRepository.getAllStocks()
    adminConfig = adminConfig.copy(stocks = stocksFromDb)

    routing {
        route("/admin") {
            // 📊 Статистика сервера
            get("/stats") {
                val stats = ServerStats(
                    personCount = getPersonCountFromDB(repository),
                    userCount = userRepository.getCount(),
                    activeConnections = getActiveConnectionsCount(),
                    uptime = calculateUptime(),
                    memoryUsage = getMemoryUsage()
                )
                call.respond(stats)
            }

            route("/news") {
                post("/items") {
                    try {
                        val multipart = call.receiveMultipart()
                        var title: String? = null
                        var source: NewsSource? = null
                        var imageBytes: ByteArray? = null
                        var fileName: String? = null

                        multipart.forEachPart { part ->
                            when (part) {
                                is PartData.FormItem -> {
                                    when (part.name) {
                                        "title" -> title = part.value
                                        "source" -> {
                                            source = try {
                                                NewsSource.valueOf(part.value)
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                    }
                                }

                                is PartData.FileItem -> {
                                    if (part.name == "image") {
                                        fileName = part.originalFileName
                                        imageBytes = part.streamProvider().readBytes()
                                    }
                                }

                                else -> {}
                            }
                            part.dispose()
                        }

                        if (imageBytes == null || fileName == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Image is required"))
                            return@post
                        }

                        if (source == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Source is required"))
                            return@post
                        }

                        // Проверка расширения файла
                        val extension = fileName!!.substringAfterLast('.', "").lowercase()
                        if (extension !in listOf("png", "jpg", "jpeg")) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Only PNG and JPG images are allowed")
                            )
                            return@post
                        }

                        // Генерация названия, если не указано
                        val finalTitle = if (title.isNullOrBlank()) {
                            val formatter = DateTimeFormatter.ofPattern("dd/MM")
                            LocalDate.now().format(formatter)
                        } else {
                            title.take(500)
                        }

                        // Сохранение файла
                        val uploadDir = File("src/main/resources/news_images")
                        uploadDir.mkdirs()
                        val uniqueFileName = "${UUID.randomUUID()}.$extension"
                        val file = File(uploadDir, uniqueFileName)
                        file.writeBytes(imageBytes!!)

                        val imageUrl = "/news/images/$uniqueFileName"
                        val news = newsRepository.createNews(finalTitle, imageUrl, source!!)
                        call.respond(HttpStatusCode.Created, news)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    }
                }

                delete("/items/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid news ID")
                    val success = newsRepository.deleteNews(id)
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "News not found"))
                    }
                }
            }

    get("/medicine-orders") {
        val notifications = getMedicineOrderNotifications(50)
        call.respond(notifications)
    }

    post("/medicine-orders/{orderId}/status") {
        try {
            val orderId = call.parameters["orderId"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid order ID")

            val request = call.receive<Map<String, String>>()
            val newStatus = request["status"] ?: throw IllegalArgumentException("Status is required")

            val success = updateMedicineOrderStatus(orderId, newStatus)
            if (success) {
                call.respond(mapOf("status" to "success", "message" to "Status updated"))
            } else {
                call.respond(
                    io.ktor.http.HttpStatusCode.NotFound,
                    mapOf("error" to "Order not found")
                )
            }
        } catch (e: Exception) {
            call.respond(
                io.ktor.http.HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

    delete("/medicine-orders/{orderId}") {
        try {
            val orderId = call.parameters["orderId"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid order ID")

            val success = removeMedicineOrder(orderId)
            if (success) {
                call.respond(mapOf("status" to "success", "message" to "Order removed"))
            } else {
                call.respond(
                    io.ktor.http.HttpStatusCode.NotFound,
                    mapOf("error" to "Order not found")
                )
            }
        } catch (e: Exception) {
            call.respond(
                io.ktor.http.HttpStatusCode.BadRequest,
                mapOf("error" to e.message)
            )
        }
    }

    get("/config") {
        val recentMessages = getRecentMessages(5)
        val stocksFromDb = stockRepository.getAllStocks()
        val config = adminConfig.copy(
            recentMessages = recentMessages,
            stocks = stocksFromDb
        )
        call.respond(config)
    }

    post("/config") {
        val newConfig = call.receive<AdministrationConfig>()
        adminConfig = newConfig.copy(recentMessages = adminConfig.recentMessages)
        val currentStocksInDb = stockRepository.getAllStocks()
        val currentStockNames = currentStocksInDb.map { it.name }.toSet()
        val newStockNames = newConfig.stocks.map { it.name }.toSet()
        currentStocksInDb.forEach { stock ->
            if (stock.name !in newStockNames) {
                stockRepository.deleteStock(stock.name)
            }
        }
        newConfig.stocks.forEach { stockConfig ->
            val existing = stockRepository.getStockByName(stockConfig.name)
            if (existing == null) {
                stockRepository.createStock(stockConfig)
            } else if (existing.averagePrice != stockConfig.averagePrice) {
                stockRepository.updateStock(stockConfig.name, stockConfig.averagePrice)
            }
        }
        val updatedStocks = stockRepository.getAllStocks()
        adminConfig = adminConfig.copy(stocks = updatedStocks)
        call.respond(mapOf("status" to "success", "message" to "Конфиг обновлён!"))
    }

    // 📋 Журнал запросов
    get("/logs") {
        call.respond(requestLogs)
    }

    // 🗑️ Очистка логов
    post("/clear-logs") {
        requestLogs.clear()
        call.respond(mapOf("status" to "success", "message" to "Logs cleared"))
    }

    // 💾 Экспорт данных
    get("/export") {
        val allData = getAllDataFromDB(repository)
        addLogEntry("GET", "/admin/export", 200)
        call.respond(allData)
    }

    post("/chat/send") {
        val request = call.receive<SendMessageRequest>()
        val message = ChatMessage(
            text = request.text,
            timestamp = System.currentTimeMillis(),
            sender = request.sender
        )
        chatMessages.add(message)
        if (chatMessages.size > 50) {
            chatMessages.removeFirst()
        }

        call.respond(mapOf("status" to "success", "message" to "Message sent"))
    }

    post("/salary/pay") {
        try {
            val request = call.receive<SalaryPaymentRequest>()
            val salaryAmount = request.amount

            if (salaryAmount <= 0) {
                call.respond(
                    io.ktor.http.HttpStatusCode.BadRequest,
                    mapOf("error" to "Сумма должна быть положительной")
                )
                return@post
            }

            // Список прав, которым нужно выплатить зарплату
            val eligibleRights = listOf(
                Rights.Administration,
                Rights.Medic,
                Rights.Police,
            )

            // Получаем всех людей с нужными правами
            val eligiblePersons = repository.personsByRights(eligibleRights)

            // Получаем счет предприятия "Администрация"
            val adminEnterpriseAccount = bankRepository.getBankAccountByEnterpriseName("Администрация")

            if (adminEnterpriseAccount == null) {
                call.respond(
                    io.ktor.http.HttpStatusCode.NotFound,
                    mapOf("error" to "Счет предприятия 'Администрация' не найден")
                )
                return@post
            }

            // Подсчитываем общую сумму выплат (только для людей со счетами)
            val personsWithAccounts = eligiblePersons.filter { person ->
                bankRepository.getBankAccountByPersonId(person.id) != null
            }

            val totalAmount = salaryAmount * personsWithAccounts.size

            if (adminEnterpriseAccount.creditAmount < totalAmount) {
                call.respond(
                    io.ktor.http.HttpStatusCode.BadRequest,
                    mapOf("error" to "Недостаточно средств на счете предприятия. Доступно: ${adminEnterpriseAccount.creditAmount}, Требуется: $totalAmount")
                )
                return@post
            }

            var successCount = 0
            var failedCount = 0
            val errors = mutableListOf<String>()

            for (person in personsWithAccounts) {
                try {
                    val success = repository.addToPersonBalance(person.id, salaryAmount)
                    if (success) {
                        successCount++
                    } else {
                        failedCount++
                        errors.add("Не удалось выплатить зарплату ${person.firstName} ${person.lastName}")
                    }
                } catch (e: Exception) {
                    failedCount++
                    errors.add("Ошибка при выплате ${person.firstName} ${person.lastName}: ${e.message}")
                }
            }

            // Вычитаем общую сумму со счета предприятия (обновляем creditAmount)
            val updatedEnterpriseAccount = adminEnterpriseAccount.copy(
                creditAmount = adminEnterpriseAccount.creditAmount - totalAmount
            )
            bankRepository.updateBankAccount(updatedEnterpriseAccount, null)

            call.respond(
                io.ktor.http.HttpStatusCode.OK,
                SalaryPaymentResponse(
                    message = "Выплата зарплаты выполнена",
                    successCount = successCount,
                    failedCount = failedCount,
                    totalAmount = totalAmount,
                    errors = errors
                )
            )
        } catch (e: Exception) {
            call.respond(
                io.ktor.http.HttpStatusCode.InternalServerError,
                mapOf("error" to "Ошибка при выплате зарплаты: ${e.message}")
            )
        }
    }

}

intercept(ApplicationCallPipeline.Call) {
    val token = call.request.header("Authorization")?.removePrefix("Bearer ")
    registerActiveConnection(token)
    if (!call.request.uri.startsWith("/admin/")) {
        addLogEntry(
            method = call.request.httpMethod.value,
            endpoint = call.request.uri,
            status = call.response.status()?.value ?: 200
        )
    }
}
}
}

fun updateMedicineOrderStatus(orderId: Int, newStatus: String): Boolean {
    val notification = medicineOrderNotifications.find { it.id == orderId }
    return if (notification != null) {
        val index = medicineOrderNotifications.indexOf(notification)
        medicineOrderNotifications[index] = notification.copy(status = newStatus)
        true
    } else {
        false
    }
}

fun removeMedicineOrder(orderId: Int): Boolean {
    val notification = medicineOrderNotifications.find { it.id == orderId }
    return if (notification != null) {
        medicineOrderNotifications.remove(notification)
        true
    } else {
        false
    }
}

fun addMedicineOrderNotification(notification: MedicineOrderNotification) {
    medicineOrderNotifications.add(notification)

    // Ограничиваем размер (последние 100 уведомлений)
    if (medicineOrderNotifications.size > 100) {
        medicineOrderNotifications.removeFirst()
    }
}


private fun addLogEntry(method: String, endpoint: String, status: Int) {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    requestLogs.add(RequestLog(timestamp, method, endpoint, status))

    // Ограничиваем размер логов (последние 100 записей)
    if (requestLogs.size > 100) {
        requestLogs.removeFirst()
    }
}

private fun getPersonCountFromDB(repository: IPersonRepository): Int {
    return repository.getCount()
}

@Suppress("DefaultLocale")
private fun calculateUptime(): String {
    val uptimeMillis = System.currentTimeMillis() - serverStartTime
    val seconds = uptimeMillis / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

private fun getMemoryUsage(): String {
    val runtime = Runtime.getRuntime()
    val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    return "${usedMemory}MB"
}

private fun getAllDataFromDB(personRepository: IPersonRepository): List<Any> {
    return personRepository.allPersons()
}
