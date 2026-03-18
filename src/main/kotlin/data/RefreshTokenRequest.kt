package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.RefreshToken
import java.util.*

@Serializable
data class RefreshTokenRequest(
    var userId: String? = null,
    var refreshToken: String = "",
    var authToken: String = "",
) {
    /**
     * Digunakan oleh ValidatorHelper untuk mengecek apakah token kosong
     * sebelum diproses oleh AuthService.
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "refreshToken" to refreshToken,
            "authToken" to authToken,
        )
    }

    /**
     * Mengonversi request menjadi entity untuk disimpan kembali ke database
     * (Token Rotation) di PostgreSQL.
     */
    fun toEntity(): RefreshToken {
        return RefreshToken(
            id = UUID.randomUUID().toString(),
            userId = userId ?: "",
            refreshToken = refreshToken,
            authToken = authToken,
            createdAt = Clock.System.now()
        )
    }
}