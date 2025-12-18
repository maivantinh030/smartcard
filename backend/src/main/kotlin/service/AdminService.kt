package com.example.service


import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.example.database.AdminAccounts
import com.example.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.time.temporal.ChronoUnit

class AdminService {
    private val jwtSecret = "park-card-jwt-secret-2024"
    private val algorithm = Algorithm.HMAC256(jwtSecret)

    /**
     * Admin login với username/password
     */
    fun login(username: String, password: String): AdminLoginResponse {
        return transaction {
            // Tìm admin trong database
            val admin = AdminAccounts.select {
                AdminAccounts.username eq username and (AdminAccounts.isActive eq true)
            }.singleOrNull()

            // Check admin tồn tại và password đúng
            if (admin == null || !BCrypt.checkpw(password, admin[AdminAccounts.passwordHash])) {
                return@transaction AdminLoginResponse(
                    success = false,
                    message = "Sai tên đăng nhập hoặc mật khẩu"
                )
            }

            // Tạo JWT token
            val token = JWT.create()
                .withClaim("adminId", admin[AdminAccounts.adminId])
                .withClaim("role", admin[AdminAccounts.role].name)
                .withClaim("username", admin[AdminAccounts.username])
                .withExpiresAt(Instant.now().plus(8, ChronoUnit.HOURS)) // 8 tiếng
                .sign(algorithm)

            // Tạo admin info
            val adminInfo = AdminInfo(
                adminId = admin[AdminAccounts.adminId],
                username = admin[AdminAccounts.username],
                fullName = admin[AdminAccounts.fullName],
                role = admin[AdminAccounts.role].name
            )

            AdminLoginResponse(
                success = true,
                token = token,
                adminInfo = adminInfo,
                message = "Đăng nhập thành công"
            )
        }
    }

    /**
     * Verify JWT token và trả về admin info
     */
    fun verifyToken(token: String): AdminInfo? {
        return try {
            val verifier = JWT.require(algorithm).build()
            val decodedJWT = verifier.verify(token)

            val adminId = decodedJWT.getClaim("adminId").asString()
            val username = decodedJWT.getClaim("username").asString()
            val role = decodedJWT.getClaim("role").asString()

            // Double-check admin vẫn active trong DB
            transaction {
                AdminAccounts.select {
                    AdminAccounts.adminId eq adminId and (AdminAccounts.isActive eq true)
                }.singleOrNull()?.let { admin ->
                    AdminInfo(
                        adminId = admin[AdminAccounts.adminId],
                        username = admin[AdminAccounts.username],
                        fullName = admin[AdminAccounts.fullName],
                        role = admin[AdminAccounts.role].name
                    )
                }
            }
        } catch (e: JWTVerificationException) {
            null
        }
    }

    /**
     * Check admin có quyền thực hiện action không
     */
    fun hasPermission(adminRole: String, requiredRole: String): Boolean {
        val roleHierarchy = mapOf(
            "SUPERVISOR" to 4,
            "CASHIER" to 3,
            "OPERATOR" to 2,
            "VIEWER" to 1
        )

        val adminLevel = roleHierarchy[adminRole] ?: 0
        val requiredLevel = roleHierarchy[requiredRole] ?: 0

        return adminLevel >= requiredLevel
    }
}