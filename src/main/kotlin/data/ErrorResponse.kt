package org.delcom.data

import kotlinx.serialization.Serializable

/**
 * Digunakan untuk mengirimkan respon ketika terjadi error/exception.
 * @param status Biasanya bernilai "error" atau "fail"
 * @param message Pesan error utama (misal: "Gagal memproses data")
 * @param data Detail error tambahan, bisa berupa string pesan dari ValidatorHelper
 */
@Serializable
data class ErrorResponse(
    val status: String,
    val message: String,
    val data: String?
)