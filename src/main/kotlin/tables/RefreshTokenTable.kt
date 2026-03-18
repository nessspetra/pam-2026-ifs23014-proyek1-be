package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object RefreshTokenTable : UUIDTable("refresh_tokens") {
    // Menambahkan referensi ke UserTable agar data konsisten
    val userId = uuid("user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val refreshToken = text("refresh_token")
    val authToken = text("auth_token")
    val createdAt = timestamp("created_at")
}