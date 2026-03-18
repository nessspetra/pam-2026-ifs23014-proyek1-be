package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    var id: String = UUID.randomUUID().toString(),
    var name: String,
    var username: String, // Pastikan di logic service nanti ini unik
    var password: String,

    // Path relatif file di server (misal: "uploads/user1.jpg")
    var photo: String? = null,

    // URL publik lengkap (misal: "https://api.delcom.org/static/user1.jpg")
    var urlPhoto: String = "",

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)