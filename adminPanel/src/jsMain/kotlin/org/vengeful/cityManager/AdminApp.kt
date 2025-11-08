package org.vengeful.cityManager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.vengeful.cityManager.models.RequestLog
import org.vengeful.cityManager.models.ServerStats

@Composable
fun AdminApp() {
    val apiClient = ApiClient()
    val coroutineScope = MainScope()

    // Состояния
    var serverStats by mutableStateOf(ServerStats(0, 0, "00:00:00", "0 MB"))
    var requestLogs by mutableStateOf(emptyList<RequestLog>())
    var isLoading by mutableStateOf(false)

    // Загрузка данных при старте
    coroutineScope.launch {
        isLoading = true
        try {
            serverStats = apiClient.getServerStats()
            requestLogs = apiClient.getRequestLogs()
        } catch (e: Exception) {
            window.alert("Ошибка подключения к серверу: ${e.message}")
        }
        isLoading = false
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
        // Заголовок
        Div({
            style {
                backgroundColor(Color("#34495E"))
                border(2.px, LineStyle.Solid, Color("#4A90E2"))
                borderRadius(8.px)
                padding(20.px)
                marginBottom(16.px)
//                boxShadow(0.px, 4.px, 6.px, Color("rgba(0, 0, 0, 0.3)"))
            }
        }) {
            H1({
                style {
                    marginTop(0.px)
                    textAlign("center")
                    fontSize(24.px)
                    fontWeight("bold")
                }
            }) {
                Text("⚙️ АДМИНИСТРИРОВАНИЕ СИСТЕМЫ ГОСУДАРСТВЕННОГО УПРАВЛЕНИЯ")
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
                    // Карточка статистики - записи
                    StatCard(
                        value = serverStats.personCount.toString(),
                        label = "записей в базе"
                    )

                    // Карточка статистики - подключения
                    StatCard(
                        value = serverStats.activeConnections.toString(),
                        label = "активных подключений"
                    )

                    // Карточка статистики - время работы
                    StatCard(
                        value = serverStats.uptime,
                        label = "время работы"
                    )

                    // Карточка статистики - память
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

            // Управление
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
                    Text("🛠️ УПРАВЛЕНИЕ СИСТЕМОЙ")
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
                            backgroundColor(Color("#4A90E2"))
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
                                try {
                                    val data = apiClient.exportData()

                                    // Простой способ через window.open для JSON данных
                                    val jsonBlob = js("new Blob([data], { type: 'application/json' })")
                                    val jsonUrl = js("URL.createObjectURL(jsonBlob)")
                                    js("window.open(jsonUrl, '_blank')")

                                    // Или альтернатива - показать данные в alert для копирования
                                    // window.alert("Данные для копирования:\\n\\n$data")

                                } catch (e: Exception) {
                                    window.alert("Ошибка при экспорте данных: ${e.message}")
                                }
                            }
                        }
                    }) {
                        Text("💾 Экспорт данных")
                    }

                    Button({
                        style {
                            backgroundColor(Color("#4A90E2"))
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
                                serverStats = apiClient.getServerStats()
                                requestLogs = apiClient.getRequestLogs()
                            }
                        }
                    }) {
                        Text("🔄 Обновить данные")
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