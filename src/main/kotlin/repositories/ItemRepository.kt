package org.delcom.repositories

import org.delcom.dao.ItemDAO
import org.delcom.entities.Item
import org.delcom.helpers.itemDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.ItemTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class ItemRepository(private val baseUrl: String) : IItemRepository {

    override suspend fun getAll(
        userId: String?,
        search: String?,
        itemType: String?,
        category: String?,
        limit: Int,
        offset: Long
    ): List<Item> = suspendTransaction {
        // Membangun query secara dinamis untuk Search & Filter
        val query = ItemTable.selectAll()

        userId?.let { query.andWhere { ItemTable.userId eq UUID.fromString(it) } }
        itemType?.let { query.andWhere { ItemTable.itemType eq it } }
        category?.let { query.andWhere { ItemTable.category eq it } }

        search?.takeIf { it.isNotBlank() }?.let {
            val keyword = "%${it.lowercase()}%"
            query.andWhere {
                (ItemTable.title.lowerCase() like keyword) or
                        (ItemTable.location.lowerCase() like keyword)
            }
        }

        // Implementasi Infinite Scroll (Limit & Offset) dan Sorting
        ItemDAO.wrapRows(query)
            .orderBy(ItemTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset)
            .map { itemDAOToModel(it, baseUrl) }
    }

    override suspend fun count(search: String?, itemType: String?, category: String?): Long = suspendTransaction {
        val query = ItemTable.selectAll()

        itemType?.let { query.andWhere { ItemTable.itemType eq it } }
        category?.let { query.andWhere { ItemTable.category eq it } }
        search?.takeIf { it.isNotBlank() }?.let {
            val keyword = "%${it.lowercase()}%"
            query.andWhere { (ItemTable.title.lowerCase() like keyword) }
        }

        query.count()
    }

    override suspend fun getById(itemId: String): Item? = suspendTransaction {
        ItemDAO.findById(UUID.fromString(itemId))?.let { itemDAOToModel(it, baseUrl) }
    }

    override suspend fun create(item: Item): String = suspendTransaction {
        val newItem = ItemDAO.new {
            userId = UUID.fromString(item.userId)
            title = item.title
            description = item.description
            itemType = item.itemType
            category = item.category
            location = item.location
            status = item.status
            image = item.image
            eventDate = item.eventDate
            createdAt = item.createdAt
            updatedAt = item.updatedAt
        }
        newItem.id.value.toString()
    }

    override suspend fun update(userId: String, itemId: String, newItem: Item): Boolean = suspendTransaction {
        val itemDAO = ItemDAO.find {
            (ItemTable.id eq UUID.fromString(itemId)) and (ItemTable.userId eq UUID.fromString(userId))
        }.firstOrNull()

        itemDAO?.apply {
            title = newItem.title
            description = newItem.description
            itemType = newItem.itemType
            category = newItem.category
            location = newItem.location
            status = newItem.status
            image = newItem.image
            eventDate = newItem.eventDate
            updatedAt = newItem.updatedAt
        } != null
    }

    override suspend fun delete(userId: String, itemId: String): Boolean = suspendTransaction {
        val rowsDeleted = ItemTable.deleteWhere {
            (id eq UUID.fromString(itemId)) and (ItemTable.userId eq UUID.fromString(userId))
        }
        rowsDeleted >= 1
    }
}