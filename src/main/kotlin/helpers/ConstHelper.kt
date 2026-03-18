package org.delcom.helpers

object JWTConstants {
    const val NAME = "auth-jwt"
    const val REALM = "delcom-realm"
    const val ISSUER = "delcom-app"
    const val AUDIENCE = "delcom-user"

    // Rahasia JWT (Sebaiknya gunakan Environment Variable, tapi untuk latihan ini ok)
    const val SECRET = "rahasia-lost-n-found-del"
}

// Tambahkan konstanta untuk keperluan Paging (Requirement Nomor 5)
object AppConstants {
    const val DEFAULT_PAGE_SIZE = "10"
    const val UPLOAD_DIR = "uploads/items/"
    const val USER_PHOTO_DIR = "uploads/users/"
}

// Tambahkan konstanta untuk status dan tipe (Agar tidak typo saat koding)
object ItemConstants {
    const val TYPE_LOST = "LOST"
    const val TYPE_FOUND = "FOUND"
    const val STATUS_OPEN = "OPEN"
    const val STATUS_RESOLVED = "RESOLVED"
}