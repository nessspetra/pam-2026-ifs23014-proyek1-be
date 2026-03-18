package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.ItemRequest // Pastikan Anda sudah membuat ItemRequest
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IItemRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.*

class ItemService(
    private val userRepo: IUserRepository,
    private val itemRepo: IItemRepository
) {
    // Mengambil daftar barang (Home / Daftar Data)
    // Mendukung Search, Filter, dan Infinite Scroll (Requirement No 5)
    suspend fun getAll(call: ApplicationCall) {
        val search = call.request.queryParameters["search"]
        val type = call.request.queryParameters["type"] // LOST atau FOUND
        val category = call.request.queryParameters["category"]

        // Parameter untuk Infinite Scroll
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
        val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?: 0L

        // Jika userId dikirim di query, berarti filter barang milik user tertentu (untuk Profil)
        val filterUserId = call.request.queryParameters["userId"]

        val items = itemRepo.getAll(filterUserId, search, type, category, limit, offset)
        val totalData = itemRepo.count(search, type, category)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar barang",
            mapOf(
                "items" to items,
                "meta" to mapOf(
                    "total" to totalData,
                    "limit" to limit,
                    "offset" to offset,
                    "hasMore" to (offset + items.size < totalData)
                )
            )
        )
        call.respond(response)
    }

    // Mengambil detail barang berdasarkan ID (Requirement No 4)
    suspend fun getById(call: ApplicationCall) {
        val itemId = call.parameters["id"] ?: throw AppException(400, "ID barang tidak valid!")
        val item = itemRepo.getById(itemId) ?: throw AppException(404, "Data barang tidak ditemukan!")

        call.respond(DataResponse("success", "Berhasil mengambil detail barang", mapOf("item" to item)))
    }

    // Menambahkan data barang baru (Requirement No 1: CRUD)
    suspend fun post(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<ItemRequest>()
        request.userId = user.id

        // Validasi input menggunakan ValidatorHelper yang sudah dimodifikasi
        val validator = ValidatorHelper(request.toMap())
        validator.required("title", "Judul barang wajib diisi")
        validator.required("itemType", "Tipe (LOST/FOUND) wajib diisi")
        validator.inList("itemType", listOf("LOST", "FOUND"), "Tipe harus LOST atau FOUND")
        validator.required("category", "Kategori wajib diisi")
        validator.required("location", "Lokasi wajib diisi")
        validator.validate()

        val itemId = itemRepo.create(request.toEntity())
        call.respond(DataResponse("success", "Berhasil melaporkan barang", mapOf("itemId" to itemId)))
    }

    // Upload / Ubah gambar barang
    suspend fun putImage(call: ApplicationCall) {
        val itemId = call.parameters["id"] ?: throw AppException(400, "ID barang tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val oldItem = itemRepo.getById(itemId)
        if (oldItem == null || oldItem.userId != user.id) {
            throw AppException(403, "Anda tidak memiliki akses untuk mengubah data ini")
        }

        var imagePath: String? = null
        val multipartData = call.receiveMultipart()

        multipartData.forEachPart { part ->
            if (part is PartData.FileItem) {
                val ext = part.originalFileName?.substringAfterLast('.', "")?.let { ".$it" } ?: ""
                val fileName = "ITEM_${UUID.randomUUID()}$ext"
                val folder = File("uploads/items")
                if (!folder.exists()) folder.mkdirs()

                val file = File(folder, fileName)
                part.provider().copyAndClose(file.writeChannel())
                imagePath = "uploads/items/$fileName"
            }
            part.dispose()
        }

        if (imagePath != null) {
            // Hapus gambar lama jika ada
            oldItem.image?.let { File(it).takeIf { f -> f.exists() }?.delete() }

            // Update path gambar di database
            val updatedItem = oldItem.copy(image = imagePath)
            itemRepo.update(user.id, itemId, updatedItem)

            call.respond(DataResponse("success", "Gambar berhasil diperbarui", null))
        } else {
            throw AppException(400, "Gagal mengunggah gambar")
        }
    }

    // Menghapus laporan barang (Requirement No 1: CRUD)
    suspend fun delete(call: ApplicationCall) {
        val itemId = call.parameters["id"] ?: throw AppException(400, "ID tidak valid")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val item = itemRepo.getById(itemId)
        if (item == null || item.userId != user.id) {
            throw AppException(404, "Data tidak ditemukan atau akses ditolak")
        }

        if (itemRepo.delete(user.id, itemId)) {
            // Hapus file fisik gambar
            item.image?.let { File(it).takeIf { f -> f.exists() }?.delete() }
            call.respond(DataResponse("success", "Laporan berhasil dihapus", null))
        } else {
            throw AppException(400, "Gagal menghapus laporan")
        }
    }
}