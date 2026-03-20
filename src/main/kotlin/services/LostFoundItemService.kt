package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.LostFoundItemRequest
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.ILostFoundItemRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.*

class LostFoundItemService(
    private val userRepo: IUserRepository,
    private val itemRepo: ILostFoundItemRepository
) {
    // Mengambil semua daftar item saya
    suspend fun getAll(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val search   = call.request.queryParameters["search"]   ?: ""
        val type     = call.request.queryParameters["type"]     ?: ""
        val category = call.request.queryParameters["category"] ?: ""
        val status   = call.request.queryParameters["status"]   ?: ""

        val items = itemRepo.getAll(user.id, search, type, category, status)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar item",
            mapOf(Pair("items", items))
        )
        call.respond(response)
    }

    // Mengambil item berdasarkan id
    suspend fun getById(call: ApplicationCall) {
        val itemId = call.parameters["id"]
            ?: throw AppException(400, "Data item tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val item = itemRepo.getById(itemId)
        if (item == null || item.userId != user.id) {
            throw AppException(404, "Data item tidak tersedia!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengambil data item",
            mapOf(Pair("item", item))
        )
        call.respond(response)
    }

    // Upload image item
    suspend fun putImage(call: ApplicationCall) {
        val itemId = call.parameters["id"]
            ?: throw AppException(400, "Data item tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = LostFoundItemRequest()
        request.userId = user.id

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" }
                        ?: ""

                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/items/$fileName"

                    withContext(Dispatchers.IO) {
                        val file = File(filePath)
                        file.parentFile.mkdirs()
                        part.provider().copyAndClose(file.writeChannel())
                        request.imageUrl = filePath
                    }
                }
                else -> {}
            }
            part.dispose()
        }

        if (request.imageUrl == null) {
            throw AppException(404, "Image item tidak tersedia!")
        }

        val newFile = File(request.imageUrl!!)
        if (!newFile.exists()) {
            throw AppException(404, "Image item gagal diunggah!")
        }

        val oldItem = itemRepo.getById(itemId)
        if (oldItem == null || oldItem.userId != user.id) {
            throw AppException(404, "Data item tidak tersedia!")
        }

        request.type        = oldItem.type
        request.title       = oldItem.title
        request.description = oldItem.description
        request.category    = oldItem.category
        request.location    = oldItem.location
        request.status      = oldItem.status
        request.date        = oldItem.date

        val isUpdated = itemRepo.update(user.id, itemId, request.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui image item!")
        }

        // Hapus image lama
        if (oldItem.imageUrl != null) {
            val oldFile = File(oldItem.imageUrl!!)
            if (oldFile.exists()) oldFile.delete()
        }

        val response = DataResponse("success", "Berhasil mengubah image item", null)
        call.respond(response)
    }

    // Menambahkan item baru
    suspend fun post(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<LostFoundItemRequest>()
        request.userId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("type",        "Tipe item tidak boleh kosong")
        validator.required("title",       "Judul item tidak boleh kosong")
        validator.required("description", "Deskripsi tidak boleh kosong")
        validator.required("category",    "Kategori tidak boleh kosong")
        validator.required("location",    "Lokasi tidak boleh kosong")
        validator.required("date",        "Tanggal tidak boleh kosong")
        validator.validate()

        val itemId = itemRepo.create(request.toEntity())

        val response = DataResponse(
            "success",
            "Berhasil menambahkan data item",
            mapOf(Pair("itemId", itemId))
        )
        call.respond(response)
    }

    // Mengubah data item
    suspend fun put(call: ApplicationCall) {
        val itemId = call.parameters["id"]
            ?: throw AppException(400, "Data item tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<LostFoundItemRequest>()
        request.userId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("type",        "Tipe item tidak boleh kosong")
        validator.required("title",       "Judul item tidak boleh kosong")
        validator.required("description", "Deskripsi tidak boleh kosong")
        validator.required("category",    "Kategori tidak boleh kosong")
        validator.required("location",    "Lokasi tidak boleh kosong")
        validator.required("status",      "Status tidak boleh kosong")
        validator.required("date",        "Tanggal tidak boleh kosong")
        validator.validate()

        val oldItem = itemRepo.getById(itemId)
        if (oldItem == null || oldItem.userId != user.id) {
            throw AppException(404, "Data item tidak tersedia!")
        }
        request.imageUrl = oldItem.imageUrl

        val isUpdated = itemRepo.update(user.id, itemId, request.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data item!")
        }

        val response = DataResponse("success", "Berhasil mengubah data item", null)
        call.respond(response)
    }

    // Menghapus data item
    suspend fun delete(call: ApplicationCall) {
        val itemId = call.parameters["id"]
            ?: throw AppException(400, "Data item tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val oldItem = itemRepo.getById(itemId)
        if (oldItem == null || oldItem.userId != user.id) {
            throw AppException(404, "Data item tidak tersedia!")
        }

        val isDeleted = itemRepo.delete(user.id, itemId)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data item!")
        }

        // Hapus image jika ada
        if (oldItem.imageUrl != null) {
            val oldFile = File(oldItem.imageUrl!!)
            if (oldFile.exists()) oldFile.delete()
        }

        val response = DataResponse("success", "Berhasil menghapus data item", null)
        call.respond(response)
    }

    // Mengambil image item
    suspend fun getImage(call: ApplicationCall) {
        val itemId = call.parameters["id"]
            ?: throw AppException(400, "Data item tidak valid!")

        val item = itemRepo.getById(itemId)
            ?: return call.respond(HttpStatusCode.NotFound)

        if (item.imageUrl == null) {
            throw AppException(404, "Item belum memiliki image")
        }

        val file = File(item.imageUrl!!)
        if (!file.exists()) {
            throw AppException(404, "Image item tidak tersedia")
        }

        call.respondFile(file)
    }
}