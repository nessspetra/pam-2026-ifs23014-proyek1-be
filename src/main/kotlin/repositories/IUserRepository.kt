package org.delcom.repositories

import org.delcom.entities.User

interface IUserRepository {
    /**
     * Digunakan oleh ServiceHelper untuk memvalidasi token JWT
     * dan menampilkan data di layar Profil (Requirement nomor 4).
     */
    suspend fun getById(userId: String): User?

    /**
     * Digunakan saat proses Login untuk mencari user berdasarkan username.
     */
    suspend fun getByUsername(username: String): User?

    /**
     * Digunakan saat proses Registrasi (Requirement nomor 1: CRUD).
     */
    suspend fun create(user: User): String

    /**
     * Digunakan jika user ingin mengubah foto profil atau nama di layar Profil.
     */
    suspend fun update(id: String, newUser: User): Boolean

    /**
     * Digunakan jika terdapat fitur hapus akun.
     */
    suspend fun delete(id: String): Boolean
}