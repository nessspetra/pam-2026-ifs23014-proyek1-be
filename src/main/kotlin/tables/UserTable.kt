package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UserTable : UUIDTable("users") {
    val name = varchar("name", 100)

    // Menambahkan uniqueIndex agar tidak ada username ganda di aplikasi
    val username = varchar("username", 50).uniqueIndex()

    val password = varchar("password", 255)

    // Path relatif file foto di server
    val photo = varchar("photo", 255).nullable()

    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}