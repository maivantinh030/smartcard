package com.example.routes

import com.example.model.AdminInfo
import com.example.model.AdminLoginRequest
import com.example.model.ErrorResponse
import com.example.service.AdminService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes() {
    val adminService = AdminService()

    route("/admin") {

        /**
         * POST /api/v1/admin/login
         * Admin login với username/password
         */
        post("/login") {
            try {
                val request = call.receive<AdminLoginRequest>()

                // Validate input
                if (request.username.isBlank() || request.password.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Username và password không được để trống"
                    )
                    )
                    return@post
                }

                val result = adminService.login(request.username, request.password)

                val status = if (result.success) HttpStatusCode.OK else HttpStatusCode.Unauthorized
                call.respond(status, result)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi hệ thống: ${e.message}"
                ))
            }
        }

        /**
         * GET /api/v1/admin/verify-token
         * Verify JWT token và trả về thông tin admin
         */
        get("/verify-token") {
            try {
                val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")

                if (token.isNullOrBlank()) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                        error = "Missing Authorization header"
                    ))
                    return@get
                }

                val adminInfo = adminService.verifyToken(token)

                if (adminInfo != null) {
                    call.respond(HttpStatusCode.OK, mapOf(
                        "valid" to true,
                        "admin" to adminInfo
                    ))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                        error = "Token không hợp lệ hoặc đã hết hạn"
                    ))
                }

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi verify token: ${e.message}"
                ))
            }
        }

        /**
         * GET /api/v1/admin/profile
         * Lấy thông tin profile admin hiện tại
         */
        get("/profile") {
            try {
                val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")

                if (token.isNullOrBlank()) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                        error = "Missing Authorization header"
                    ))
                    return@get
                }

                val adminInfo = adminService.verifyToken(token)

                if (adminInfo != null) {
                    call.respond(HttpStatusCode.OK, adminInfo)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                        error = "Token không hợp lệ"
                    ))
                }

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi lấy profile: ${e.message}"
                ))
            }
        }
    }
}

/**
 * Extension function để verify admin token trong các routes khác
 */
suspend fun ApplicationCall.getAuthenticatedAdmin(): AdminInfo? {
    val adminService = AdminService()
    val token = request.headers["Authorization"]?.removePrefix("Bearer ")

    return if (token != null) {
        adminService.verifyToken(token)
    } else {
        null
    }
}