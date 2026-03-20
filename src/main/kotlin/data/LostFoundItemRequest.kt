package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.LostFoundItem

@Serializable
data class LostFoundItemRequest(
    var userId: String = "",
    var type: String = "",
    var title: String = "",
    var description: String = "",
    var category: String = "",
    var location: String = "",
    var imageUrl: String? = null,
    var status: String = "ACTIVE",
    var date: String = "",
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId"      to userId,
            "type"        to type,
            "title"       to title,
            "description" to description,
            "category"    to category,
            "location"    to location,
            "imageUrl"    to imageUrl,
            "status"      to status,
            "date"        to date,
        )
    }

    fun toEntity(): LostFoundItem {
        return LostFoundItem(
            userId      = userId,
            type        = type,
            title       = title,
            description = description,
            category    = category,
            location    = location,
            imageUrl    = imageUrl,
            status      = status,
            date        = date,
            updatedAt   = Clock.System.now()
        )
    }
}