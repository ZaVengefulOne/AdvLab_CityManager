package org.vengeful.citymanager.auth

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

object SessionLockManager {
    private val isLocked = AtomicBoolean(false)
    private val lockUntil = AtomicLong(0)
    private val allowedUserId = AtomicLong(-1)
    private val blacklistedTokens = ConcurrentHashMap<String, Long>()

    fun activateEmergencyShutdown(allowedUserId: Int, durationMinutes: Int) {
        require(durationMinutes in 1..30) { "Duration must be between 1 and 30 minutes" }
        isLocked.set(true)
        lockUntil.set(System.currentTimeMillis() + durationMinutes * 60 * 1000L)
        this.allowedUserId.set(allowedUserId.toLong())
        blacklistedTokens.clear()
    }

    fun isSessionBlocked(userId: Int, token: String?): Boolean {
        if (!isLocked.get()) return false
        
        if (System.currentTimeMillis() > lockUntil.get()) {
            isLocked.set(false)
            lockUntil.set(0)
            allowedUserId.set(-1)
            blacklistedTokens.clear()
            return false
        }

        if (userId == allowedUserId.get().toInt()) {
            return false
        }

        token?.let {
            if (blacklistedTokens.containsKey(it)) {
                val expiration = blacklistedTokens[it] ?: 0
                if (System.currentTimeMillis() < expiration) {
                    return true
                } else {
                    blacklistedTokens.remove(it)
                }
            }
        }

        return true
    }

    fun addTokenToBlacklist(token: String, expirationTime: Long) {
        blacklistedTokens[token] = expirationTime
    }

    fun getRemainingTimeMillis(): Long? {
        if (!isLocked.get()) return null
        val currentTime = System.currentTimeMillis()
        val lockUntilTime = lockUntil.get()
        if (currentTime >= lockUntilTime) {
            isLocked.set(false)
            lockUntil.set(0)
            allowedUserId.set(-1)
            blacklistedTokens.clear()
            return null
        }
        return lockUntilTime - currentTime
    }

    fun getAllowedUserId(): Int? {
        if (!isLocked.get()) return null
        val currentTime = System.currentTimeMillis()
        val lockUntilTime = lockUntil.get()
        if (currentTime >= lockUntilTime) {
            isLocked.set(false)
            lockUntil.set(0)
            allowedUserId.set(-1)
            blacklistedTokens.clear()
            return null
        }
        return allowedUserId.get().toInt()
    }

    fun isUserAllowedToLogin(userId: Int): Boolean {
        if (!isLocked.get()) return true
        val currentTime = System.currentTimeMillis()
        val lockUntilTime = lockUntil.get()
        if (currentTime >= lockUntilTime) {
            isLocked.set(false)
            lockUntil.set(0)
            allowedUserId.set(-1)
            blacklistedTokens.clear()
            return true
        }
        return userId == allowedUserId.get().toInt()
    }

    fun isEmergencyShutdownActive(): Boolean {
        if (isLocked.get() && System.currentTimeMillis() > lockUntil.get()) {
            isLocked.set(false)
            lockUntil.set(0)
            allowedUserId.set(-1)
            blacklistedTokens.clear()
        }
        return isLocked.get()
    }

    fun reset() {
        isLocked.set(false)
        lockUntil.set(0)
        allowedUserId.set(-1)
        blacklistedTokens.clear()
    }
}
