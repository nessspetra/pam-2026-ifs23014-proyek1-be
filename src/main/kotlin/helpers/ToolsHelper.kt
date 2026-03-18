package org.delcom.helpers

import org.mindrot.jbcrypt.BCrypt

/**
 * Membantu memparsing pesan string menjadi Map.
 * Berguna untuk mengirimkan pesan validasi error (misal: "title: Judul wajib diisi | location: Lokasi tidak boleh kosong")
 */
fun parseMessageToMap(rawMessage: String): Map<String, List<String>> {
    return rawMessage.split("|").mapNotNull { part ->
        val split = part.split(":", limit = 2)
        if (split.size == 2) {
            val key = split[0].trim()
            val value = split[1].trim()
            key to listOf(value)
        } else null
    }.toMap()
}

/**
 * Menghasilkan hash password yang aman sebelum disimpan ke UserTable.
 */
fun hashPassword(password: String): String {
    return BCrypt.hashpw(password, BCrypt.gensalt(12))
}

/**
 * Mencocokkan password input user dengan hash yang ada di database.
 * Digunakan pada proses Login.
 */
fun verifyPassword(password: String, hashed: String): Boolean {
    return try {
        BCrypt.checkpw(password, hashed)
    } catch (e: Exception) {
        false
    }
}