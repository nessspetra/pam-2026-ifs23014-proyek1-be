package org.delcom.repositories

import org.delcom.dao.UserDAO
import org.delcom.entities.User
import org.delcom.helpers.suspendTransaction
import org.delcom.helpers.userDAOToModel
import org.delcom.tables.UserTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import java.util.*

class UserRepository(private val baseUrl: String) : IUserRepository {

    // Digunakan untuk mengambil data profil di Android (Requirement No 4)
    override suspend fun getById(userId: String): User? = suspendTransaction {
        UserDAO.findById(UUID.fromString(userId))?.let { userDAOToModel(it, baseUrl) }
    }

    // Digunakan saat proses Login (Requirement No 1)
    override suspend fun getByUsername(username: String): User? = suspendTransaction {
        UserDAO.find { UserTable.username eq username }
            .limit(1)
            .firstOrNull()
            ?.let { userDAOToModel(it, baseUrl) }
    }

    // Digunakan saat proses Registrasi (Requirement No 1)
    override suspend fun create(user: User): String = suspendTransaction {
        val userDAO = UserDAO.new(UUID.fromString(user.id)) {
            name = user.name
            username = user.username
            password = user.password
            photo = user.photo
            createdAt = user.createdAt
            updatedAt = user.updatedAt
        }
        userDAO.id.value.toString()
    }

    // Digunakan untuk memperbarui profil atau foto (Requirement No 4)
    override suspend fun update(id: String, newUser: User): Boolean = suspendTransaction {
        val userDAO = UserDAO.findById(UUID.fromString(id))

        if (userDAO != null) {
            userDAO.name = newUser.name
            userDAO.username = newUser.username
            userDAO.password = newUser.password
            userDAO.photo = newUser.photo
            userDAO.updatedAt = newUser.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun delete(id: String): Boolean = suspendTransaction {
        val rowsDeleted = UserTable.deleteWhere {
            UserTable.id eq UUID.fromString(id)
        }
        rowsDeleted >= 1
    }
}