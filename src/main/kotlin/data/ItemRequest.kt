package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Item
import java.util.UUID
import kotlinx.datetime.Instant

@Serializable
data class ItemRequest(
    var userId: String = "",
    var title: String = "",
    var description: String = "",
    var itemType: String = "LOST", // LOST atau FOUND
    var category: String = "",
    var location: String = "",
    var status: String = "OPEN",   // OPEN atau RESOLVED
    var image: String? = null,
    var eventDate: String? = null, // Tanggal barang hilang/ditemukan
) {
    /**
     * Mengonversi request ke Map untuk divalidasi oleh ValidatorHelper.
     * Digunakan di level Service sebelum data disimpan ke DB.
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "title" to title,
            "description" to description,
            "itemType" to itemType,
            "category" to category,
            "location" to location,
            "status" to status,
            "image" to image,
            "eventDate" to eventDate
        )
    }

    /**
     * Mengonversi request menjadi objek Entity Item.
     * Fungsi ini memastikan data siap diproses oleh Repository.
     */
    fun toEntity(): Item {
        return Item(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            description = description,
            itemType = itemType,
            category = category,
            location = location,
            status = status,
            image = image,
            // PERBAIKAN DI SINI:
            eventDate = if (eventDate != null) Instant.parse(eventDate!!) else Clock.System.now(),
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }
}