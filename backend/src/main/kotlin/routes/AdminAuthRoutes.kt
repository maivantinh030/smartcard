package com.example.routes

import com.example.model.*
import com.example.service.AdminAuthManager
import com.example.service.RSAService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.adminAuthRoutes() {
    val adminAuthManager = AdminAuthManager()
    val rsaService = RSAService()

    route("/admin-auth") {

        /**
         * GET /api/v1/admin-auth/public-key
         * Get admin public key for card provisioning (exponent + modulus)
         */
        get("/public-key") {
            try {
                val (exp, mod) = adminAuthManager.getAdminPublicKeyComponents()
                val expB64 = Base64.getEncoder().encodeToString(exp)
                val modB64 = Base64.getEncoder().encodeToString(mod)

                call.respond(HttpStatusCode.OK, mapOf(
                    "exponent" to expB64,
                    "modulus" to modB64
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi lấy admin public key: ${e.message}"
                ))
            }
        }

        /**
         * POST /api/v1/admin-auth/provision-card
         * Provision admin public key to card (via terminal/reader)
         * Frontend calls this, then uses SMartCardManager to send APDUs to card
         */
        post("/provision-card") {
            try {
                val request = call.receive<AdminCardProvisionRequest>()

                if (request.customerId.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Customer ID không được để trống"
                    ))
                    return@post
                }

                val (exp, mod) = adminAuthManager.getAdminPublicKeyComponents()
                val expB64 = Base64.getEncoder().encodeToString(exp)
                val modB64 = Base64.getEncoder().encodeToString(mod)

                call.respond(HttpStatusCode.OK, AdminCardProvisionResponse(
                    success = true,
                    publicKeyExp = expB64,
                    publicKeyMod = modB64,
                    message = "Lay admin public key thanh cong"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi cấp phát admin key: ${e.message}"
                ))
            }
        }

        /**
         * POST /api/v1/admin-auth/card-challenge
         * Issue challenge to be signed by card's private key
         * Card signs this challenge with INS 0x1B (SIGN_CHALLENGE)
         */
        post("/card-challenge") {
            try {
                val request = call.receive<AdminChallengeRequest>()

                if (request.customerId.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Customer ID không được để trống"
                    ))
                    return@post
                }

                val challenge = adminAuthManager.issueCardChallenge(request.customerId)
                val challengeB64 = Base64.getEncoder().encodeToString(challenge)
                val expiresAt = System.currentTimeMillis() + 10 * 60 * 1000

                call.respond(HttpStatusCode.OK, AdminChallengeResponse(
                    challenge = challengeB64,
                    expiresAt = expiresAt
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi tạo challenge: ${e.message}"
                ))
            }
        }

        /**
         * POST /api/v1/admin-auth/verify-card-signature
         * Verify signature from card over challenge
         * Card sent signature via INS 0x1B (after receiving challenge)
         */
        post("/verify-card-signature") {
            try {
                val request = call.receive<AdminVerifyCardSignatureRequest>()

                if (request.customerId.isBlank() || request.signature.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Customer ID va signature khong duoc de trong"
                    ))
                    return@post
                }

                val signatureBytes = Base64.getDecoder().decode(request.signature)
                val ok = adminAuthManager.verifyCardSignature(
                    request.customerId,
                    signatureBytes,
                    rsaService
                )

                if (ok) {
                    call.respond(HttpStatusCode.OK, AdminVerifyCardSignatureResponse(
                        success = true,
                        message = "Xac thuc card thanh cong"
                    ))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, AdminVerifyCardSignatureResponse(
                        success = false,
                        message = "Xac thuc card that bai"
                    ))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi verify card signature: ${e.message}"
                ))
            }
        }

        /**
         * POST /api/v1/admin-auth/admin-challenge
         * Sign admin challenge for card to verify admin authenticity
         * Card calls INS 0x1F to get challenge, then admin signs and card verifies with INS 0x20
         */
        post("/sign-admin-challenge") {
            try {
                val request = call.receive<AdminSignChallengeRequest>()

                if (request.customerId.isBlank() || request.challenge.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Customer ID va challenge khong duoc de trong"
                    ))
                    return@post
                }

                val challengeBytes = Base64.getDecoder().decode(request.challenge)
                val signature = adminAuthManager.signAdminChallenge(challengeBytes)

                if (signature.isEmpty()) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                        error = "Khong the ky challenge"
                    ))
                    return@post
                }

                val signatureB64 = Base64.getEncoder().encodeToString(signature)
                val expiresAt = System.currentTimeMillis() + 5 * 60 * 1000

                call.respond(HttpStatusCode.OK, AdminSignChallengeResponse(
                    signature = signatureB64,
                    expiresAt = expiresAt
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi ký admin challenge: ${e.message}"
                ))
            }
        }

        /**
         * POST /api/v1/admin-auth/cleanup
         * Cleanup expired challenges (admin only)
         */
        post("/cleanup") {
            try {
                val admin = call.getAuthenticatedAdmin()
                if (admin == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                        error = "Khong duoc phep"
                    ))
                    return@post
                }

                adminAuthManager.cleanupExpiredChallenges()

                call.respond(HttpStatusCode.OK, ApiResponse(
                    success = true,
                    message = "Cleanup challenges thanh cong"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi cleanup: ${e.message}"
                ))
            }
        }
    }
}
