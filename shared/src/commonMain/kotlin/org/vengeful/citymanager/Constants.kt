package org.vengeful.citymanager

const val SERVER_PORT = 8080
const val SERVER_HOST = "0.0.0.0"
const val BUILD_VERSION = "0.0.5"


// Routes for navigation
const val ROUTE_MAIN = "main"
const val ROUTE_ADMINISTRATION = "administration"
const val ROUTE_COMMON_LIBRARY = "common_library"
const val ROUTE_COURT = "court"
const val ROUTE_MEDIC = "medic"
const val ROUTE_POLICE = "police"
const val ROUTE_CLICKER = "clicker"
const val ROUTE_BANK = "bank"
const val ROUTE_BACKUP = "backup"

val BUILD_VARIANT = BuildVariant.DEBUG

enum class BuildVariant {
    DEBUG, PROD
}
