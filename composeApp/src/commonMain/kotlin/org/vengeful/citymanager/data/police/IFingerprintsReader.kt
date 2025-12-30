package org.vengeful.citymanager.data.police

expect class FingerprintsReader {
    fun getAvailableFingerprintNumbers(): List<Int>
    fun getNextAvailableFingerprintNumber(): Int?
    suspend fun loadFingerprintImage(number: Int): ByteArray?
    fun getAllFingerprintNumbers(): List<Int>
}

expect fun createFingerprintsReader(): FingerprintsReader

