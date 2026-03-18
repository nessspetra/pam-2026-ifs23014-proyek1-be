package org.delcom.data

import kotlinx.serialization.Serializable

/**
 * Wrapper standar untuk semua response API.
 * @param status Biasanya berisi "success" atau "error"
 * @param message Pesan informasi untuk ditampilkan di UI (misal: "Login Berhasil")
 * @param data Data utama (bisa berupa User, Item, List, atau Map)
 */
@Serializable
data class DataResponse<T>(
    val status: String,
    val message: String,
    val data: T? = null
)