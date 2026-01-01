package org.vengeful.citymanager.data.police

actual class FingerprintsReader {
    actual fun getAvailableFingerprintNumbers(): List<Int> {
        // На Android функционал чтения папки fingerprints не реализован
        return emptyList()
    }

    actual fun getNextAvailableFingerprintNumber(): Int? {
        return null
    }

    actual suspend fun loadFingerprintImage(number: Int): ByteArray? {
        return null
    }

    actual fun getAllFingerprintNumbers(): List<Int> {
        return emptyList()
    }
}

actual fun createFingerprintsReader(): FingerprintsReader = FingerprintsReader()


