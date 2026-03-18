package org.delcom.repositories

import org.delcom.entities.Item

interface IItemRepository {
    /**
     * Mendapatkan daftar barang dengan dukungan:
     * 1. Search (berdasarkan judul/lokasi)
     * 2. Filter (berdasarkan itemType: LOST/FOUND atau category)
     * 3. Infinite Scroll (menggunakan limit dan offset)
     */
    suspend fun getAll(
        userId: String? = null,
        search: String? = null,
        itemType: String? = null,
        category: String? = null,
        limit: Int = 10,
        offset: Long = 0
    ): List<Item>

    /**
     * Mendapatkan detail satu barang (Requirement nomor 4: Detail Data)
     */
    suspend fun getById(itemId: String): Item?

    /**
     * Membuat laporan barang baru (Requirement nomor 1: CRUD)
     */
    suspend fun create(item: Item): String

    /**
     * Memperbarui data barang (Requirement nomor 1: CRUD)
     */
    suspend fun update(userId: String, itemId: String, newItem: Item): Boolean

    /**
     * Menghapus data barang (Requirement nomor 1: CRUD)
     */
    suspend fun delete(userId: String, itemId: String): Boolean

    /**
     * Menghitung total data untuk keperluan metadata Paging di Android
     */
    suspend fun count(
        search: String? = null,
        itemType: String? = null,
        category: String? = null
    ): Long
}