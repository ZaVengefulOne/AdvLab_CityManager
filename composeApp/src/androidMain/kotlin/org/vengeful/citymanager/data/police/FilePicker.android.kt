package org.vengeful.citymanager.data.police

actual class FilePicker {
    actual suspend fun pickImage(): ByteArray? {
        // На Android функционал выбора файлов не реализован
        // Можно добавить реализацию через Android Storage Access Framework при необходимости
        throw UnsupportedOperationException("Выбор файлов поддерживается только на desktop")
    }
}

actual fun createFilePicker(): FilePicker = FilePicker()



