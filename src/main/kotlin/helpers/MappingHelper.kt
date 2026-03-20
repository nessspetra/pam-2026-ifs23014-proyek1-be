package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.LostFoundItemDAO
import org.delcom.dao.RefreshTokenDAO
import org.delcom.dao.UserDAO
import org.delcom.entities.LostFoundItem
import org.delcom.entities.RefreshToken
import org.delcom.entities.User
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun userDAOToModel(dao: UserDAO) = User(
    dao.id.value.toString(),
    dao.name,
    dao.username,
    dao.password,
    dao.photo,
    dao.createdAt,
    dao.updatedAt
)

fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    dao.id.value.toString(),
    dao.userId.toString(),
    dao.refreshToken,
    dao.authToken,
    dao.createdAt,
)

fun lostFoundItemDAOToModel(dao: LostFoundItemDAO) = LostFoundItem(
    id          = dao.id.value.toString(),
    userId      = dao.userId.toString(),
    type        = dao.type,
    title       = dao.title,
    description = dao.description,
    category    = dao.category,
    location    = dao.location,
    imageUrl    = dao.imageUrl,
    status      = dao.status,
    date        = dao.date.toString(),
    createdAt   = dao.createdAt,
    updatedAt   = dao.updatedAt
)