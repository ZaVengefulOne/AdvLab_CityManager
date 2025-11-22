package org.vengeful.citymanager.data.backup

internal actual fun saveFile(content: String, filename: String, contentType: String) {
    // На Android функционал сохранения файлов не реализован
    // Можно добавить реализацию через Android Storage Access Framework при необходимости
    throw UnsupportedOperationException("Сохранение файлов поддерживается только на macOS и Windows")
}