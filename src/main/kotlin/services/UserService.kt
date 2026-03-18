package org.delcom.services

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.AppException
import org.delcom.data.AuthRequest
import org.delcom.data.DataResponse
import org.delcom.data.UserResponse
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.helpers.hashPassword
import org.delcom.helpers.verifyPassword
import org.delcom.repositories.IRefreshTokenRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.*

class UserService(
    private val userRepo: IUserRepository,
    private val refreshTokenRepo: IRefreshTokenRepository,
) {
    // Mengambil data user yang login (Requirement No 4: Profil)
    suspend fun getMe(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val response = DataResponse(
            "success",
            "Berhasil mengambil informasi akun saya",
            mapOf(
                "user" to UserResponse(
                    id = user.id,
                    name = user.name,
                    username = user.username,
                    urlPhoto = user.urlPhoto, // Ditambahkan agar Android bisa render gambar
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt,
                ),
            )
        )
        call.respond(response)
    }

    // Mengubah data profil (Nama & Username)
    suspend fun putMe(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("name", "Nama tidak boleh kosong")
        validator.required("username", "Username tidak boleh kosong")
        validator.validate()

        val existUser = userRepo.getByUsername(request.username)
        if (existUser != null && existUser.id != user.id) {
            throw AppException(409, "Username sudah digunakan oleh akun lain!")
        }

        user.username = request.username
        user.name = request.name
        user.updatedAt = kotlinx.datetime.Clock.System.now()

        if (!userRepo.update(user.id, user)) {
            throw AppException(400, "Gagal memperbarui data profil!")
        }

        call.respond(DataResponse("success", "Berhasil mengubah data profil", null))
    }

    // Mengubah photo profile (Requirement No 1 & 4)
    suspend fun putMyPhoto(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        var newPhotoPath: String? = null

        val multipartData = call.receiveMultipart()
        multipartData.forEachPart { part ->
            if (part is PartData.FileItem) {
                val ext = part.originalFileName?.substringAfterLast('.', "")?.let { ".$it" } ?: ""
                val fileName = "USER_${UUID.randomUUID()}$ext"
                val filePath = "uploads/users/$fileName"

                val file = File(filePath)
                file.parentFile.mkdirs()

                part.provider().copyAndClose(file.writeChannel())
                newPhotoPath = filePath
            }
            part.dispose()
        }

        if (newPhotoPath == null) throw AppException(400, "File foto tidak ditemukan!")

        val oldPhotoPath = user.photo
        user.photo = newPhotoPath
        user.updatedAt = kotlinx.datetime.Clock.System.now()

        if (userRepo.update(user.id, user)) {
            // Hapus file lama jika ada
            oldPhotoPath?.let { File(it).takeIf { f -> f.exists() }?.delete() }
            call.respond(DataResponse("success", "Foto profil berhasil diperbarui", null))
        } else {
            throw AppException(400, "Gagal menyimpan foto profil baru")
        }
    }

    // Mengubah password (Security Requirement)
    suspend fun putMyPassword(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("password", "Password lama wajib diisi")
        validator.required("newPassword", "Password baru wajib diisi")
        validator.validate()

        if (!verifyPassword(request.password, user.password)) {
            throw AppException(400, "Password lama tidak sesuai!")
        }

        user.password = hashPassword(request.newPassword!!)
        user.updatedAt = kotlinx.datetime.Clock.System.now()

        if (userRepo.update(user.id, user)) {
            // Paksa logout dari semua perangkat setelah ganti password
            refreshTokenRepo.deleteByUserId(user.id)
            call.respond(DataResponse("success", "Password berhasil diubah, silakan login kembali", null))
        } else {
            throw AppException(400, "Gagal mengubah password")
        }
    }

    // Mengambil file foto secara langsung (Static Content Fallback)
    suspend fun getPhoto(call: ApplicationCall) {
        val userId = call.parameters["id"] ?: throw AppException(400, "ID User tidak valid!")
        val user = userRepo.getById(userId) ?: throw AppException(404, "User tidak ditemukan")

        val photoPath = user.photo ?: throw AppException(404, "User tidak memiliki foto profil")
        val file = File(photoPath)

        if (!file.exists()) throw AppException(404, "File foto tidak ditemukan di server")

        call.respondFile(file)
    }
}