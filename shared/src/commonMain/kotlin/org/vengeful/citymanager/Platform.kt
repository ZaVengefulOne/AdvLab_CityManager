package org.vengeful.citymanager

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform