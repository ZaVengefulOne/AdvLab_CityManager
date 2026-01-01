package org.vengeful.citymanager.data.backup

internal actual fun saveFile(content: String, filename: String, contentType: String) {
    // На Android используется AndroidBackupInteractor с Context
    throw UnsupportedOperationException("Use AndroidBackupInteractor instead")
}