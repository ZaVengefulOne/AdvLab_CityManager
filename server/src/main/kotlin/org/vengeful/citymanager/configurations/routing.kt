package org.vengeful.citymanager.configurations

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.vengeful.citymanager.Greeting
import org.vengeful.citymanager.models.users.LoginRequest
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.models.users.AuthResponse
import org.vengeful.citymanager.models.users.User
import org.vengeful.citymanager.personService.IPersonRepository
import java.util.Date
import io.ktor.server.request.contentType
import io.ktor.server.request.header
import io.ktor.server.routing.put
import org.vengeful.citymanager.auth.EmergencyShutdownConfig
import org.vengeful.citymanager.auth.SessionLockManager
import org.vengeful.citymanager.backupService.BackupService
import org.vengeful.citymanager.bankService.IBankRepository
import org.vengeful.citymanager.medicService.MedicalRepository
import org.vengeful.citymanager.models.BankAccount
import org.vengeful.citymanager.models.CallRequest
import org.vengeful.citymanager.models.CallStatus
import org.vengeful.citymanager.models.CreateMedicalRecordRequest
import org.vengeful.citymanager.models.emergencyShutdown.EmergencyShutdownRequest
import org.vengeful.citymanager.models.emergencyShutdown.EmergencyShutdownResponse
import org.vengeful.citymanager.models.emergencyShutdown.EmergencyShutdownStatusResponse
import org.vengeful.citymanager.models.Enterprise
import org.vengeful.citymanager.models.UpdateMedicalRecordRequest
import org.vengeful.citymanager.models.emergencyShutdown.ErrorResponse
import org.vengeful.citymanager.models.backup.MasterBackup
import org.vengeful.citymanager.models.users.CreateBankAccountRequest
import org.vengeful.citymanager.models.users.RegisterRequest
import org.vengeful.citymanager.models.users.RegisterResponse
import org.vengeful.citymanager.models.users.UpdateBankAccountRequest
import org.vengeful.citymanager.models.users.UpdateClicksRequest
import org.vengeful.citymanager.models.users.UpdateUserRequest
import org.vengeful.citymanager.userService.IUserRepository

private val callStatuses = mutableMapOf(
    Enterprise.POLICE to CallStatus(Enterprise.POLICE, false),
    Enterprise.MEDIC to CallStatus(Enterprise.MEDIC, false),
    Enterprise.BANK to CallStatus(Enterprise.BANK, false),
    Enterprise.COURT to CallStatus(Enterprise.COURT, false)
)

fun Application.configureSerialization(
    personRepository: IPersonRepository,
    userRepository: IUserRepository,
    bankRepository: IBankRepository,
    emergencyShutdownConfig: EmergencyShutdownConfig
) {

    fun getCurrentUser(call: ApplicationCall, userRepository: IUserRepository): User? {
        return try {
            val principal = call.principal<JWTPrincipal>()
            principal?.let {
                val userIdClaim = it.payload.getClaim("userId")
                if (userIdClaim.isNull) {
                    println("getCurrentUser: userId claim is null")
                    return null
                }
                val userId = userIdClaim.asInt()

                // Проверяем блокировку сессий
                val token = call.request.header("Authorization")?.removePrefix("Bearer ") ?: ""
                if (SessionLockManager.isSessionBlocked(userId, token)) {
                    println("getCurrentUser: Session blocked for user $userId")
                    return null
                }

                println("getCurrentUser: extracted userId=$userId")
                val user = userRepository.findById(userId)
                if (user == null) {
                    println("getCurrentUser: user with id=$userId not found in repository")
                }
                user
            }
        } catch (e: Exception) {
            println("getCurrentUser error: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            null
        }
    }

    routing { // Публичный роутинг
        get("/") {
            call.respondText("Vengeful Server: ${Greeting().greet()}")
        }
        get("/library") {
            call.respondText(text = "Здесь будет библиотека г. Лабтауна. В разработке.")
        }

        post("/adminReg") {
            try {
                val registerRequest = call.receive<RegisterRequest>()

                // Валидация входных данных
                if (registerRequest.username.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username cannot be empty"))
                    return@post
                }

                if (registerRequest.password.isBlank() || registerRequest.password.length < 4) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Password must be at least 4 characters")
                    )
                    return@post
                }

                val rights = registerRequest.rights.ifEmpty {
                    listOf(Rights.Any)
                }

                val user = userRepository.registerUser(
                    username = registerRequest.username,
                    password = registerRequest.password,
                    personId = registerRequest.personId,
                    rights = rights
                )

                call.respond(
                    HttpStatusCode.Created,
                    RegisterResponse(
                        message = "User registered successfully",
                        userId = user.id,
                        username = user.username
                    )
                )
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                println("Registration error: ${e::class.simpleName} - ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Registration failed: ${e.message}")
                )
            }
        }

        get("/adminPersons") {
            val persons = personRepository.allPersons()
            call.respond(HttpStatusCode.OK, persons)
        }
    }

    routing { // Приватный роутинг

        // Аутентификация и регистрация
        route("/auth") {
            post("/login") {
                try {
                    println("Received login request")
                    println("Content-Type: ${call.request.contentType()}")
                    println("Headers: ${call.request.headers.entries()}")

                    val loginRequest = call.receive<LoginRequest>()
                    println("Login attempt: username='${loginRequest.username}'")

                    val user = authenticateUser(
                        loginRequest.username,
                        loginRequest.password,
                        userRepository
                    )
                    val wrongPasswordUser = User(0, "error", "", listOf(Rights.Any))

                    when (user) {
                        wrongPasswordUser -> call.respond(HttpStatusCode.Forbidden)
                        is User -> {
                            // Проверяем блокировку перед выдачей токена
                            if (!SessionLockManager.isUserAllowedToLogin(user.id)) {
                                call.respond(
                                    HttpStatusCode.ServiceUnavailable,
                                    ErrorResponse("Система под блокировкой. Повторите попытку позже")
                                )
                                return@post
                            }

                            val token = generateJwtToken(user)
                            call.respond(HttpStatusCode.OK, AuthResponse(token, user, user.rights))
                        }

                        null -> call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid credentials"))
                    }

                } catch (e: JsonConvertException) {
                    println("JSON deserialization error: ${e::class.simpleName}")
                    println("Error message: ${e.message}")
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid JSON format: ${e.message}"))
                } catch (e: Exception) {
                    println("Login error: ${e::class.simpleName}")
                    println("Error message: ${e.message}")
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request: ${e.message}"))
                }
            }

            post("/register") {
                try {
                    val registerRequest = call.receive<RegisterRequest>()

                    // Валидация входных данных
                    if (registerRequest.username.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username cannot be empty"))
                        return@post
                    }

                    if (registerRequest.password.isBlank() || registerRequest.password.length < 4) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Password must be at least 4 characters")
                        )
                        return@post
                    }

                    val rights = registerRequest.rights.ifEmpty {
                        listOf(Rights.Any)
                    }

                    val user = userRepository.registerUser(
                        username = registerRequest.username,
                        password = registerRequest.password,
                        personId = registerRequest.personId,
                        rights = rights
                    )

                    call.respond(
                        HttpStatusCode.Created,
                        RegisterResponse(
                            message = "User registered successfully",
                            userId = user.id,
                            username = user.username
                        )
                    )
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    println("Registration error: ${e::class.simpleName} - ${e.message}")
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Registration failed: ${e.message}")
                    )
                }
            }
        }

        authenticate("auth-jwt") {
            route("/users") {
                // GET /users - получить всех пользователей
                get {
                    try {
                        val users = userRepository.getAllUsers()
                        call.respond(HttpStatusCode.OK, users)
                    } catch (e: Exception) {
                        println("Get users error: ${e::class.simpleName} - ${e.message}")
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to get users: ${e.message}")
                        )
                    }
                }

                // PUT /users/{id} - обновить пользователя
                put("/{id}") {
                    try {
                        val id = call.parameters["id"]?.toInt()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID"))
                            return@put
                        }

                        val updateRequest = call.receive<UpdateUserRequest>()

                        // Валидация: ID в параметре должен совпадать с ID в теле запроса
                        if (updateRequest.id != id) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "User ID in path does not match ID in request body")
                            )
                            return@put
                        }

                        // Проверяем существование пользователя
                        val existingUser = userRepository.findById(id)
                        if (existingUser == null) {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                            return@put
                        }

                        // Проверяем уникальность username (если изменился)
                        if (updateRequest.username != existingUser.username) {
                            if (userRepository.userExists(updateRequest.username)) {
                                call.respond(
                                    HttpStatusCode.Conflict,
                                    mapOf("error" to "Username already exists")
                                )
                                return@put
                            }
                        }

                        // Создаем объект User для обновления
                        val userToUpdate = User(
                            id = updateRequest.id,
                            username = updateRequest.username,
                            passwordHash = existingUser.passwordHash, // Будет обновлен, если передан пароль
                            rights = updateRequest.rights,
                            isActive = updateRequest.isActive,
                            createdAt = existingUser.createdAt
                        )

                        // Обновляем пользователя
                        val updatedUser = userRepository.updateUser(
                            user = userToUpdate,
                            password = updateRequest.password,
                            personId = updateRequest.personId
                        )

                        if (updatedUser == null) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("error" to "Failed to update user")
                            )
                        } else {
                            call.respond(HttpStatusCode.OK, updatedUser)
                        }
                    } catch (e: JsonConvertException) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid JSON format: ${e.message}")
                        )
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: Exception) {
                        println("Update user error: ${e::class.simpleName} - ${e.message}")
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to update user: ${e.message}")
                        )
                    }
                }

                // DELETE /users/{id} - удалить пользователя (отвязать Person)
                delete("/{id}") {
                    try {
                        val id = call.parameters["id"]?.toInt()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID"))
                            return@delete
                        }

                        // Проверяем существование пользователя
                        val existingUser = userRepository.findById(id)
                        if (existingUser == null) {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                            return@delete
                        }

                        // Удаляем пользователя (Person будет отвязан автоматически)
                        val deleted = userRepository.deleteUser(id)

                        if (deleted) {
                            call.respond(HttpStatusCode.OK, mapOf("message" to "User deleted successfully"))
                        } else {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("error" to "Failed to delete user")
                            )
                        }
                    } catch (e: Exception) {
                        println("Delete user error: ${e::class.simpleName} - ${e.message}")
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to delete user: ${e.message}")
                        )
                    }
                }

                // PUT /users/{id}/clicks - обновить количество кликов
                put("/{id}/clicks") {
                    try {
                        val id = call.parameters["id"]?.toInt()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID"))
                            return@put
                        }

                        val request = call.receive<UpdateClicksRequest>()
                        println("PUT /users/$id/clicks: request.clicks=${request.clicks}")

                        // Проверяем, что пользователь обновляет свои клики
                        val currentUser = getCurrentUser(call, userRepository)
                        if (currentUser == null) {
                            println("PUT /users/$id/clicks: currentUser is null")
                            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not authenticated"))
                            return@put
                        }

                        println("PUT /users/$id/clicks: currentUser.id=${currentUser.id}")

                        if (currentUser.id != id) {
                            println("PUT /users/$id/clicks: Forbidden - currentUser.id=${currentUser.id} != requested id=$id")
                            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Can only update own clicks"))
                            return@put
                        }

                        val success = userRepository.updateUserClicks(id, request.clicks)
                        println("PUT /users/$id/clicks: updateUserClicks returned success=$success")

                        if (success) {
                            call.respond(HttpStatusCode.OK, mapOf("message" to "Clicks updated successfully"))
                        } else {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                        }
                    } catch (e: JsonConvertException) {
                        println("PUT /users/{id}/clicks: JSON error - ${e.message}")
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON format: ${e.message}"))
                    } catch (e: Exception) {
                        println("PUT /users/{id}/clicks: Exception - ${e::class.simpleName} - ${e.message}")
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to update clicks: ${e.message}")
                        )
                    }
                }

            }

            route("/persons") {
                // Get all
                get {
                    val persons = personRepository.allPersons()
                    call.respond(HttpStatusCode.OK, persons)
                }

                // Get by Id
                get("/byId/{id}") {
                    val id = call.parameters["id"]?.toInt()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }
                    val person = personRepository.personById(id)
                    if (person == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(person)
                }

                // Get by Name
                get("/byName/{name}_{lastName}") {
                    val name = call.parameters["name"]
                    val lastName = call.parameters["lastName"]
                    if (name == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }
                    val person = personRepository.personByName(name, lastName)
                    if (person == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(person)
                }

                // Get by Rights
                get("/byRights") {
                    val rights = call.queryParameters["rights"]
                    if (rights.isNullOrBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest
                        )
                        return@get
                    }
                    try {
                        val rightsList = rights.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .map { Rights.valueOf(it) }

                        val persons = personRepository.personsByRights(rightsList)
                        if (persons.isEmpty()) {
                            call.respond(
                                HttpStatusCode.NotFound,
                                mapOf("error" to "No persons found with rights: $rights")
                            )
                        } else {
                            call.respond(persons)
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid rights value. Available: ${Rights.entries.joinToString { it.name }}, full error: ${e.message}")
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Internal server error: ${e.message}")
                        )
                    }
                }

                // Add person
                post {
                    try {
                        val person = call.receive<Person>()
                        personRepository.addPerson(person)
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
                        personRepository.updatePerson(person)
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
                    if (personRepository.removePerson(id)) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }

            route("/bank") {
                // GET /bank/accounts - получить все банковские счета
                get("/accounts") {
                    try {
                        val accounts = bankRepository.getAllBankAccounts()
                        call.respond(HttpStatusCode.OK, accounts)
                    } catch (e: Exception) {
                        println("Get bank accounts error: ${e::class.simpleName} - ${e.message}")
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to get bank accounts: ${e.message}")
                        )
                    }
                }

                // GET /bank/accounts/person/{personId} - получить счет по personId
                get("/accounts/person/{personId}") {
                    try {
                        val personId = call.parameters["personId"]?.toInt()
                        if (personId == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid person ID"))
                            return@get
                        }
                        val account = bankRepository.getBankAccountByPersonId(personId)
                        if (account == null) {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Bank account not found"))
                        } else {
                            call.respond(HttpStatusCode.OK, account)
                        }
                    } catch (e: Exception) {
                        println("Get bank account by personId error: ${e::class.simpleName} - ${e.message}")
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to get bank account: ${e.message}")
                        )
                    }
                }

                // GET /bank/accounts/{id} - получить счет по id
                get("/accounts/{id}") {
                    try {
                        val id = call.parameters["id"]?.toInt()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid account ID"))
                            return@get
                        }
                        val account = bankRepository.getBankAccountById(id)
                        if (account == null) {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Bank account not found"))
                        } else {
                            call.respond(HttpStatusCode.OK, account)
                        }
                    } catch (e: Exception) {
                        println("Get bank account by id error: ${e::class.simpleName} - ${e.message}")
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to get bank account: ${e.message}")
                        )
                    }
                }

                // POST /bank/accounts - создать банковский счет
                post("/accounts") {
                    try {
                        val request = call.receive<CreateBankAccountRequest>()

                        if (request.personId != null && request.personId!! <= 0) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid person ID"))
                            return@post
                        }

                        if (request.depositAmount < 0 || request.creditAmount < 0) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Amounts cannot be negative"))
                            return@post
                        }

                        // Если это предприятие, должно быть указано название
                        if (request.personId == null && (request.enterpriseName.isNullOrBlank())) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Enterprise name is required for enterprise accounts")
                            )
                            return@post
                        }

                        val account = bankRepository.createBankAccount(
                            personId = request.personId,
                            enterpriseName = request.enterpriseName, // НОВОЕ
                            depositAmount = request.depositAmount,
                            creditAmount = request.creditAmount
                        )
                        call.respond(HttpStatusCode.Created, account)
                    } catch (e: IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                    } catch (e: JsonConvertException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON format: ${e.message}"))
                    } catch (e: Exception) {
                        println("Create bank account error: ${e::class.simpleName} - ${e.message}")
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to create bank account: ${e.message}")
                        )
                    }
                }

                // PUT /bank/accounts/{id} - обновить банковский счет
                put("/accounts/{id}") {
                    try {
                        val id = call.parameters["id"]?.toInt()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid account ID"))
                            return@put
                        }

                        val request = call.receive<UpdateBankAccountRequest>()

                        if (request.id != id) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Account ID in path does not match ID in request body")
                            )
                            return@put
                        }

                        if (request.depositAmount < 0 || request.creditAmount < 0) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Amounts cannot be negative"))
                            return@put
                        }

                        val existingAccount = bankRepository.getBankAccountById(id)
                        if (existingAccount == null) {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Bank account not found"))
                            return@put
                        }

                        val bankAccount = BankAccount(
                            id = request.id,
                            personId = request.personId,
                            enterpriseName = request.enterpriseName, // НОВОЕ
                            depositAmount = request.depositAmount,
                            creditAmount = request.creditAmount
                        )

                        val updatedAccount = bankRepository.updateBankAccount(bankAccount)
                        if (updatedAccount == null) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("error" to "Failed to update bank account")
                            )
                        } else {
                            call.respond(HttpStatusCode.OK, updatedAccount)
                        }
                    } catch (e: JsonConvertException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON format: ${e.message}"))
                    } catch (e: Exception) {
                        println("Update bank account error: ${e::class.simpleName} - ${e.message}")
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to update bank account: ${e.message}")
                        )
                    }
                }

                // DELETE /bank/accounts/{id} - удалить банковский счет
                delete("/accounts/{id}") {
                    try {
                        val id = call.parameters["id"]?.toInt()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid account ID"))
                            return@delete
                        }

                        val existingAccount = bankRepository.getBankAccountById(id)
                        if (existingAccount == null) {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Bank account not found"))
                            return@delete
                        }

                        val deleted = bankRepository.deleteBankAccount(id)
                        if (deleted) {
                            call.respond(HttpStatusCode.OK, mapOf("message" to "Bank account deleted successfully"))
                        } else {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("error" to "Failed to delete bank account")
                            )
                        }
                    } catch (e: Exception) {
                        println("Delete bank account error: ${e::class.simpleName} - ${e.message}")
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to delete bank account: ${e.message}")
                        )
                    }
                }
            }

            route("/medic") {
                val medicalRepository = MedicalRepository()

                // Создание медицинской карточки
                post("/medical-records") {
                    try {
                        val request = call.receive<CreateMedicalRecordRequest>()
                        val record = medicalRepository.createMedicalRecord(
                            record = request.record,
                            healthStatus = request.healthStatus
                        )
                        call.respond(HttpStatusCode.OK, record)
                    } catch (e: IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "")
                    }
                }

                // Получение списка пациентов с медкартами
                get("/patients") {
                    try {
                        val patients = medicalRepository.getPatientsWithRecords()
                        call.respond(HttpStatusCode.OK, patients)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "")
                    }
                }

                // Получение медкарт по personId
                get("/medical-records/{personId}") {
                    val personId = call.parameters["personId"]?.toInt()
                    if (personId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }
                    try {
                        val records = medicalRepository.getMedicalRecordsByPersonId(personId)
                        call.respond(HttpStatusCode.OK, records)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "")
                    }
                }

                put("/medical-records/{recordId}") {
                    try {
                        val recordId = call.parameters["recordId"]?.toInt()
                        if (recordId == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid record ID")
                            return@put
                        }
                        val request = call.receive<UpdateMedicalRecordRequest>()
                        val record = medicalRepository.updateMedicalRecord(
                            recordId = recordId,
                            record = request.record,
                            healthStatus = request.healthStatus
                        )
                        call.respond(HttpStatusCode.OK, record)
                    } catch (e: IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "")
                    }
                }

                // НОВЫЙ: Получение медкарты по ID
                get("/medical-records/byId/{recordId}") {
                    try {
                        val recordId = call.parameters["recordId"]?.toInt()
                        if (recordId == null) {
                            call.respond(HttpStatusCode.BadRequest)
                            return@get
                        }
                        val record = medicalRepository.getMedicalRecordById(recordId)
                        if (record == null) {
                            call.respond(HttpStatusCode.NotFound)
                        } else {
                            call.respond(HttpStatusCode.OK, record)
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, e.message ?: "")
                    }
                }
            }


            route("/backup") {
                // Проверка прав Joker
                fun checkJokerAccess(call: ApplicationCall, userRepository: IUserRepository): Boolean {
                    val currentUser = getCurrentUser(call, userRepository)
                    return currentUser?.rights?.contains(Rights.Joker) == true
                }

                // GET /backup/game?format=html - получить игровой бэкап в формате HTML
                get("/game") {
                    try {
                        if (!checkJokerAccess(call, userRepository)) {
                            call.respond(
                                HttpStatusCode.Forbidden,
                                mapOf("error" to "Access denied. You have no rights!")
                            )
                            return@get
                        }

                        val format = call.request.queryParameters["format"] ?: "html"
                        val backupService = BackupService(personRepository, userRepository, bankRepository)

                        when (format.lowercase()) {
                            "html" -> {
                                val html = backupService.createGameBackupHtml()
                                call.response.headers.append("Content-Type", "text/html; charset=UTF-8")
                                call.respondText(html, contentType = io.ktor.http.ContentType.Text.Html)
                            }

                            "markdown" -> {
                                val markdown = backupService.createGameBackupMarkdown()
                                call.response.headers.append("Content-Type", "text/markdown; charset=UTF-8")
                                call.respondText(markdown, contentType = io.ktor.http.ContentType.Text.Plain)
                            }

                            else -> {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    mapOf("error" to "Invalid format. Use 'html' or 'markdown'")
                                )
                            }
                        }
                    } catch (e: Exception) {
                        println("Get game backup error: ${e::class.simpleName} - ${e.message}")
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to create game backup: ${e.message}")
                        )
                    }
                }

                // GET /backup/master - получить мастерский бэкап (JSON)
                get("/master") {
                    try {
                        if (!checkJokerAccess(call, userRepository)) {
                            call.respond(
                                HttpStatusCode.Forbidden,
                                mapOf("error" to "Access denied. You have no rights!")
                            )
                            return@get
                        }

                        val backupService = BackupService(personRepository, userRepository, bankRepository)
                        val masterBackup = backupService.createMasterBackup()
                        call.respond(HttpStatusCode.OK, masterBackup)
                    } catch (e: Exception) {
                        println("Get master backup error: ${e::class.simpleName} - ${e.message}")
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to create master backup: ${e.message}")
                        )
                    }
                }

                // POST /backup/restore - загрузить и восстановить мастерский бэкап
                post("/restore") {
                    try {
                        if (!checkJokerAccess(call, userRepository)) {
                            call.respond(
                                HttpStatusCode.Forbidden,
                                mapOf("error" to "Access denied. You have no rights!")
                            )
                            return@post
                        }

                        val masterBackup = call.receive<MasterBackup>()
                        val backupService = BackupService(personRepository, userRepository, bankRepository)

                        backupService.restoreFromMasterBackup(masterBackup)

                        call.respond(
                            HttpStatusCode.OK,
                            mapOf("message" to "Database restored successfully from master backup")
                        )
                    } catch (e: JsonConvertException) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid JSON format: ${e.message}")
                        )
                    } catch (e: Exception) {
                        println("Restore backup error: ${e::class.simpleName} - ${e.message}")
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to restore backup: ${e.message}")
                        )
                    }
                }
            }


            route("/administration") {
                post("/emergency-shutdown") {
                    try {
                        val currentUser = getCurrentUser(call, userRepository)
                        if (currentUser == null) {
                            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("User not authenticated"))
                            return@post
                        }

                        val request = call.receive<EmergencyShutdownRequest>()

                        if (request.durationMinutes !in 1..30) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("Duration must be between 1 and 30 minutes")
                            )
                            return@post
                        }

                        // Проверяем отдельный пароль экстренного отключения
                        if (request.password != emergencyShutdownConfig.password) {
                            call.respond(
                                HttpStatusCode.Forbidden,
                                ErrorResponse("Invalid emergency shutdown password")
                            )
                            return@post
                        }

                        SessionLockManager.activateEmergencyShutdown(
                            allowedUserId = currentUser.id,
                            durationMinutes = request.durationMinutes
                        )

                        call.respond(
                            HttpStatusCode.OK,
                            EmergencyShutdownResponse(
                                message = "Emergency shutdown activated",
                                durationMinutes = request.durationMinutes,
                                allowedUserId = currentUser.id
                            )
                        )
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Invalid argument"))
                    } catch (e: Exception) {
                        println("Emergency shutdown error: ${e::class.simpleName} - ${e.message}")
                        e.printStackTrace()
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse("Failed to activate emergency shutdown: ${e.message}")
                        )
                    }
                }

                get("/emergency-shutdown/status") {
                    try {
                        val isActive = SessionLockManager.isEmergencyShutdownActive()
                        val remainingTimeMillis = SessionLockManager.getRemainingTimeMillis()
                        val remainingTimeSeconds = remainingTimeMillis?.let { it / 1000 } ?: 0L

                        call.respond(
                            HttpStatusCode.OK,
                            EmergencyShutdownStatusResponse(
                                isActive = isActive,
                                remainingTimeSeconds = if (isActive) remainingTimeSeconds else null
                            )
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse("Failed to get status: ${e.message}")
                        )
                    }
                }
            }


            route("/call") {
                get("/status/{enterprise}") {
                    val enterpriseStr = call.parameters["enterprise"]
                    val enterprise = try {
                        Enterprise.valueOf(enterpriseStr ?: "")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid enterprise"))
                        return@get
                    }

                    val status = callStatuses[enterprise] ?: CallStatus(enterprise, false)
                    call.respond(status)
                }

                post("/send") {
                    val request = call.receive<CallRequest>()

                    callStatuses[request.enterprise] = CallStatus(
                        enterprise = request.enterprise,
                        isCalled = true,
                        calledAt = System.currentTimeMillis()
                    )

                    call.respond(mapOf("status" to "success", "message" to "Call sent"))
                }

                // Сбросить статус вызова (когда представитель ответил)
                post("/reset/{enterprise}") {
                    val enterpriseStr = call.parameters["enterprise"]
                    val enterprise = try {
                        Enterprise.valueOf(enterpriseStr ?: "")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid enterprise"))
                        return@post
                    }

                    callStatuses[enterprise] = CallStatus(enterprise, false)
                    call.respond(mapOf("status" to "success", "message" to "Call reset"))
                }
            }


        }
    }
}

// Функция для генерации JWT токена
private fun Application.generateJwtToken(user: User): String {
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val expirationTime = environment.config.property("jwt.expiration_time").getString().toLong()

    val rightsAsStrings = user.rights.map { it.name }

    return JWT.create()
        .withAudience(jwtAudience)
        .withIssuer(jwtIssuer)
        .withClaim("userId", user.id)
        .withClaim("username", user.username)
        .withClaim("rights", rightsAsStrings)
        .withExpiresAt(Date(System.currentTimeMillis() + expirationTime * 1000))
        .sign(Algorithm.HMAC256(jwtSecret))
}
