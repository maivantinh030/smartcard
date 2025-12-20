package com.example.routes

import com.example.database.AdminAccounts
import com.example.database.AdminRole
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

fun Route.debugRoutes() {

    route("/debug") {

        get("/admin-info") {
            try {
                val adminInfo = transaction {
                    AdminAccounts.select { AdminAccounts.username eq "admin" }
                        .singleOrNull()?.let { admin ->
                            mapOf(
                                "found" to true,
                                "adminId" to admin[AdminAccounts.adminId],
                                "username" to admin[AdminAccounts.username],
                                "fullName" to admin[AdminAccounts.fullName],
                                "role" to admin[AdminAccounts.role].name,
                                "isActive" to admin[AdminAccounts.isActive],
                                "passwordHashLength" to admin[AdminAccounts.passwordHash].length,
                                "passwordHashPrefix" to admin[AdminAccounts.passwordHash].take(10),
                                "passwordHashFormat" to if (admin[AdminAccounts.passwordHash].startsWith("\$2")) "BCrypt" else "Unknown"
                            )
                        } ?: mapOf("found" to false)
                }

                call.respond(HttpStatusCode.OK, adminInfo)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "error" to e.message
                ))
            }
        }

        get("/test-password/{password}") {
            try {
                val testPassword = call.parameters["password"] ?: "admin123"

                val result = transaction {
                    val admin = AdminAccounts.select { AdminAccounts.username eq "admin" }.singleOrNull()

                    if (admin != null) {
                        val storedHash = admin[AdminAccounts.passwordHash]

                        // Test multiple scenarios
                        val tests = mapOf(
                            "bcrypt_check" to try {
                                BCrypt.checkpw(testPassword, storedHash)
                            } catch (e: Exception) {
                                "ERROR: ${e.message}"
                            },
                            "direct_compare" to (testPassword == storedHash),
                            "hash_info" to mapOf(
                                "length" to storedHash.length,
                                "prefix" to storedHash.take(20),
                                "is_bcrypt_format" to storedHash.startsWith("\$2"),
                                "full_hash" to storedHash // TEMPORARY - remove in production
                            )
                        )

                        mapOf(
                            "admin_found" to true,
                            "test_password" to testPassword,
                            "tests" to tests
                        )
                    } else {
                        mapOf(
                            "admin_found" to false,
                            "test_password" to testPassword
                        )
                    }
                }

                call.respond(HttpStatusCode.OK, result)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "error" to e.message
                ))
            }
        }

        get("/reset-admin-password") {
            try {
                val result = transaction {
                    // Delete existing admin
                    AdminAccounts.deleteWhere { AdminAccounts.username eq "admin" }

                    // Create new admin with fresh BCrypt hash
                    val newHash = BCrypt.hashpw("admin123", BCrypt.gensalt())

                    AdminAccounts.insert {
                        it[adminId] = "ADM001"
                        it[username] = "admin"
                        it[passwordHash] = newHash
                        it[fullName] = "System Administrator"
                        it[role] = AdminRole.SUPERVISOR
                        it[isActive] = true
                    }

                    // Test the new hash immediately
                    val testResult = BCrypt.checkpw("admin123", newHash)

                    mapOf(
                        "reset_completed" to true,
                        "new_hash_prefix" to newHash.take(20),
                        "immediate_test" to testResult,
                        "message" to "Admin password reset to 'admin123'"
                    )
                }

                call.respond(HttpStatusCode.OK, result)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "error" to e.message,
                    "stack_trace" to e.stackTraceToString()
                ))
            }
        }
    }
}