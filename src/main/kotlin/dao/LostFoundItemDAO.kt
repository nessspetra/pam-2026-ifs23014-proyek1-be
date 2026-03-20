package org.delcom.dao

import org.delcom.tables.LostFoundItemTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID


class LostFoundItemDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, LostFoundItemDAO>(LostFoundItemTable)

    var userId      by LostFoundItemTable.userId
    var type        by LostFoundItemTable.type
    var title       by LostFoundItemTable.title
    var description by LostFoundItemTable.description
    var category    by LostFoundItemTable.category
    var location    by LostFoundItemTable.location
    var imageUrl    by LostFoundItemTable.imageUrl
    var status      by LostFoundItemTable.status
    var date        by LostFoundItemTable.date
    var createdAt   by LostFoundItemTable.createdAt
    var updatedAt   by LostFoundItemTable.updatedAt
}