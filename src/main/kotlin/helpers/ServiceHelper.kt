package org.delcom.helpers

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.delcom.data.AppException
import org.delcom.entities.User
import org.delcom.repositories.IUserRepository

object ServiceHelper {
    /**
     * Mengambil data user yang sedang login berdasarkan JWT Token.
     * Digunakan untuk memastikan setiap laporan barang (Lost/Found)
     * terikat dengan user yang benar.
     */
    suspend fun getAuthUser(call: ApplicationCall, userRepository: IUserRepository): User {
        val principal = call.principal<JWTPrincipal>()
            ?: throw AppException(401, "Unauthorized")

        val userId = principal
            .payload
            .getClaim("userId")
            .asString()
            ?: throw AppException(401, "Token tidak valid")

        val user = userRepository.getById(userId)
            ?: throw AppException(401, "User tidak valid")

        return user
    }
}