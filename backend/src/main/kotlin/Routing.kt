package com.example

import com.example.routes.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {

        // Health check endpoint
        get("/") {
            call.respondText("Park Card System API v1.0 - Running! 🚀")
        }

        get("/health") {
            call.respondText("OK")
        }

        // Test route để debug
        get("/test") {
            call.respondText("Test route works!")
        }

        debugRoutes()
        // API v1 routes
        route("/api/v1") {

            // Test admin route trực tiếp
            get("/admin/test") {
                call.respondText("Admin route works!")
            }

            adminRoutes()
            adminAuthRoutes()
            gameRoutes()
            rsaRoutes()
            transactionRoutes()
        }

        // Static files (nếu cần serve frontend)
        staticResources("/static", "static")
    }
}