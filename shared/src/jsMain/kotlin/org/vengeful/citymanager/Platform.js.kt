package org.vengeful.citymanager



class JsPlatform : Platform {
    override val name: String
        get() = "JS Web Admin Panel Platform"
}

actual fun getPlatform(): Platform = JsPlatform()