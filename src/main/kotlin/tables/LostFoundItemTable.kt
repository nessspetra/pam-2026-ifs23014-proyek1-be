package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object LostFoundItemTable : UUIDTable("todos") {
    val userId      = uuid("user_id")
    val type        = varchar("type", 10)        // "LOST" or "FOUND"
    val title       = varchar("title", 200)
    val description = text("description")
    val category    = varchar("category", 50)
    val location    = varchar("location", 300)
    val imageUrl    = text("image_url").nullable()
    val status      = varchar("status", 20).default("ACTIVE")
    val date        = timestamp("date")
    val createdAt   = timestamp("created_at")
    val updatedAt   = timestamp("updated_at")
}