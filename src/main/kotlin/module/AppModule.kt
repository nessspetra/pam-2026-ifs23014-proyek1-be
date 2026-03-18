package org.delcom.module

import org.delcom.repositories.*
import org.delcom.services.AuthService
import org.delcom.services.ItemService
import org.delcom.services.UserService
import org.koin.dsl.module
import io.ktor.server.application.*

fun appModule(application: Application) = module {
    // Ambil konfigurasi dari application.conf
    val baseUrl = application.environment.config
        .property("ktor.app.baseUrl")
        .getString()
        .trimEnd('/')

    val jwtSecret = application.environment.config
        .property("ktor.jwt.secret")
        .getString()

    // 1. User Management
    single<IUserRepository> {
        UserRepository(baseUrl)
    }
    single {
        UserService(get(), get())
    }

    // 2. Token Management (Auth)
    single<IRefreshTokenRepository> {
        RefreshTokenRepository()
    }
    single {
        AuthService(jwtSecret, get(), get())
    }

    // 3. Item Management (Lost & Found - Requirement No 5)
    single<IItemRepository> {
        ItemRepository(baseUrl)
    }
    single {
        ItemService(get(), get())
    }
}