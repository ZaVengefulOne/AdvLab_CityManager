package org.vengeful.citymanager.adminPanel.configurations

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.vengeful.citymanager.adminPanel.AdminStats
import org.vengeful.citymanager.adminPanel.RequestLog
import org.vengeful.citymanager.adminPanel.ServerStats
import org.vengeful.citymanager.models.AdministrationConfig
import org.vengeful.citymanager.personService.IPersonRepository
import org.vengeful.citymanager.personService.db.PersonRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Временное хранилище для логов (в реальном приложении используй БД)
private val requestLogs = mutableListOf<RequestLog>()

private var adminConfig = AdministrationConfig(
    severiteRate = 42.75,
    controlLossThreshold = 75,
)

fun Application.configureAdminApi(repository: IPersonRepository) {
    routing {
        route("/admin") {
            // 📊 Статистика сервера
            get("/stats") {
                val stats = ServerStats(
                    personCount = getPersonCountFromDB(repository),
                    activeConnections = 1,
                    uptime = calculateUptime(),
                    memoryUsage = getMemoryUsage()
                )
                call.respond(stats)
            }

            get("/config") {
                call.respond(adminConfig)
            }

            post("/config"){
                val newConfig = call.receive<AdministrationConfig>()
                adminConfig = newConfig
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
        }

        // Логируем все запросы к API
        intercept(ApplicationCallPipeline.Call) {
            if (call.request.uri.startsWith("/persons/")) {
                addLogEntry(
                    method = call.request.httpMethod.value,
                    endpoint = call.request.uri,
                    status = call.response.status()?.value ?: 200
                )
            }
        }
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

private fun calculateUptime(): String {
    // Простая реализация - в реальном приложении считай с момента старта сервера
    return "12:34:56"
}

private fun getMemoryUsage(): String {
    val runtime = Runtime.getRuntime()
    val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    return "${usedMemory}MB"
}

private fun getAllDataFromDB(personRepository: IPersonRepository): List<Any> {
    return personRepository.allPersons()
}
