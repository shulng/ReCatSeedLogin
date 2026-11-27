package cc.baka9.catseedlogin.bukkit.`object`

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object TwoFactorLimiter {
    private class Attempt(var failedCount: Int = 0, var lastAttempt: Long = 0)
    private val attempts = ConcurrentHashMap<UUID, Attempt>()
    @JvmStatic fun recordFailure(uuid: UUID?, maxAttempts: Int, windowMillis: Long): Boolean {
        if (uuid == null || maxAttempts <= 0) return false
        val attempt = attempts.computeIfAbsent(uuid) { Attempt() }
        val now = System.currentTimeMillis()
        synchronized(attempt) {
            if (attempt.lastAttempt > 0 && now - attempt.lastAttempt > windowMillis) attempt.failedCount = 0
            attempt.failedCount++; attempt.lastAttempt = now
            return attempt.failedCount >= maxAttempts
        }
    }
    @JvmStatic fun recordSuccess(uuid: UUID?) { if (uuid != null) attempts.remove(uuid) }
    @JvmStatic fun getFailedCount(uuid: UUID?): Int {
        val attempt = uuid?.let { attempts[it] } ?: return 0
        synchronized(attempt) { return attempt.failedCount }
    }
    @JvmStatic fun sweep(windowMillis: Long) {
        val now = System.currentTimeMillis()
        attempts.entries.removeIf { (_, attempt) -> synchronized(attempt) { attempt.lastAttempt > 0 && now - attempt.lastAttempt > windowMillis } }
    }
}
