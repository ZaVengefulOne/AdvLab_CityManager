package org.vengeful.cityManager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.attributes.InputType
import org.vengeful.cityManager.models.RequestLog
import org.vengeful.cityManager.models.ServerStats
import org.vengeful.citymanager.models.AdministrationConfig
import org.vengeful.citymanager.models.ChatMessage
import org.vengeful.citymanager.models.backup.MasterBackup
import org.vengeful.citymanager.models.medicine.Medicine
import org.vengeful.citymanager.models.medicine.MedicineOrderNotification
import org.vengeful.citymanager.models.stocks.StockConfig
import kotlin.collections.emptyList
import kotlin.js.Date
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun AdminApp() {
    val authManager = AuthManager()

    val coroutineScope = MainScope()
    var isLoggedIn by mutableStateOf(authManager.isLoggedIn())
    var showLoginDialog by mutableStateOf(!authManager.isLoggedIn())
    var medicineOrderNotifications by mutableStateOf<List<MedicineOrderNotification>>(emptyList())

    val onUnauthorized = {
        isLoggedIn = false
        showLoginDialog = true
        window.alert("Сессия истекла. Пожалуйста, войдите снова.")
    }

    val apiClient = ApiClient(authManager, onUnauthorized)
    // Состояния

    var loginUsername by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    var loginError by mutableStateOf<String?>(null)
    var isLoggingIn by mutableStateOf(false)

    var serverStats by mutableStateOf(ServerStats(0, 0, 0, "00:00:00", "0 MB"))
    var requestLogs by mutableStateOf(emptyList<RequestLog>())
    var showBackupDialog by mutableStateOf(false)
    var backupData by mutableStateOf<String?>(null)
    var isBackupLoading by mutableStateOf(false)
    var isLoading by mutableStateOf(false)

    var severitRate by mutableStateOf("42.75")
    var controlLossThreshold by mutableStateOf("75")
    var isConfigLoading by mutableStateOf(false)

    var chatMessageText by mutableStateOf("")
    var chatMessages by mutableStateOf<List<ChatMessage>>(emptyList())

    var medicines by mutableStateOf<List<Medicine>>(emptyList())
    var showMedicineDialog by mutableStateOf(false)
    var editingMedicine by mutableStateOf<Medicine?>(null)
    var medicineName by mutableStateOf("")
    var medicinePrice by mutableStateOf("")

    var salaryAmount by mutableStateOf("20.0") // Размер зарплаты
    var isPayingSalary by mutableStateOf(false)
    var salaryPaymentResult by mutableStateOf<String?>(null)

    var stocks by mutableStateOf<List<StockConfig>>(emptyList())
    var showStockDialog by mutableStateOf(false)
    var editingStockIndex by mutableStateOf<Int?>(null)
    var stockName by mutableStateOf("")
    var stockPrice by mutableStateOf("")

    // Загрузка данных при старте (только если залогинен)
    if (isLoggedIn) {
        LaunchedEffect(Unit) {
            isLoading = true
            try {
                serverStats = apiClient.getServerStats()
                requestLogs = apiClient.getRequestLogs()
                val config = apiClient.getConfig()
                severitRate = config.severiteRate.toString()
                controlLossThreshold = config.controlLossThreshold.toString()
                chatMessages = config.recentMessages
                stocks = config.stocks
                medicineOrderNotifications = apiClient.getMedicineOrderNotifications()
                medicines = apiClient.getAllMedicines()
            } catch (e: Exception) {
                window.alert("Ошибка подключения к серверу: ${e.message}")
            }
            isLoading = false
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) return@LaunchedEffect
        while (isLoggedIn) {
            kotlinx.coroutines.delay(30000)
            try {
                medicineOrderNotifications = apiClient.getMedicineOrderNotifications()
            } catch (e: Exception) {

            }
        }
    }

    // Форма входа
    if (showLoginDialog) {
        Div({
            style {
                top(0.px)
                left(0.px)
                width(100.percent)
                height(100.percent)
                backgroundColor(Color("rgba(0, 0, 0, 0.9)"))
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
            }
        }) {
            Div({
                style {
                    backgroundColor(Color("#2C3E50"))
                    border(2.px, LineStyle.Solid, Color("#4A90E2"))
                    borderRadius(8.px)
                    padding(40.px)
                    maxWidth(400.px)
                    width(90.percent)
                }
            }) {
                H2({
                    style {
                        marginTop(0.px)
                        marginBottom(24.px)
                        color(Color("#FFFFFF"))
                        textAlign("center")
                    }
                }) {
                    Text("🔐 Вход в админ-панель")
                }

                if (loginError != null) {
                    P({
                        style {
                            color(Color("#E74C3C"))
                            marginBottom(16.px)
                            fontSize(14.px)
                        }
                    }) {
                        Text(loginError ?: "")
                    }
                }

                Input(InputType.Text, {
                    style {
                        width(100.percent)
                        padding(12.px)
                        marginBottom(16.px)
                        backgroundColor(Color("#1A2530"))
                        color(Color("#4A90E2"))
                        border(2.px, LineStyle.Solid, Color("#4A90E2"))
                        borderRadius(4.px)
                        fontFamily("'Courier New', monospace")
                        fontSize(14.px)
                    }
                    attr("placeholder", "Имя пользователя")
                    value(loginUsername)
                    onInput { event ->
                        val target = event.target
                        loginUsername = target.value
                    }
                })

                Input(InputType.Password, {
                    style {
                        width(100.percent)
                        padding(12.px)
                        marginBottom(24.px)
                        backgroundColor(Color("#1A2530"))
                        color(Color("#4A90E2"))
                        border(2.px, LineStyle.Solid, Color("#4A90E2"))
                        borderRadius(4.px)
                        fontFamily("'Courier New', monospace")
                        fontSize(14.px)
                    }
                    attr("placeholder", "Пароль")
                    value(loginPassword)
                    onInput { event ->
                        val target = event.target
                        loginPassword = target.value
                    }
                })

                Button({
                    style {
                        width(100.percent)
                        backgroundColor(
                            if (isLoggingIn || loginUsername.isBlank() || loginPassword.isBlank()) Color("#7F8C8D") else Color(
                                "#4A90E2"
                            )
                        )
                        color(Color("#FFFFFF"))
                        borderWidth(0.px)
                        padding(12.px, 24.px)
                        borderRadius(4.px)
                        fontFamily("'Courier New', monospace")
                        fontWeight("bold")
                        cursor(if (isLoggingIn || loginUsername.isBlank() || loginPassword.isBlank()) "not-allowed" else "pointer")
                        fontSize(14.px)
                    }
                    onClick {
                        if (!isLoggingIn && loginUsername.isNotBlank() && loginPassword.isNotBlank()) {
                            coroutineScope.launch {
                                isLoggingIn = true
                                loginError = null
                                try {
                                    apiClient.login(loginUsername, loginPassword)
                                    isLoggedIn = true
                                    showLoginDialog = false
                                    loginUsername = ""
                                    loginPassword = ""
                                } catch (e: Exception) {
                                    loginError = "Ошибка входа: ${e.message}"
                                }
                                isLoggingIn = false
                            }
                        }
                    }
                }) {
                    Text(if (isLoggingIn) "⏳ Вход..." else "Войти")
                }
            }
        }
    }

    if (!isLoggedIn) {
        return
    }

    Div({
        style {
            fontFamily("'Courier New', monospace")
            backgroundColor(Color("#2C3E50"))
            color(Color("#4A90E2"))
            margin(0.px)
            padding(20.px)
            property("max-width", "1200px")
            property("margin", "0 auto")
        }
    }) {
        // Заголовок с кнопкой выхода
        Div({
            style {
                backgroundColor(Color("#34495E"))
                border(2.px, LineStyle.Solid, Color("#4A90E2"))
                borderRadius(8.px)
                padding(20.px)
                marginBottom(16.px)
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                flexWrap(FlexWrap("wrap"))
            }
        }) {
            Div({
                style {
                    flex(1)
                    minWidth(0.px)
                }
            }) {
                H1({
                    style {
                        marginTop(0.px)
                        marginBottom(8.px)
                        fontSize(24.px)
                        fontWeight("bold")
                        textAlign("center")
                    }
                }) {
                    Text("⚙️ АДМИНИСТРИРОВАНИЕ СИСТЕМЫ ГОСУДАРСТВЕННОГО КОНТРОЛЯ")
                }
                P({
                    style {
                        textAlign("center")
                        marginTop(8.px)
                        marginBottom(0.px)
                    }
                }) {
                    Text("Панель мониторинга и управления базой данных")
                }
            }

            Button({
                style {
                    backgroundColor(Color("#E74C3C"))
                    color(Color("#FFFFFF"))
                    borderWidth(0.px)
                    padding(10.px, 20.px)
                    borderRadius(4.px)
                    fontFamily("'Courier New', monospace")
                    fontWeight("bold")
                    cursor("pointer")
                    fontSize(14.px)
                    marginLeft(16.px)
                    whiteSpace("nowrap")
                }
                onClick {
                    authManager.clearToken()
                    isLoggedIn = false
                    showLoginDialog = true
                    // Очистка данных при выходе
                    serverStats = ServerStats(0, 0, 0,"00:00:00", "0 MB")
                    requestLogs = emptyList()
                    backupData = null
                }
            }) {
                Text("🚪 Выход")
            }
        }

        if (isLoading) {
            Div({
                style {
                    textAlign("center")
                    padding(40.px)
                    fontSize(18.px)
                }
            }) {
                Text("🔄 Загрузка данных...")
            }
        } else {
            // Статистика сервера
            Div({
                style {
                    backgroundColor(Color("#34495E"))
                    border(2.px, LineStyle.Solid, Color("#4A90E2"))
                    borderRadius(8.px)
                    padding(20.px)
                    marginBottom(16.px)
                }
            }) {
                H3({
                    style {
                        marginTop(0.px)
                        marginBottom(16.px)
                        fontSize(18.px)
                    }
                }) {
                    Text("📊 СТАТИСТИКА СЕРВЕРА")
                }

                Div({
                    style {
                        display(DisplayStyle.Grid)
                        gridTemplateColumns("repeat(auto-fit, minmax(200px, 1fr))")
                        gap(16.px)
                    }
                }) {
                    StatCard(
                        value = serverStats.personCount.toString(),
                        label = "записей в базе"
                    )

                    StatCard(
                        value = serverStats.userCount.toString(),
                        label = "пользователей"
                    )

                    StatCard(
                        value = serverStats.activeConnections.toString(),
                        label = "активных подключений"
                    )

                    StatCard(
                        value = serverStats.uptime,
                        label = "время работы"
                    )

                    StatCard(
                        value = serverStats.memoryUsage,
                        label = "использование памяти"
                    )
                }
            }

            // Журнал запросов
            Div({
                style {
                    backgroundColor(Color("#34495E"))
                    border(2.px, LineStyle.Solid, Color("#4A90E2"))
                    borderRadius(8.px)
                    padding(20.px)
                    marginBottom(16.px)
                }
            }) {
                H3({
                    style {
                        marginTop(0.px)
                        marginBottom(16.px)
                        fontSize(18.px)
                    }
                }) {
                    Text("📋 ЖУРНАЛ ЗАПРОСОВ")
                }

                Button({
                    style {
                        backgroundColor(Color("#4A90E2"))
                        color(Color("#FFFFFF"))
                        borderWidth(0.px)
                        padding(8.px, 16.px)
                        borderRadius(4.px)
                        fontFamily("'Courier New', monospace")
                        fontWeight("bold")
                        cursor("pointer")
                        margin(4.px)
                    }
                    onClick {
                        coroutineScope.launch {
                            apiClient.clearLogs()
                            requestLogs = apiClient.getRequestLogs()
                        }
                    }
                }) {
                    Text("🗑️ Очистить логи")
                }

                Div({
                    style {
                        maxHeight(300.px)
                        overflowY("auto")
                        marginTop(16.px)
                        padding(8.px)
                        backgroundColor(Color("#1A2530"))
                        borderRadius(4.px)
                    }
                }) {
                    if (requestLogs.isEmpty()) {
                        Div({
                            style {
                                textAlign("center")
                                padding(20.px)
                                color(Color("#7B9EB0"))
                            }
                        }) {
                            Text("Логи отсутствуют")
                        }
                    } else {
                        requestLogs.forEach { log ->
                            Div({
                                style {
                                    fontSize(12.px)
                                    margin(4.px)
                                    padding(8.px)
                                    backgroundColor(Color("#2C3E50"))
                                    borderRadius(4.px)
                                    border(3.px, LineStyle.Solid, Color("#4A90E2"))
                                }
                            }) {
                                Text("${log.timestamp} ${log.method} ${log.endpoint} - ${log.status}")
                            }
                        }
                    }
                }
            }

            Div({
                style {
                    backgroundColor(Color("#34495E"))
                    border(2.px, LineStyle.Solid, Color("#4A90E2"))
                    borderRadius(8.px)
                    padding(20.px)
                    marginBottom(16.px)
                }
            }) {
                H3({
                    style {
                        marginTop(0.px)
                        marginBottom(16.px)
                        fontSize(18.px)
                    }
                }) {
                    Text("💾 МАСТЕРСКИЕ БЭКАПЫ БАЗЫ ДАННЫХ")
                }

                Div({
                    style {
                        display(DisplayStyle.Flex)
                        gap(12.px)
                        flexWrap(FlexWrap("wrap"))
                    }
                }) {
                    Button({
                        style {
                            backgroundColor(Color("#27AE60"))
                            color(Color("#FFFFFF"))
                            borderWidth(0.px)
                            padding(12.px, 24.px)
                            borderRadius(4.px)
                            fontFamily("'Courier New', monospace")
                            fontWeight("bold")
                            cursor("pointer")
                            fontSize(14.px)
                        }
                        onClick {
                            coroutineScope.launch {
                                isBackupLoading = true
                                try {
                                    val backup = apiClient.getMasterBackup()
                                    val jsonString = Json.encodeToString(backup)
                                    backupData = jsonString

                                    // Скачать файл
                                    val url = js("URL.createObjectURL(blob)")
                                    val link = js("document.createElement('a')")
                                    link.href = url
                                    link.download = "master_backup_${Clock.System.now().toEpochMilliseconds()}.json"
                                    js("document.body.appendChild(link)")
                                    link.click()
                                    js("document.body.removeChild(link)")
                                    js("URL.revokeObjectURL(url)")

                                    window.alert("✅ Мастерский бэкап успешно создан и скачан!")
                                } catch (e: Exception) {
                                    window.alert("❌ Ошибка при создании бэкапа: ${e.message}")
                                }
                                isBackupLoading = false
                            }
                        }
                    }) {
                        Text(if (isBackupLoading) "⏳ Создание..." else "📥 Создать и скачать мастерский бэкап")
                    }

                    Button({
                        style {
                            backgroundColor(Color("#E74C3C"))
                            color(Color("#FFFFFF"))
                            borderWidth(0.px)
                            padding(12.px, 24.px)
                            borderRadius(4.px)
                            fontFamily("'Courier New', monospace")
                            fontWeight("bold")
                            cursor("pointer")
                            fontSize(14.px)
                        }
                        onClick {
                            showBackupDialog = true
                        }
                    }) {
                        Text("📤 Загрузить и восстановить бэкап")
                    }
                }

                if (showBackupDialog) {
                    Div({
                        style {
                            top(0.px)
                            left(0.px)
                            width(100.percent)
                            height(100.percent)
                            backgroundColor(Color("rgba(0, 0, 0, 0.8)"))
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            justifyContent(JustifyContent.Center)
                        }
                    }) {
                        Div({
                            style {
                                backgroundColor(Color("#2C3E50"))
                                border(2.px, LineStyle.Solid, Color("#4A90E2"))
                                borderRadius(8.px)
                                padding(30.px)
                                maxWidth(600.px)
                                width(90.percent)
                            }
                        }) {
                            H3({
                                style {
                                    marginTop(0.px)
                                    marginBottom(16.px)
                                    color(Color("#FFFFFF"))
                                }
                            }) {
                                Text("📤 Восстановление из мастерского бэкапа")
                            }

                            P({
                                style {
                                    color(Color("#E74C3C"))
                                    marginBottom(16.px)
                                }
                            }) {
                                Text("⚠️ ВНИМАНИЕ: Это действие полностью очистит базу данных и восстановит данные из бэкапа!")
                            }

                            TextArea(
                                attrs = {
                                    style {
                                        width(100.percent)
                                        minHeight(300.px)
                                        padding(12.px)
                                        backgroundColor(Color("#1A2530"))
                                        color(Color("#4A90E2"))
                                        border(2.px, LineStyle.Solid, Color("#4A90E2"))
                                        borderRadius(4.px)
                                        fontFamily("'Courier New', monospace")
                                        fontSize(12.px)
                                    }
                                    attr("placeholder", "Вставьте JSON содержимое мастерского бэкапа здесь...")
                                    value(backupData ?: "")
                                    onInput { event ->
                                        val target = event.target
                                        backupData = target.value
                                    }
                                }
                            )

                            Div({
                                style {
                                    display(DisplayStyle.Flex)
                                    gap(12.px)
                                    marginTop(16.px)
                                    justifyContent(JustifyContent.FlexEnd)
                                }
                            }) {
                                Button({
                                    style {
                                        backgroundColor(Color("#7F8C8D"))
                                        color(Color("#FFFFFF"))
                                        borderWidth(0.px)
                                        padding(8.px, 16.px)
                                        borderRadius(4.px)
                                        fontFamily("'Courier New', monospace")
                                        cursor("pointer")
                                    }
                                    onClick {
                                        showBackupDialog = false
                                        backupData = null
                                    }
                                }) {
                                    Text("Отмена")
                                }

                                Button({
                                    style {
                                        backgroundColor(Color("#E74C3C"))
                                        color(Color("#FFFFFF"))
                                        borderWidth(0.px)
                                        padding(8.px, 16.px)
                                        borderRadius(4.px)
                                        fontFamily("'Courier New', monospace")
                                        fontWeight("bold")
                                        cursor("pointer")
                                    }
                                    onClick {
                                        coroutineScope.launch {
                                            try {
                                                val jsonString = backupData ?: return@launch
                                                val backup = Json.decodeFromString<MasterBackup>(jsonString)

                                                if (window.confirm("Вы уверены? Это действие полностью очистит базу данных!")) {
                                                    apiClient.restoreMasterBackup(backup)
                                                    window.alert("✅ База данных успешно восстановлена из бэкапа!")
                                                    showBackupDialog = false
                                                    backupData = null
                                                    // Обновить данные
                                                    serverStats = apiClient.getServerStats()
                                                }
                                            } catch (e: Exception) {
                                                window.alert("❌ Ошибка при восстановлении: ${e.message}")
                                            }
                                        }
                                    }
                                }) {
                                    Text("Восстановить")
                                }
                            }
                        }
                    }
                }
            }

            Div({
                style {
                    backgroundColor(Color("#34495E"))
                    border(2.px, LineStyle.Solid, Color("#4A90E2"))
                    borderRadius(8.px)
                    padding(20.px)
                    marginBottom(16.px)
                }
            }) {
                H3({
                    style {
                        marginTop(0.px)
                        marginBottom(16.px)
                        fontSize(18.px)
                    }
                }) {
                    Text("💬 ЧАТ")
                }

                // Список сообщений
                Div({
                    style {
                        maxHeight(120.px)
                        overflowY("auto")
                        backgroundColor(Color("#1A2530"))
                        borderRadius(4.px)
                        padding(12.px)
                        marginBottom(12.px)
                    }
                }) {
                    if (chatMessages.isEmpty()) {
                        P({
                            style {
                                color(Color("#7B9EB0"))
                                fontSize(12.px)
                            }
                        }) {
                            Text("Нет сообщений")
                        }
                    } else {
                        chatMessages.forEach { message ->
                            Div({
                                style {
                                    marginBottom(8.px)
                                    padding(8.px)
                                    backgroundColor(Color("#2C3E50"))
                                    borderRadius(4.px)
                                }
                            }) {
                                Div({
                                    style {
                                        display(DisplayStyle.Flex)
                                        justifyContent(JustifyContent.SpaceBetween)
                                        marginBottom(4.px)
                                    }
                                }) {
                                    Span({
                                        style {
                                            color(if (message.sender == "admin") Color("#4A90E2") else Color("#27AE60"))
                                            fontSize(10.px)
                                            fontWeight("bold")
                                        }
                                    }) {
                                        Text(if (message.sender == "admin") "Эбони-Бэй" else "Лэбтаун")
                                    }
                                    Span({
                                        style {
                                            color(Color("#7B9EB0"))
                                            fontSize(10.px)
                                        }
                                    }) {
                                        Text(Date(message.timestamp).toLocaleTimeString())
                                    }
                                }
                                P({
                                    style {
                                        color(Color("#FFFFFF"))
                                        fontSize(12.px)
                                        margin(0.px)
                                    }
                                }) {
                                    Text(message.text)
                                }
                            }
                        }
                    }
                }

                // Поле ввода и кнопка
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        gap(8.px)
                    }
                }) {
                    Input(InputType.Text, {
                        style {
                            flex(1)
                            padding(8.px)
                            backgroundColor(Color("#1A2530"))
                            color(Color("#4A90E2"))
                            border(2.px, LineStyle.Solid, Color("#4A90E2"))
                            borderRadius(4.px)
                            fontFamily("'Courier New', monospace")
                            fontSize(14.px)
                        }
                        value(chatMessageText)
                        onInput { event ->
                            chatMessageText = event.target.value
                        }
                        onKeyDown { event ->
                            if (event.key == "Enter") {
                                if (chatMessageText.isNotBlank()) {
                                    coroutineScope.launch {
                                        try {
                                            apiClient.sendChatMessage(chatMessageText)
                                            chatMessageText = ""
                                            // Обновить конфигурацию для получения новых сообщений
                                            val config = apiClient.getConfig()
                                            chatMessages = config.recentMessages
                                        } catch (e: Exception) {
                                            window.alert("Ошибка отправки: ${e.message}")
                                        }
                                    }
                                }
                            }
                        }
                    })

                    Button({
                        style {
                            backgroundColor(Color("#4A90E2"))
                            color(Color("#FFFFFF"))
                            borderWidth(0.px)
                            padding(8.px, 16.px)
                            borderRadius(4.px)
                            fontFamily("'Courier New', monospace")
                            fontWeight("bold")
                            cursor("pointer")
                            fontSize(14.px)
                        }
                        onClick {
                            if (chatMessageText.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        apiClient.sendChatMessage(chatMessageText)
                                        chatMessageText = ""
                                        val config = apiClient.getConfig()
                                        chatMessages = config.recentMessages
                                    } catch (e: Exception) {
                                        window.alert("Ошибка отправки: ${e.message}")
                                    }
                                }
                            }
                        }
                    }) {
                        Text("Отправить")
                    }
                }
            }

            Div({
                style {
                    backgroundColor(Color("#34495E"))
                    border(2.px, LineStyle.Solid, Color("#4A90E2"))
                    borderRadius(8.px)
                    padding(20.px)
                    marginBottom(16.px)
                }
            }) {
                H3({
                    style {
                        marginTop(0.px)
                        marginBottom(16.px)
                        color(Color("#4A90E2"))
                        fontSize(18.px)
                        fontWeight("bold")
                    }
                }) {
                    Text("💊 ЗАКАЗЫ ЛЕКАРСТВ")
                }

                Div({
                    style {
                        maxHeight(300.px)
                        overflowY("auto")
                        backgroundColor(Color("#1A2530"))
                        borderRadius(4.px)
                        padding(12.px)
                    }
                }) {
                    if (medicineOrderNotifications.isEmpty()) {
                        P({
                            style {
                                color(Color("#7B9EB0"))
                                fontSize(12.px)
                            }
                        }) {
                            Text("Нет заказов")
                        }
                    } else {
                        medicineOrderNotifications.reversed().forEach { notification ->
                            Div({
                                onClick {
                                    coroutineScope.launch {
                                        try {
                                            val newStatus = when (notification.status) {
                                                "pending" -> "delivering"
                                                "delivering" -> "delivered"
                                                "delivered" -> {
                                                    apiClient.deleteMedicineOrder(notification.id)
                                                    medicineOrderNotifications =
                                                        apiClient.getMedicineOrderNotifications()
                                                    return@launch
                                                }

                                                else -> "pending"
                                            }

                                            if (newStatus != null) {
                                                if (apiClient.updateMedicineOrderStatus(notification.id, newStatus)) {
                                                    medicineOrderNotifications =
                                                        apiClient.getMedicineOrderNotifications()
                                                } else {
                                                    window.alert("Ошибка обновления статуса")
                                                }
                                            }
                                        } catch (e: Exception) {
                                            window.alert("Ошибка: ${e.message}")
                                        }
                                    }
                                }
                                style {
                                    marginBottom(12.px)
                                    padding(12.px)
                                    backgroundColor(
                                        Color("#2C3E50")
                                    )
                                    borderRadius(8.px)
                                    border(1.px, LineStyle.Solid, Color("#34495E"))
                                    cursor("pointer")
                                }
                            }) {
                                Div({
                                    style {
                                        display(DisplayStyle.Flex)
                                        justifyContent(JustifyContent.SpaceBetween)
                                        marginBottom(8.px)
                                    }
                                }) {
                                    Span({
                                        style {
                                            color(Color("#4A90E2"))
                                            fontSize(12.px)
                                            fontWeight("bold")
                                        }
                                    }) {
                                        Text(notification.medicineName)
                                    }
                                    Span({
                                        style {
                                            color(Color("#7B9EB0"))
                                            fontSize(10.px)
                                            backgroundColor(
                                                when (notification.status) {
                                                    "pending" -> Color("#2C3E50")
                                                    "delivering" -> Color("#3498DB")
                                                    "delivered" -> Color("#27AE60")
                                                    else -> Color("#2C3E50")
                                                }
                                            )
                                        }
                                    }) {
                                        Text(Date(notification.timestamp).toLocaleString())
                                    }
                                }

                                Div({
                                    style {
                                        color(Color("#FFFFFF"))
                                        fontSize(11.px)
                                        marginBottom(4.px)
                                    }
                                }) {
                                    Text("Количество: ${notification.quantity} шт.")
                                }

                                Div({
                                    style {
                                        color(Color("#FFFFFF"))
                                        fontSize(11.px)
                                        marginBottom(4.px)
                                    }
                                }) {
                                    Text("Сумма: ${notification.totalPrice} ЛБ")
                                }

                                Div({
                                    style {
                                        color(Color("#7B9EB0"))
                                        fontSize(10.px)
                                    }
                                }) {
                                    Text(
                                        when {
                                            notification.orderedByEnterprise != null ->
                                                "Заказано от: ${notification.orderedByEnterprise}"

                                            notification.orderedByPersonName != null ->
                                                "Заказано: ${notification.orderedByPersonName}"

                                            else -> "Заказано: Неизвестно"
                                        }
                                    )
                                }

                                Div({
                                    style {
                                        marginTop(8.px)
                                        padding(4.px, 8.px)
                                        backgroundColor(
                                            when (notification.status) {
                                                "pending" -> Color("#F39C12")
                                                "delivered" -> Color("#27AE60")
                                                "cancelled" -> Color("#E74C3C")
                                                else -> Color("#7B9EB0")
                                            }
                                        )
                                        borderRadius(4.px)
                                        display(DisplayStyle.InlineBlock)
                                    }
                                }) {
                                    Span({
                                        style {
                                            padding(4.px, 8.px)
                                            borderRadius(4.px)
                                            fontSize(11.px)
                                            fontWeight("bold")
                                            backgroundColor(
                                                when (notification.status) {
                                                    "pending" -> Color("#F39C12")
                                                    "delivering" -> Color("#3498DB")
                                                    "delivered" -> Color("#27AE60")
                                                    else -> Color("#7B9EB0")
                                                }
                                            )
                                            color(Color.white)
                                            cursor("pointer")
                                        }
                                    }) {
                                        Text(
                                            when (notification.status) {
                                                "pending" -> "Ожидает доставки"
                                                "delivering" -> "Доставляется"
                                                "delivered" -> "Доставлено"
                                                else -> notification.status
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Div({
                style {
                    backgroundColor(Color("#34495E"))
                    border(2.px, LineStyle.Solid, Color("#4A90E2"))
                    borderRadius(8.px)
                    padding(20.px)
                    marginBottom(16.px)
                }
            }) {
                H3({
                    style {
                        marginTop(0.px)
                        marginBottom(16.px)
                        color(Color("#4A90E2"))
                        fontSize(18.px)
                        fontWeight("bold")
                    }
                }) {
                    Text("💊 Управление лекарствами")
                }

                // Кнопка добавления
                Button({
                    style {
                        backgroundColor(Color("#27AE60"))
                        color(Color("#FFFFFF"))
                        borderWidth(0.px)
                        padding(8.px, 16.px)
                        borderRadius(4.px)
                        fontFamily("'Courier New', monospace")
                        fontWeight("bold")
                        cursor("pointer")
                        fontSize(14.px)
                        marginBottom(16.px)
                    }
                    onClick {
                        editingMedicine = null
                        medicineName = ""
                        medicinePrice = ""
                        showMedicineDialog = true
                    }
                }) {
                    Text("+ Добавить лекарство")
                }

                // Список лекарств
                Div({
                    style {
                        maxHeight(300.px)
                        overflowY("auto")
                        backgroundColor(Color("#1A2530"))
                        borderRadius(4.px)
                        padding(12.px)
                    }
                }) {
                    if (medicines.isEmpty()) {
                        P({
                            style {
                                color(Color("#7B9EB0"))
                                fontSize(12.px)
                            }
                        }) {
                            Text("Нет лекарств")
                        }
                    } else {
                        medicines.forEach { medicine ->
                            Div({
                                style {
                                    marginBottom(8.px)
                                    padding(12.px)
                                    backgroundColor(Color("#2C3E50"))
                                    borderRadius(4.px)
                                    display(DisplayStyle.Flex)
                                    justifyContent(JustifyContent.SpaceBetween)
                                    alignItems(AlignItems.Center)
                                }
                            }) {
                                Div({
                                    style {
                                        flex(1)
                                    }
                                }) {
                                    Div({
                                        style {
                                            color(Color("#FFFFFF"))
                                            fontSize(14.px)
                                            fontWeight("bold")
                                            marginBottom(4.px)
                                        }
                                    }) {
                                        Text(medicine.name)
                                    }
                                    Div({
                                        style {
                                            color(Color("#7B9EB0"))
                                            fontSize(12.px)
                                        }
                                    }) {
                                        Text("Цена: ${medicine.price} ЛБ")
                                    }
                                }
                                Div({
                                    style {
                                        display(DisplayStyle.Flex)
                                        gap(8.px)
                                    }
                                }) {
                                    Button({
                                        style {
                                            backgroundColor(Color("#F39C12"))
                                            color(Color("#FFFFFF"))
                                            borderWidth(0.px)
                                            padding(6.px, 12.px)
                                            borderRadius(4.px)
                                            fontFamily("'Courier New', monospace")
                                            fontSize(12.px)
                                            cursor("pointer")
                                        }
                                        onClick {
                                            editingMedicine = medicine
                                            medicineName = medicine.name
                                            medicinePrice = medicine.price.toString()
                                            showMedicineDialog = true
                                        }
                                    }) {
                                        Text("Изменить")
                                    }
                                    Button({
                                        style {
                                            backgroundColor(Color("#E74C3C"))
                                            color(Color("#FFFFFF"))
                                            borderWidth(0.px)
                                            padding(6.px, 12.px)
                                            borderRadius(4.px)
                                            fontFamily("'Courier New', monospace")
                                            fontSize(12.px)
                                            cursor("pointer")
                                        }
                                        onClick {
                                            if (window.confirm("Удалить лекарство '${medicine.name}'?")) {
                                                coroutineScope.launch {
                                                    try {
                                                        apiClient.deleteMedicine(medicine.id)
                                                        medicines = apiClient.getAllMedicines()
                                                    } catch (e: Exception) {
                                                        window.alert("Ошибка удаления: ${e.message}")
                                                    }
                                                }
                                            }
                                        }
                                    }) {
                                        Text("Удалить")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showMedicineDialog) {
                Div({
                    style {
                        position(Position.Fixed)
                        top(0.px)
                        left(0.px)
                        width(100.percent)
                        height(100.percent)
                        backgroundColor(Color("rgba(0, 0, 0, 0.8)"))
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.Center)
                    }
                    onClick { event ->
//                        // Проверяем, был ли клик именно на фоне, а не на содержимом диалога
//                        val target = event.asDynamic().target
//                        val currentTarget = event.asDynamic().currentTarget
//                        if (target == currentTarget) {
//                            showMedicineDialog = false
//                        }
                    }
                }) {
                    Div({
                        style {
                            backgroundColor(Color("#34495E"))
                            border(2.px, LineStyle.Solid, Color("#4A90E2"))
                            borderRadius(8.px)
                            padding(30.px)
                            maxWidth(400.px)
                            width(90.percent)
                        }
                        onClick {
                            // Пустой onClick предотвращает всплытие события
                            // (в Compose for Web это работает как stopPropagation)
                        }
                    }) {
                        H3({
                            style {
                                marginTop(0.px)
                                marginBottom(20.px)
                                color(Color("#4A90E2"))
                                fontSize(18.px)
                                fontWeight("bold")
                            }
                        }) {
                            Text(if (editingMedicine != null) "Изменить лекарство" else "Добавить лекарство")
                        }

                        Label(attrs = {
                            style {
                                color(Color("#FFFFFF"))
                                fontSize(14.px)
                                fontWeight("bold")
                                display(DisplayStyle.Block)
                                marginBottom(8.px)
                            }
                        }) {
                            Text("Название")
                        }
                        Input(InputType.Text, {
                            style {
                                width(100.percent)
                                padding(12.px)
                                marginBottom(16.px)
                                backgroundColor(Color("#1A2530"))
                                color(Color("#4A90E2"))
                                border(2.px, LineStyle.Solid, Color("#4A90E2"))
                                borderRadius(4.px)
                                fontFamily("'Courier New', monospace")
                                fontSize(14.px)
                            }
                            value(medicineName)
                            onInput { event ->
                                medicineName = event.target.value
                            }
                            onClick {
                                // Предотвращаем всплытие события
                            }
                        })

                        Label(attrs = {
                            style {
                                color(Color("#FFFFFF"))
                                fontSize(14.px)
                                fontWeight("bold")
                                display(DisplayStyle.Block)
                                marginBottom(8.px)
                            }
                        }) {
                            Text("Цена")
                        }
                        Input(InputType.Number, {
                            style {
                                width(100.percent)
                                padding(12.px)
                                marginBottom(20.px)
                                backgroundColor(Color("#1A2530"))
                                color(Color("#4A90E2"))
                                border(2.px, LineStyle.Solid, Color("#4A90E2"))
                                borderRadius(4.px)
                                fontFamily("'Courier New', monospace")
                                fontSize(14.px)
                            }
                            attr("step", "0.01")
                            value(medicinePrice)
                            onInput { event ->
                                medicinePrice = event.target.value
                            }
                            onClick {
                                // Предотвращаем всплытие события
                            }
                        })

                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                gap(12.px)
                                justifyContent(JustifyContent.FlexEnd)
                            }
                        }) {
                            Button({
                                style {
                                    backgroundColor(Color("#7B9EB0"))
                                    color(Color("#FFFFFF"))
                                    borderWidth(0.px)
                                    padding(10.px, 20.px)
                                    borderRadius(4.px)
                                    fontFamily("'Courier New', monospace")
                                    fontSize(14.px)
                                    cursor("pointer")
                                }
                                onClick {
                                    showMedicineDialog = false
                                }
                            }) {
                                Text("Отмена")
                            }
                            Button({
                                style {
                                    backgroundColor(Color("#27AE60"))
                                    color(Color("#FFFFFF"))
                                    borderWidth(0.px)
                                    padding(10.px, 20.px)
                                    borderRadius(4.px)
                                    fontFamily("'Courier New', monospace")
                                    fontSize(14.px)
                                    fontWeight("bold")
                                    cursor("pointer")
                                }
                                onClick {
                                    if (medicineName.isNotBlank() && medicinePrice.toDoubleOrNull() != null) {
                                        coroutineScope.launch {
                                            try {
                                                if (editingMedicine != null) {
                                                    val updated = editingMedicine!!.copy(
                                                        name = medicineName,
                                                        price = medicinePrice.toDouble()
                                                    )
                                                    apiClient.updateMedicine(updated)
                                                } else {
                                                    val newMedicine = Medicine(
                                                        id = 0,
                                                        name = medicineName,
                                                        price = medicinePrice.toDouble()
                                                    )
                                                    apiClient.createMedicine(newMedicine)
                                                }
                                                medicines = apiClient.getAllMedicines()
                                                showMedicineDialog = false
                                                medicineName = ""
                                                medicinePrice = ""
                                                editingMedicine = null
                                            } catch (e: Exception) {
                                                window.alert("Ошибка: ${e.message}")
                                            }
                                        }
                                    } else {
                                        window.alert("Заполните все поля корректно")
                                    }
                                }
                            }) {
                                Text(if (editingMedicine != null) "Сохранить" else "Добавить")
                            }
                        }
                    }
                }
            }

            Div({
                style {
                    backgroundColor(Color("#34495E"))
                    border(2.px, LineStyle.Solid, Color("#4A90E2"))
                    borderRadius(8.px)
                    padding(20.px)
                    marginBottom(16.px)
                }
            }) {
                H3({
                    style {
                        marginTop(0.px)
                        marginBottom(16.px)
                        fontSize(18.px)
                    }
                }) {
                    Text("⚙️ КОНФИГУРАЦИЯ СИСТЕМЫ")
                }

                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(16.px)
                    }
                }) {
                    // Поле курса северита
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            gap(8.px)
                        }
                    }) {
                        Label(attrs = {
                            style {
                                color(Color("#FFFFFF"))
                                fontSize(14.px)
                                fontWeight("bold")
                            }
                        }) {
                            Text("Курс северита")
                        }
                        Input(InputType.Number, {
                            style {
                                width(95.percent)
                                padding(12.px)
                                backgroundColor(Color("#1A2530"))
                                color(Color("#4A90E2"))
                                border(2.px, LineStyle.Solid, Color("#4A90E2"))
                                borderRadius(4.px)
                                fontFamily("'Courier New', monospace")
                                fontSize(14.px)
                            }
                            value(severitRate)
                            onInput { event ->
                                severitRate = event.target.value
                            }
                        })
                    }

                    // Поле границы потери контроля
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            gap(8.px)
                        }
                    }) {
                        Label(attrs = {
                            style {
                                color(Color("#FFFFFF"))
                                fontSize(14.px)
                                fontWeight("bold")
                            }
                        }) {
                            Text("Граница потери контроля (0-100)")
                        }
                        Input(InputType.Number, {
                            style {
                                width(95.percent)
                                padding(12.px)
                                backgroundColor(Color("#1A2530"))
                                color(Color("#4A90E2"))
                                border(2.px, LineStyle.Solid, Color("#4A90E2"))
                                borderRadius(4.px)
                                fontFamily("'Courier New', monospace")
                                fontSize(14.px)
                            }
                            value(controlLossThreshold)
                            onInput { event ->
                                controlLossThreshold = event.target.value
                            }
                        })
                    }

                    Button({
                        style {
                            backgroundColor(if (isConfigLoading) Color("#7F8C8D") else Color("#27AE60"))
                            color(Color("#FFFFFF"))
                            borderWidth(0.px)
                            padding(12.px, 24.px)
                            borderRadius(4.px)
                            fontFamily("'Courier New', monospace")
                            fontWeight("bold")
                            cursor(if (isConfigLoading) "not-allowed" else "pointer")
                            fontSize(14.px)
                        }
                        onClick {
                            coroutineScope.launch {
                                isConfigLoading = true
                                try {
                                    val config = AdministrationConfig(
                                        severiteRate = severitRate.toDoubleOrNull() ?: 42.75,
                                        controlLossThreshold = controlLossThreshold.toIntOrNull() ?: 75
                                    )
                                    apiClient.updateConfig(config)
                                    window.alert("✅ Конфигурация успешно сохранена!")
                                } catch (e: Exception) {
                                    window.alert("❌ Ошибка при сохранении: ${e.message}")
                                }
                                isConfigLoading = false
                            }
                        }
                    }) {
                        Text(if (isConfigLoading) "⏳ Сохранение..." else "💾 Сохранить конфигурацию")
                    }
                }
            }

            Div({
                style {
                    backgroundColor(Color("#34495E"))
                    border(2.px, LineStyle.Solid, Color("#4A90E2"))
                    borderRadius(8.px)
                    padding(20.px)
                    marginBottom(16.px)
                }
            }) {
                H3({
                    style {
                        marginTop(0.px)
                        marginBottom(16.px)
                        fontSize(18.px)
                    }
                }) {
                    Text("💰 ВЫПЛАТА ЗАРПЛАТЫ")
                }

                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(16.px)
                    }
                }) {
                    // Поле ввода суммы зарплаты
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            gap(8.px)
                        }
                    }) {
                        Label(attrs = {
                            style {
                                color(Color("#FFFFFF"))
                                fontSize(14.px)
                                fontWeight("bold")
                            }
                        }) {
                            Text("Сумма зарплаты (ЛБ)")
                        }
                        Input(InputType.Number, {
                            style {
                                width(95.percent)
                                padding(12.px)
                                backgroundColor(Color("#1A2530"))
                                color(Color("#4A90E2"))
                                border(2.px, LineStyle.Solid, Color("#4A90E2"))
                                borderRadius(4.px)
                                fontFamily("'Courier New', monospace")
                                fontSize(14.px)
                            }
                            attr("step", "0.01")
                            value(salaryAmount)
                            onInput { event ->
                                salaryAmount = event.target.value
                            }
                        })
                    }

                    // Сообщение о результате
                    if (salaryPaymentResult != null) {
                        Div({
                            style {
                                padding(12.px)
                                backgroundColor(
                                    if (salaryPaymentResult!!.contains("успешно") || salaryPaymentResult!!.contains("✅"))
                                        Color("#27AE60")
                                    else
                                        Color("#E74C3C")
                                )
                                borderRadius(4.px)
                                color(Color("#FFFFFF"))
                                fontSize(14.px)
                            }
                        }) {
                            Text(salaryPaymentResult ?: "")
                        }
                    }

                    Button({
                        style {
                            backgroundColor(if (isPayingSalary) Color("#7F8C8D") else Color("#27AE60"))
                            color(Color("#FFFFFF"))
                            borderWidth(0.px)
                            padding(12.px, 24.px)
                            borderRadius(4.px)
                            fontFamily("'Courier New', monospace")
                            fontWeight("bold")
                            cursor(if (isPayingSalary) "not-allowed" else "pointer")
                            fontSize(14.px)
                        }
                        onClick {
                            val amount = salaryAmount.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                window.alert("Введите корректную сумму зарплаты")
                                return@onClick
                            }

                            if (!window.confirm("Выплатить зарплату в размере $amount ЛБ?")) {
                                return@onClick
                            }

                            coroutineScope.launch {
                                isPayingSalary = true
                                salaryPaymentResult = null
                                try {
                                    val result = apiClient.paySalary(amount)
                                    val successCount = result.successCount
                                    val failedCount = result.failedCount
                                    val totalAmount = result.totalAmount
                                    val errors = result.errors

                                    var message = "✅ Выплата выполнена!\n"
                                    message += "Успешно: $successCount человек\n"
                                    message += "Ошибок: $failedCount\n"
                                    message += "Общая сумма: $totalAmount ЛБ"

                                    if (errors.isNotEmpty()) {
                                        message += "\n\nОшибки:\n" + errors.joinToString("\n")
                                    }

                                    salaryPaymentResult = message
                                    window.alert(message)

                                    // Обновить статистику сервера
                                    serverStats = apiClient.getServerStats()
                                } catch (e: Exception) {
                                    val errorMsg = "❌ Ошибка при выплате зарплаты: ${e.message}"
                                    salaryPaymentResult = errorMsg
                                    window.alert(errorMsg)
                                }
                                isPayingSalary = false
                            }
                        }
                    }) {
                        Text(if (isPayingSalary) "⏳ Выплата..." else "💰 Выплатить зарплату")
                    }
                }
            }
            Div({
                style {
                    backgroundColor(Color("#34495E"))
                    border(2.px, LineStyle.Solid, Color("#4A90E2"))
                    borderRadius(8.px)
                    padding(20.px)
                    marginBottom(16.px)
                }
            }) {
                H3({
                    style {
                        marginTop(0.px)
                        marginBottom(16.px)
                        fontSize(18.px)
                    }
                }) {
                    Text("📈 УПРАВЛЕНИЕ АКЦИЯМИ")
                }

                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(16.px)
                    }
                }) {
                    // Кнопка добавления
                    Button({
                        style {
                            backgroundColor(Color("#27AE60"))
                            color(Color("#FFFFFF"))
                            borderWidth(0.px)
                            padding(8.px, 16.px)
                            borderRadius(4.px)
                            fontFamily("'Courier New', monospace")
                            fontWeight("bold")
                            cursor("pointer")
                            fontSize(14.px)
                        }
                        onClick {
                            editingStockIndex = null
                            stockName = ""
                            stockPrice = ""
                            showStockDialog = true
                        }
                    }) {
                        Text("+ Добавить акцию")
                    }

                    // Список акций
                    Div({
                        style {
                            maxHeight(300.px)
                            overflowY("auto")
                            backgroundColor(Color("#1A2530"))
                            borderRadius(4.px)
                            padding(12.px)
                        }
                    }) {
                        if (stocks.isEmpty()) {
                            P({
                                style {
                                    color(Color("#7B9EB0"))
                                    fontSize(12.px)
                                }
                            }) {
                                Text("Нет акций")
                            }
                        } else {
                            stocks.forEachIndexed { index, stock ->
                                Div({
                                    style {
                                        marginBottom(8.px)
                                        padding(12.px)
                                        backgroundColor(Color("#2C3E50"))
                                        borderRadius(4.px)
                                        display(DisplayStyle.Flex)
                                        justifyContent(JustifyContent.SpaceBetween)
                                        alignItems(AlignItems.Center)
                                    }
                                }) {
                                    Div({
                                        style {
                                            flex(1)
                                        }
                                    }) {
                                        Div({
                                            style {
                                                color(Color("#FFFFFF"))
                                                fontSize(14.px)
                                                fontWeight("bold")
                                                marginBottom(4.px)
                                            }
                                        }) {
                                            Text(stock.name)
                                        }
                                        Div({
                                            style {
                                                color(Color("#7B9EB0"))
                                                fontSize(12.px)
                                            }
                                        }) {
                                            Text("Средняя цена: ${stock.averagePrice} ЛБ")
                                        }
                                    }
                                    Div({
                                        style {
                                            display(DisplayStyle.Flex)
                                            gap(8.px)
                                        }
                                    }) {
                                        Button({
                                            style {
                                                backgroundColor(Color("#F39C12"))
                                                color(Color("#FFFFFF"))
                                                borderWidth(0.px)
                                                padding(6.px, 12.px)
                                                borderRadius(4.px)
                                                fontFamily("'Courier New', monospace")
                                                fontSize(12.px)
                                                cursor("pointer")
                                            }
                                            onClick {
                                                editingStockIndex = index
                                                stockName = stock.name
                                                stockPrice = stock.averagePrice.toString()
                                                showStockDialog = true
                                            }
                                        }) {
                                            Text("Изменить")
                                        }
                                        Button({
                                            style {
                                                backgroundColor(Color("#E74C3C"))
                                                color(Color("#FFFFFF"))
                                                borderWidth(0.px)
                                                padding(6.px, 12.px)
                                                borderRadius(4.px)
                                                fontFamily("'Courier New', monospace")
                                                fontSize(12.px)
                                                cursor("pointer")
                                            }
                                            onClick {
                                                if (window.confirm("Удалить акцию '${stock.name}'?")) {
                                                    coroutineScope.launch {
                                                        try {
                                                            val updatedStocks = stocks.toMutableList()
                                                            updatedStocks.removeAt(index)
                                                            val config = AdministrationConfig(
                                                                severiteRate = severitRate.toDoubleOrNull() ?: 42.75,
                                                                controlLossThreshold = controlLossThreshold.toIntOrNull() ?: 75,
                                                                stocks = updatedStocks
                                                            )
                                                            apiClient.updateConfig(config)
                                                            stocks = updatedStocks
                                                            window.alert("✅ Акция удалена!")
                                                        } catch (e: Exception) {
                                                            window.alert("❌ Ошибка: ${e.message}")
                                                        }
                                                    }
                                                }
                                            }
                                        }) {
                                            Text("Удалить")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showStockDialog) {
                Div({
                    style {
                        position(Position.Fixed)
                        top(0.px)
                        left(0.px)
                        width(100.percent)
                        height(100.percent)
                        backgroundColor(Color("rgba(0, 0, 0, 0.8)"))
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.Center)
                    }
                }) {
                    Div({
                        style {
                            backgroundColor(Color("#34495E"))
                            border(2.px, LineStyle.Solid, Color("#4A90E2"))
                            borderRadius(8.px)
                            padding(30.px)
                            maxWidth(400.px)
                            width(90.percent)
                        }
                    }) {
                        H3({
                            style {
                                marginTop(0.px)
                                marginBottom(20.px)
                                color(Color("#4A90E2"))
                                fontSize(18.px)
                                fontWeight("bold")
                            }
                        }) {
                            Text(if (editingStockIndex != null) "Изменить акцию" else "Добавить акцию")
                        }

                        Label(attrs = {
                            style {
                                color(Color("#FFFFFF"))
                                fontSize(14.px)
                                fontWeight("bold")
                                display(DisplayStyle.Block)
                                marginBottom(8.px)
                            }
                        }) {
                            Text("Название акции")
                        }
                        Input(InputType.Text, {
                            style {
                                width(100.percent)
                                padding(12.px)
                                marginBottom(16.px)
                                backgroundColor(Color("#1A2530"))
                                color(Color("#4A90E2"))
                                border(2.px, LineStyle.Solid, Color("#4A90E2"))
                                borderRadius(4.px)
                                fontFamily("'Courier New', monospace")
                                fontSize(14.px)
                            }
                            value(stockName)
                            onInput { event ->
                                stockName = event.target.value
                            }
                        })

                        Label(attrs = {
                            style {
                                color(Color("#FFFFFF"))
                                fontSize(14.px)
                                fontWeight("bold")
                                display(DisplayStyle.Block)
                                marginBottom(8.px)
                            }
                        }) {
                            Text("Средняя цена")
                        }
                        Input(InputType.Number, {
                            style {
                                width(100.percent)
                                padding(12.px)
                                marginBottom(20.px)
                                backgroundColor(Color("#1A2530"))
                                color(Color("#4A90E2"))
                                border(2.px, LineStyle.Solid, Color("#4A90E2"))
                                borderRadius(4.px)
                                fontFamily("'Courier New', monospace")
                                fontSize(14.px)
                            }
                            attr("step", "0.01")
                            value(stockPrice)
                            onInput { event ->
                                stockPrice = event.target.value
                            }
                        })

                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                gap(12.px)
                                justifyContent(JustifyContent.FlexEnd)
                            }
                        }) {
                            Button({
                                style {
                                    backgroundColor(Color("#7F8C8D"))
                                    color(Color("#FFFFFF"))
                                    borderWidth(0.px)
                                    padding(10.px, 20.px)
                                    borderRadius(4.px)
                                    fontFamily("'Courier New', monospace")
                                    fontWeight("bold")
                                    cursor("pointer")
                                    fontSize(14.px)
                                }
                                onClick {
                                    showStockDialog = false
                                    editingStockIndex = null
                                    stockName = ""
                                    stockPrice = ""
                                }
                            }) {
                                Text("Отмена")
                            }
                            Button({
                                style {
                                    backgroundColor(Color("#27AE60"))
                                    color(Color("#FFFFFF"))
                                    borderWidth(0.px)
                                    padding(10.px, 20.px)
                                    borderRadius(4.px)
                                    fontFamily("'Courier New', monospace")
                                    fontWeight("bold")
                                    cursor("pointer")
                                    fontSize(14.px)
                                }
                                onClick {
                                    if (stockName.isBlank() || stockPrice.isBlank()) {
                                        window.alert("Заполните все поля")
                                        return@onClick
                                    }
                                    val price = stockPrice.toDoubleOrNull()
                                    if (price == null || price < 0) {
                                        window.alert("Введите корректную цену")
                                        return@onClick
                                    }
                                    coroutineScope.launch {
                                        try {
                                            isConfigLoading = true
                                            val updatedStocks = stocks.toMutableList()
                                            if (editingStockIndex != null) {
                                                updatedStocks[editingStockIndex!!] = StockConfig(
                                                    name = stockName,
                                                    averagePrice = price
                                                )
                                            } else {
                                                updatedStocks.add(StockConfig(
                                                    name = stockName,
                                                    averagePrice = price
                                                ))
                                            }
                                            val config = AdministrationConfig(
                                                severiteRate = severitRate.toDoubleOrNull() ?: 42.75,
                                                controlLossThreshold = controlLossThreshold.toIntOrNull() ?: 75,
                                                stocks = updatedStocks
                                            )
                                            apiClient.updateConfig(config)
                                            stocks = updatedStocks
                                            showStockDialog = false
                                            editingStockIndex = null
                                            stockName = ""
                                            stockPrice = ""
                                            window.alert("✅ Акция ${if (editingStockIndex != null) "изменена" else "добавлена"}!")
                                        } catch (e: Exception) {
                                            window.alert("❌ Ошибка: ${e.message}")
                                        } finally {
                                            isConfigLoading = false
                                        }
                                    }
                                }
                            }) {
                                Text(if (editingStockIndex != null) "Сохранить" else "Добавить")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Вспомогательный компонент для карточек статистики
@Composable
private fun StatCard(value: String, label: String) {
    Div({
        style {
            backgroundColor(Color("#2C3E50"))
            padding(16.px)
            borderRadius(6.px)
            textAlign("center")
            border(1.px, LineStyle.Solid, Color("#4A90E2"))
        }
    }) {
        P({
            style {
                fontSize(24.px)
                margin(0.px)
                fontWeight("bold")
                color(Color("#FFFFFF"))
            }
        }) {
            Text(value)
        }
        P({
            style {
                margin(0.px)
                marginTop(4.px)
                fontSize(12.px)
                color(Color("#A8D0E6"))
            }
        }) {
            Text(label)
        }
    }
}
