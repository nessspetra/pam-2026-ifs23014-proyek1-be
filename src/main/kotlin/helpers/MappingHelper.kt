package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.ItemDAO
import org.delcom.dao.RefreshTokenDAO
import org.delcom.dao.UserDAO
import org.delcom.entities.Item
import org.delcom.entities.RefreshToken
import org.delcom.entities.User
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun userDAOToModel(dao: UserDAO, baseUrl: String) = User(
    id = dao.id.value.toString(),
    name = dao.name,
    username = dao.username,
    password = dao.password,
    photo = dao.photo,
    urlPhoto = buildImageUrl(baseUrl, dao.photo ?: "uploads/defaults/user.png"),
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    id = dao.id.value.toString(),
    userId = dao.userId.toString(),
    refreshToken = dao.refreshToken,
    authToken = dao.authToken,
    createdAt = dao.createdAt,
)

// Mengubah todoDAOToModel menjadi itemDAOToModel untuk Lost & Found
fun itemDAOToModel(dao: ItemDAO, baseUrl: String) = Item(
    id = dao.id.value.toString(),
    userId = dao.userId.toString(),
    title = dao.title,
    description = dao.description,
    itemType = dao.itemType,
    category = dao.category,
    location = dao.location,
    status = dao.status,
    image = dao.image,
    urlImage = buildImageUrl(baseUrl, dao.image ?: "uploads/defaults/item.png"),
    eventDate = dao.eventDate,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

/**
 * Membangun URL publik gambar dari path relatif.
 * Folder "uploads/" pada path relatif dipetakan ke route "/static/"
 */
fun buildImageUrl(baseUrl: String, pathGambar: String): String {
    // Memastikan prefix "uploads/" ditangani dengan benar
    val relativePath = pathGambar.removePrefix("uploads/").removePrefix("/")
    return "$baseUrl/static/$relativePath"
}