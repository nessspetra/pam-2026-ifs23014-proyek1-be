package org.delcom.data

import kotlinx.serialization.Serializable

/**
 * Custom Exception untuk menangani error aplikasi secara global.
 * @param code Kode HTTP Status (misal: 400, 401, 404, 409)
 * @param message Pesan error yang akan ditampilkan di UI Android (Jetpack Compose)
 */
@Serializable
open class AppException(
    val code: Int,
    override val message: String
) : Exception(message)