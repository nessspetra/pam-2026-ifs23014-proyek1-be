package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

// Mengubah nama objek dari TodoTable menjadi ItemTable
object ItemTable : UUIDTable("items") {
    // Relasi ke UserTable (User yang memposting laporan)
    val userId = uuid("user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)

    // Data Barang (Penting untuk Fitur Search di Android)
    val title = varchar("title", 100)
    val description = text("description")

    // Tipe: 'LOST' atau 'FOUND' (Penting untuk Fitur Filter di Android)
    val itemType = varchar("item_type", 10)

    // Kategori: 'Elektronik', 'Dokumen', dll (Penting untuk Fitur Filter di Android)
    val category = varchar("category", 50)

    // Lokasi ditemukannya atau hilangnya barang
    val location = text("location")

    // Status: 'OPEN' (Aktif) atau 'RESOLVED' (Sudah Selesai)
    val status = varchar("status", 15).default("OPEN")

    // Gambar barang (Menggantikan cover)
    val image = text("image").nullable()

    // Tanggal Kejadian (Bisa digunakan untuk pengurutan/sorting)
    val eventDate = timestamp("event_date")

    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}