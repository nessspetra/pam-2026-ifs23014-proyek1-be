package org.delcom.repositories

import org.delcom.entities.LostFoundItem

interface ILostFoundItemRepository {
    suspend fun getAll(userId: String, search: String, type: String, category: String, status: String): List<LostFoundItem>
    suspend fun getById(itemId: String): LostFoundItem?
    suspend fun create(lostFoundItem: LostFoundItem): String
    suspend fun update(userId: String, itemId: String, newItem: LostFoundItem): Boolean
    suspend fun delete(userId: String, itemId: String): Boolean
}