package org.vengeful.citymanager

const val SERVER_PORT = 8080
const val SERVER_HOST = "0.0.0.0"
const val BUILD_VERSION = "0.0.6"


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

val BUILD_VARIANT = BuildVariant.DEBUG

enum class BuildVariant {
    DEBUG, PROD
}
