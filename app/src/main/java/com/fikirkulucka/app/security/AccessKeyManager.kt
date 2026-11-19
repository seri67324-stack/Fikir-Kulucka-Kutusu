package com.fikirkulucka.app.security

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AccessStatus {
    LOCKED_NEEDS_KEY,
    ACTIVE_TRIAL,
    EXPIRED_PERMANENTLY
}

data class AccessUiState(
    val status: AccessStatus = AccessStatus.LOCKED_NEEDS_KEY,
    val remainingMillis: Long = 0L,
    val errorMessage: String? = null
)

class AccessKeyManager(context: Context) {

    companion object {
        val VALID_ACCESS_KEYS: Set<String> = setOf(
            "KULUCKA-9104-A",
            "KULUCKA-3827-B",
            "KULUCKA-6519-C",
            "KULUCKA-2048-D",
            "KULUCKA-7731-E",
            "KULUCKA-5492-F",
            "KULUCKA-8160-G",
            "KULUCKA-4295-H",
            "KULUCKA-1983-J",
            "KULUCKA-6640-K"
        )
        const val TRIAL_DURATION_MILLIS: Long = 5L * 60L * 1000L // 5 dakika

        private const val PREFS_NAME = "fikir_kulucka_access_guard"
        private const val KEY_PERMANENTLY_EXPIRED = "permanently_expired"
        private const val KEY_EXPIRES_AT = "expires_at_millis"
        private const val KEY_MAX_SEEN_TIME = "max_seen_millis"
        private const val KEY_USED_KEYS = "used_keys"
    }

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(evaluateCurrentState())
    val state: StateFlow<AccessUiState> = _state

    fun evaluateCurrentState(errorMsg: String? = null): AccessUiState {
        val now = System.currentTimeMillis()
        val prevMaxSeen = prefs.getLong(KEY_MAX_SEEN_TIME, 0L)
        val maxSeen = maxOf(prevMaxSeen, now)
        prefs.edit().putLong(KEY_MAX_SEEN_TIME, maxSeen).apply()

        if (prefs.getBoolean(KEY_PERMANENTLY_EXPIRED, false)) {
            return AccessUiState(
                status = AccessStatus.EXPIRED_PERMANENTLY,
                remainingMillis = 0L,
                errorMessage = errorMsg
            )
        }

        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        if (expiresAt > 0L) {
            val effectiveNow = maxOf(now, maxSeen)
            val remaining = expiresAt - effectiveNow
            return if (remaining > 0L) {
                AccessUiState(
                    status = AccessStatus.ACTIVE_TRIAL,
                    remainingMillis = remaining,
                    errorMessage = null
                )
            } else {
                prefs.edit().putBoolean(KEY_PERMANENTLY_EXPIRED, true).apply()
                AccessUiState(
                    status = AccessStatus.EXPIRED_PERMANENTLY,
                    remainingMillis = 0L,
                    errorMessage = null
                )
            }
        }

        return AccessUiState(
            status = AccessStatus.LOCKED_NEEDS_KEY,
            remainingMillis = 0L,
            errorMessage = errorMsg
        )
    }

    fun submitKey(rawInput: String) {
        val current = evaluateCurrentState()
        if (current.status == AccessStatus.EXPIRED_PERMANENTLY) {
            _state.value = current
            return
        }

        val normalized = rawInput.trim().uppercase()
        val usedKeys = prefs.getStringSet(KEY_USED_KEYS, emptySet()) ?: emptySet()

        if (normalized in usedKeys) {
            _state.value = current.copy(
                status = AccessStatus.LOCKED_NEEDS_KEY,
                errorMessage = "Bu Key daha önce kullanıldı."
            )
            return
        }

        if (normalized in VALID_ACCESS_KEYS) {
            val now = System.currentTimeMillis()
            val expiresAt = now + TRIAL_DURATION_MILLIS
            val updatedUsed = usedKeys + normalized
            prefs.edit()
                .putStringSet(KEY_USED_KEYS, updatedUsed)
                .putLong(KEY_EXPIRES_AT, expiresAt)
                .putLong(KEY_MAX_SEEN_TIME, maxOf(prefs.getLong(KEY_MAX_SEEN_TIME, 0L), now))
                .putBoolean(KEY_PERMANENTLY_EXPIRED, false)
                .apply()
            _state.value = evaluateCurrentState()
        } else {
            _state.value = current.copy(
                status = AccessStatus.LOCKED_NEEDS_KEY,
                errorMessage = "Geçersiz Key numarası. Lütfen doğru anahtarı gir."
            )
        }
    }

    fun tick() {
        _state.value = evaluateCurrentState()
    }
}
