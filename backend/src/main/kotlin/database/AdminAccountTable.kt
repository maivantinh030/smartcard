package com.example.database
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object AdminAccounts : Table("admin_accounts") {
    val adminId = varchar("admin_id", 10)
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val fullName = varchar("full_name", 100)
    val role = enumeration("role", AdminRole::class)
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").default(Instant.now())

    override val primaryKey = PrimaryKey(adminId)
}

enum class AdminRole {
    SUPERVISOR,    // Quyền cao nhất
    CASHIER,       // Nạp tiền, quản lý customer
    OPERATOR,      // Quản lý game, vận hành
    VIEWER         // Chỉ xem thông tin
}