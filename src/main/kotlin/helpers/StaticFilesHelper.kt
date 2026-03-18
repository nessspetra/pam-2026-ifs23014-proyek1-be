package org.delcom.helpers

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import java.io.File

/**
 * Mendaftarkan folder "uploads/" sebagai direktori file statis
 * yang dapat diakses melalui URL "/static/...".
 *
 * Contoh akses untuk Lost & Found:
 * File di disk  : uploads/items/barang.png
 * URL publik    : http://host:port/static/items/barang.png
 * File di disk  : uploads/users/profil.png
 * URL publik    : http://host:port/static/users/profil.png
 */
fun Application.configureStaticFiles() {
    val uploadDir = File("uploads")

    // Memastikan folder utama uploads tersedia
    if (!uploadDir.exists()) {
        uploadDir.mkdirs()
    }

    // Opsional: Membuat sub-folder agar penyimpanan lebih rapi
    val itemsDir = File("uploads/items")
    if (!itemsDir.exists()) itemsDir.mkdirs()

    val usersDir = File("uploads/users")
    if (!usersDir.exists()) usersDir.mkdirs()

    routing {
        // Memetakan folder fisik "uploads" ke route "/static"
        staticFiles("/static", uploadDir)
    }
}