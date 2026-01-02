package org.vengeful.citymanager.data.police

expect class FilePicker {
    suspend fun pickImage(): ByteArray?
}

expect fun createFilePicker(): FilePicker




