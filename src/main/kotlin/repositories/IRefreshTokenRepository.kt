package org.delcom.repositories

import org.delcom.entities.RefreshToken

interface IRefreshTokenRepository {
    /**
     * Mencari token untuk validasi saat user melakukan refresh session.
     */
    suspend fun getByToken(refreshToken: String, authToken: String): RefreshToken?

    /**
     * Menyimpan token baru setelah user login atau refresh.
     */
    suspend fun create(newRefreshToken: RefreshToken): String

    /**
     * Menghapus token (Logout dari perangkat tertentu).
     */
    suspend fun delete(authToken: String): Boolean

    /**
     * Menghapus semua token milik user (Logout dari semua perangkat).
     */
    suspend fun deleteByUserId(userId: String): Boolean
}