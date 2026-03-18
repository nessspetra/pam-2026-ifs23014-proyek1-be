package org.delcom.dao

import org.delcom.tables.ItemTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class ItemDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, ItemDAO>(ItemTable)

    // Mapping field dari ItemTable
    var userId by ItemTable.userId
    var title by ItemTable.title
    var description by ItemTable.description

    // Field baru untuk fitur Filter dan Searching
    var itemType by ItemTable.itemType
    var category by ItemTable.category
    var location by ItemTable.location
    var status by ItemTable.status

    // Foto barang
    var image by ItemTable.image

    // Tanggal kejadian untuk pengurutan data
    var eventDate by ItemTable.eventDate

    var createdAt by ItemTable.createdAt
    var updatedAt by ItemTable.updatedAt
}