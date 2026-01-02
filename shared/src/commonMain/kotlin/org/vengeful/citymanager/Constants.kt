package org.vengeful.citymanager

// Версия сборки
const val BUILD_VERSION = "1.0.0"

// Конфигурация сервера - значения по умолчанию
// Могут быть переопределены через переменные окружения при сборке
const val SERVER_HOST = "localhost"
const val SERVER_PORT = 8080
const val SERVER_PROTOCOL = "http"

// Базовый URL сервера (для клиентов)
val SERVER_BASE_URL: String
    get() = "$SERVER_PROTOCOL://$SERVER_HOST:$SERVER_PORT"

// Для Android эмулятора
const val SERVER_ADDRESS_DEBUG = "10.0.2.2"

val SERVER_ANDROID_URL: String
    get() = "$SERVER_PROTOCOL://$SERVER_ADDRESS_DEBUG:$SERVER_PORT"

// Routes for navigation
const val ROUTE_MAIN = "main"
const val ROUTE_ADMINISTRATION = "administration"
const val ROUTE_COMMON_LIBRARY = "common_library"
const val ROUTE_LIBRARY_ARTICLE = "library_article"
const val ROUTE_COURT = "court"
const val ROUTE_MEDIC = "medic"
const val ROUTE_MEDIC_ORDERS = "medic_orders"
const val ROUTE_POLICE = "police"
const val ROUTE_CLICKER = "clicker"
const val ROUTE_BANK = "bank"
const val ROUTE_BACKUP = "backup"
const val ROUTE_MY_BANK = "my_bank"
const val ROUTE_STOCKS = "stocks"
const val ROUTE_NEWS = "news"
const val ROUTE_NEWS_ITEM = "news_item"
const val ROUTE_NIIS = "niis"
const val ROUTE_NIIS_CLEANING = "niis_cleaning"
const val ROUTE_USERS_AND_PERSONS = "users_and_persons"

// Пароль для управления персоналом предприятий
const val PERSONNEL_MANAGEMENT_PASSWORD = "196482"

val BUILD_VARIANT = BuildVariant.DEBUG

enum class BuildVariant {
    DEBUG, PROD, ANDROID
}
