package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.User
import java.util.UUID

@Serializable
data class AuthRequest(
    var name: String = "",
    var username: String = "",
    var password: String = "",
    var newPassword: String? = null, // Dibuat nullable karena tidak selalu digunakan
) {
    /**
     * Mengonversi data request ke Map untuk divalidasi oleh ValidatorHelper.
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "username" to username,
            "password" to password,
            "newPassword" to newPassword
        )
    }

    /**
     * Mengonversi data request menjadi objek Entity User.
     * Digunakan saat proses pendaftaran (Register).
     */
    fun toEntity(): User {
        return User(
            id = UUID.randomUUID().toString(), // Generate ID baru di sini
            name = name,
            username = username,
            password = password,
            updatedAt = Clock.System.now(),
            createdAt = Clock.System.now()
        )
    }
}