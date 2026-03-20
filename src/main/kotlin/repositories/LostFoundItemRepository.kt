package org.delcom.repositories

import org.delcom.dao.LostFoundItemDAO
import org.delcom.entities.LostFoundItem
import org.delcom.helpers.suspendTransaction
import org.delcom.helpers.lostFoundItemDAOToModel
import org.delcom.tables.LostFoundItemTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.*

class LostFoundItemRepository : ILostFoundItemRepository {

    override suspend fun getAll(
        userId: String,
        search: String,
        type: String,
        category: String,
        status: String
    ): List<LostFoundItem> = suspendTransaction {
        var query = LostFoundItemTable.userId eq UUID.fromString(userId)

        if (type.isNotBlank())     query = query and (LostFoundItemTable.type eq type)
        if (category.isNotBlank()) query = query and (LostFoundItemTable.category eq category)
        if (status.isNotBlank())   query = query and (LostFoundItemTable.status eq status)

        if (search.isBlank()) {
            LostFoundItemDAO
                .find { query }
                .orderBy(LostFoundItemTable.createdAt to SortOrder.DESC)
                .map(::lostFoundItemDAOToModel)
        } else {
            val keyword = "%${search.lowercase()}%"
            LostFoundItemDAO
                .find { query and (LostFoundItemTable.title.lowerCase() like keyword) }
                .orderBy(LostFoundItemTable.title to SortOrder.ASC)
                .map(::lostFoundItemDAOToModel)
        }
    }

    override suspend fun getById(itemId: String): LostFoundItem? = suspendTransaction {
        LostFoundItemDAO
            .find {
                LostFoundItemTable.id eq UUID.fromString(itemId)
            }
            .limit(1)
            .map(::lostFoundItemDAOToModel)
            .firstOrNull()
    }

    override suspend fun create(lostFoundItem: LostFoundItem): String = suspendTransaction {
        val dao = LostFoundItemDAO.new {
            userId      = UUID.fromString(lostFoundItem.userId)
            type        = lostFoundItem.type
            title       = lostFoundItem.title
            description = lostFoundItem.description
            category    = lostFoundItem.category
            location    = lostFoundItem.location
            imageUrl    = lostFoundItem.imageUrl
            status      = lostFoundItem.status
            date        = kotlinx.datetime.Instant.parse(lostFoundItem.date)
            createdAt   = lostFoundItem.createdAt
            updatedAt   = lostFoundItem.updatedAt
        }
        dao.id.value.toString()
    }

    override suspend fun update(userId: String, itemId: String, newItem: LostFoundItem): Boolean = suspendTransaction {
        val dao = LostFoundItemDAO
            .find {
                (LostFoundItemTable.id eq UUID.fromString(itemId)) and
                        (LostFoundItemTable.userId eq UUID.fromString(userId))
            }
            .limit(1)
            .firstOrNull()

        if (dao != null) {
            dao.type        = newItem.type
            dao.title       = newItem.title
            dao.description = newItem.description
            dao.category    = newItem.category
            dao.location    = newItem.location
            dao.imageUrl    = newItem.imageUrl
            dao.status      = newItem.status
            dao.date        = kotlinx.datetime.Instant.parse(newItem.date)
            dao.updatedAt   = newItem.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun delete(userId: String, itemId: String): Boolean = suspendTransaction {
        val rowsDeleted = LostFoundItemTable.deleteWhere {
            (LostFoundItemTable.id eq UUID.fromString(itemId)) and
                    (LostFoundItemTable.userId eq UUID.fromString(userId))
        }
        rowsDeleted >= 1
    }
}