package org.delcom.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.data.AppException
import org.delcom.data.AuthRequest
import org.delcom.data.DataResponse
import org.delcom.data.RefreshTokenRequest
import org.delcom.entities.RefreshToken
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.ValidatorHelper
import org.delcom.helpers.hashPassword
import org.delcom.helpers.verifyPassword
import org.delcom.repositories.IRefreshTokenRepository
import org.delcom.repositories.IUserRepository
import java.util.*

class AuthService(
    private val jwtSecret: String,
    private val userRepository: IUserRepository,
    private val refreshTokenRepository: IRefreshTokenRepository,
) {
    // Register (Requirement No 1: Autentikasi)
    suspend fun postRegister(call: ApplicationCall) {
        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("name", "Nama tidak boleh kosong")
        validator.required("username", "Username tidak boleh kosong")
        validator.required("password", "Password tidak boleh kosong")
        validator.validate()

        // Periksa apakah username sudah dipakai (karena kita pakai uniqueIndex di UserTable)
        val existUser = userRepository.getByUsername(request.username)
        if (existUser != null) {
            throw AppException(409, "Akun dengan username ini sudah terdaftar!")
        }

        // Hash password sebelum disimpan ke database
        request.password = hashPassword(request.password)
        val userId = userRepository.create(request.toEntity())

        val response = DataResponse(
            "success",
            "Berhasil melakukan pendaftaran",
            mapOf("userId" to userId)
        )
        call.respond(response)
    }

    // Login (Requirement No 1: Autentikasi)
    suspend fun postLogin(call: ApplicationCall) {
        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("username", "Username tidak boleh kosong")
        validator.required("password", "Password tidak boleh kosong")
        validator.validate()

        val existUser = userRepository.getByUsername(request.username)
            ?: throw AppException(404, "Username atau Password salah!")

        if (!verifyPassword(request.password, existUser.password)) {
            throw AppException(404, "Username atau Password salah!")
        }

        // Generate JWT Token (Berlaku 1 Jam)
        val authToken = generateToken(existUser.id)

        // Hapus token lama untuk keamanan
        refreshTokenRepository.deleteByUserId(existUser.id)

        // Buat refresh token baru
        val strRefreshToken = UUID.randomUUID().toString()
        refreshTokenRepository.create(
            RefreshToken(
                userId = existUser.id,
                authToken = authToken,
                refreshToken = strRefreshToken
            )
        )

        call.respond(DataResponse(
            "success",
            "Berhasil melakukan login",
            mapOf(
                "authToken" to authToken,
                "refreshToken" to strRefreshToken
            )
        ))
    }

    // Refresh Token (Requirement No 3: Persistensi Login dengan DataStore)
    suspend fun postRefreshToken(call: ApplicationCall) {
        val request = call.receive<RefreshTokenRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("refreshToken", "Refresh Token tidak boleh kosong")
        validator.required("authToken", "Auth Token tidak boleh kosong")
        validator.validate()

        val existRefreshToken = refreshTokenRepository.getByToken(
            refreshToken = request.refreshToken,
            authToken = request.authToken
        )

        // Selalu hapus token lama (Rotation Token Policy)
        refreshTokenRepository.delete(request.authToken)

        if (existRefreshToken == null) {
            throw AppException(401, "Sesi berakhir, silakan login kembali.")
        }

        val user = userRepository.getById(existRefreshToken.userId)
            ?: throw AppException(404, "User tidak ditemukan.")

        val newAuthToken = generateToken(user.id)
        val newRefreshToken = UUID.randomUUID().toString()

        refreshTokenRepository.create(
            RefreshToken(
                userId = user.id,
                authToken = newAuthToken,
                refreshToken = newRefreshToken
            )
        )

        call.respond(DataResponse(
            "success",
            "Berhasil melakukan refresh token",
            mapOf(
                "authToken" to newAuthToken,
                "refreshToken" to newRefreshToken
            )
        ))
    }

    // Logout
    suspend fun postLogout(call: ApplicationCall) {
        val request = call.receive<RefreshTokenRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("authToken", "Auth Token tidak boleh kosong")
        validator.validate()

        try {
            val decodedJWT = JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(request.authToken)
            val userId = decodedJWT.getClaim("userId").asString()

            refreshTokenRepository.delete(request.authToken)
            if (userId != null) refreshTokenRepository.deleteByUserId(userId)
        } catch (e: Exception) {
            // Jika token sudah expired, tetap hapus dari repository jika ada
            refreshTokenRepository.delete(request.authToken)
        }

        call.respond(DataResponse("success", "Berhasil logout", null))
    }

    // Helper untuk generate JWT agar kode tidak berulang (DRY)
    private fun generateToken(userId: String): String {
        return JWT.create()
            .withAudience(JWTConstants.AUDIENCE)
            .withIssuer(JWTConstants.ISSUER)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000)) // 1 Jam
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}