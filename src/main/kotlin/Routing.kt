package org.delcom

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.data.AppException
import org.delcom.data.ErrorResponse
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.parseMessageToMap
import org.delcom.services.ItemService
import org.delcom.services.AuthService
import org.delcom.services.UserService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val itemService: ItemService by inject()
    val authService: AuthService by inject()
    val userService: UserService by inject()

    install(StatusPages) {
        // Tangkap AppException untuk validasi dan error bisnis
        exception<AppException> { call, cause ->
            val dataMap: Map<String, List<String>> = parseMessageToMap(cause.message)

            call.respond(
                status = HttpStatusCode.fromValue(cause.code),
                message = ErrorResponse(
                    status = "fail",
                    message = if (dataMap.isEmpty()) cause.message else "Data yang dikirimkan tidak valid!",
                    data = if (dataMap.isEmpty()) null else dataMap.toString()
                )
            )
        }

        // Tangkap error sistem (500)
        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    status = "error",
                    message = cause.message ?: "Terjadi kesalahan pada server",
                    data = ""
                )
            )
        }
    }

    routing {
        get("/") {
            call.respondText("Delcom Lost & Found API telah berjalan.")
        }

        // Route Auth (Publik)
        route("/auth") {
            post("/login") { authService.postLogin(call) }
            post("/register") { authService.postRegister(call) }
            post("/refresh-token") { authService.postRefreshToken(call) }
            post("/logout") { authService.postLogout(call) }
        }

        // Route yang membutuhkan Token JWT
        authenticate(JWTConstants.NAME) {

            // Route User (Profil - Requirement No 4)
            route("/users") {
                get("/me") { userService.getMe(call) }
                put("/me") { userService.putMe(call) }
                put("/me/password") { userService.putMyPassword(call) }
                put("/me/photo") { userService.putMyPhoto(call) }
            }

            // Route Items (Lost & Found - Requirement No 1 & 5)
            route("/items") {
                get { itemService.getAll(call) } // Mendukung Search, Filter, & Paging
                post { itemService.post(call) }
                get("/{id}") { itemService.getById(call) }
                put("/{id}") { userService.putMe(call) } // Bisa disesuaikan untuk update item
                put("/{id}/image") { itemService.putImage(call) }
                delete("/{id}") { itemService.delete(call) }
            }
        }

        // Route untuk akses gambar (Fallback jika tidak menggunakan Static Files Helper)
        route("/images") {
            get("users/{id}") { userService.getPhoto(call) }
            get("items/{id}") { itemService.getById(call) } // Menampilkan detail via ID
        }
    }
}