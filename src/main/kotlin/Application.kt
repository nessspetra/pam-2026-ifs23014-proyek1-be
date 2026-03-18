package org.delcom

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.delcom.data.ErrorResponse
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.configureDatabases
import org.delcom.helpers.configureStaticFiles
import org.delcom.module.appModule
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    // Memuat Environment Variables dari file .env
    val dotenv = dotenv {
        directory = "."
        ignoreIfMissing = true
    }

    dotenv.entries().forEach {
        System.setProperty(it.key, it.value)
    }

    EngineMain.main(args)
}

fun Application.module() {
    val jwtSecret = environment.config.property("ktor.jwt.secret").getString()

    // 1. Konfigurasi Keamanan (JWT) - Requirement No 1
    install(Authentication) {
        jwt(JWTConstants.NAME) {
            realm = JWTConstants.REALM

            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(JWTConstants.ISSUER)
                    .withAudience(JWTConstants.AUDIENCE)
                    .build()
            )

            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (!userId.isNullOrBlank()) JWTPrincipal(credential.payload) else null
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        status = "fail",
                        message = "Sesi telah berakhir atau token tidak valid",
                        data = null
                    )
                )
            }
        }
    }

    // 2. Konfigurasi CORS (Penting jika testing via web/browser)
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowCredentials = true
    }

    // 3. Serialisasi JSON (Requirement No 5 - Data Handling)
    install(ContentNegotiation) {
        json(Json {
            explicitNulls = false
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    // 4. Dependency Injection (Koin)
    install(Koin) {
        slf4jLogger()
        modules(appModule(this@module))
    }

    // 5. Inisialisasi Helper & Routing
    configureDatabases()     // Koneksi PostgreSQL & Auto-Migration
    configureStaticFiles()   // Akses Gambar Lost & Found (uploads/)
    configureRouting()       // Pendaftaran Endpoint API
}