package com.example.routes


import com.example.model.*
import com.example.service.RSAService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.rsaRoutes() {
    val rsaService = RSAService()

    route("/rsa") {

        /**
         * GET /api/v1/rsa/challenge
         * Tạo challenge để xác thực RSA
         */
        get("/challenge") {
            try {
                val challenge = rsaService.generateChallenge()
                call.respond(HttpStatusCode.OK, challenge)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi tạo challenge: ${e.message}"
                ))
            }
        }

        /**
         * POST /api/v1/rsa/verify
         * Verify RSA signature từ thẻ
         */
        post("/verify") {
            try {
                val request = call.receive<RSAVerifyRequest>()

                // Validate input
                if (request.customerId.isBlank() || request.challenge.isBlank() || request.signature.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Customer ID, challenge và signature không được để trống"
                    ))
                    return@post
                }

                val result = rsaService.verifySignature(request)

                val status = if (result.success) HttpStatusCode.OK else HttpStatusCode.Unauthorized
                call.respond(status, result)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi verify signature: ${e.message}"
                ))
            }
        }

        /**
         * POST /api/v1/rsa/register-key
         * Đăng ký public key cho customer
         */
        post("/register-key") {
            try {
                val request = call.receive<RegisterKeyRequest>()

                // Validate input
                if (request.customerId.isBlank() || request.publicKey.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Customer ID và public key không được để trống"
                    ))
                    return@post
                }

                // Check format public key
                if (!request.publicKey.contains("BEGIN PUBLIC KEY") || !request.publicKey.contains("END PUBLIC KEY")) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Public key phải có định dạng PEM"
                    ))
                    return@post
                }

                val result = rsaService.registerCustomerKey(request)

                val status = if (result.success) HttpStatusCode.Created else HttpStatusCode.BadRequest
                call.respond(status, result)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi đăng ký key: ${e.message}"
                ))
            }
        }

        /**
         * DELETE /api/v1/rsa/deactivate-key/{customerId}
         * Vô hiệu hóa key của customer (cần admin token)
         */
        delete("/deactivate-key/{customerId}") {
            try {
                val admin = call.getAuthenticatedAdmin()
                if (admin == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                        error = "Cần đăng nhập admin"
                    ))
                    return@delete
                }

                val customerId = call.parameters["customerId"]

                if (customerId.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Customer ID không hợp lệ"
                    ))
                    return@delete
                }

                val result = rsaService.deactivateCustomerKey(customerId)

                val status = if (result.success) HttpStatusCode.OK else HttpStatusCode.BadRequest
                call.respond(status, result)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi vô hiệu hóa key: ${e.message}"
                ))
            }
        }

        /**
         * POST /api/v1/rsa/cleanup
         * Cleanup expired challenges (admin only)
         */
        post("/cleanup") {
            try {
                val admin = call.getAuthenticatedAdmin()
                if (admin == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                        error = "Cần đăng nhập admin"
                    ))
                    return@post
                }

                rsaService.cleanupExpiredChallenges()

                call.respond(HttpStatusCode.OK, ApiResponse(
                    success = true,
                    message = "Cleanup challenges thành công"
                ))

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi cleanup: ${e.message}"
                ))
            }
        }
    }
}