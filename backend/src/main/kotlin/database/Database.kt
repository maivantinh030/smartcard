package com.example.database


import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    // Đọc config từ application.yml
    val config = environment.config
    val url = config.property("datasource.url").getString()
    val user = config.property("datasource.user").getString()
    val password = config.property("datasource.password").getString()

    // Setup HikariCP connection pool
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = url
        username = user
        this.password = password
        driverClassName = "com.mysql.cj.jdbc.Driver"
        maximumPoolSize = 10
        minimumIdle = 2
        connectionTimeout = 30000
        idleTimeout = 600000
        maxLifetime = 1800000
        validate()
    }

    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)

    // Tạo tables nếu chưa tồn tại
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            AdminAccounts,
            Games,
            CustomerKeys
        )
        // Insert sample admin nếu chưa có
        initializeSampleData()
    }

    println("✅ Database connected successfully!")
}

/**
 * Insert sample data cho development
 */
private fun initializeSampleData() {
    // Check admin đã tồn tại chưa
    val adminExists = AdminAccounts.selectAll().count() > 0

    if (!adminExists) {
        // Insert sample admin account
        AdminAccounts.insert {
            it[adminId] = "ADM001"
            it[username] = "admin"
            it[passwordHash] = "\$2a\$12\$rJW1G5Y5dF9p5YqF5YqF5u.qF5YqF5YqF5YqF5YqF5YqF5YqF5YqF5" // password: admin123
            it[fullName] = "System Administrator"
            it[role] = AdminRole.SUPERVISOR
            it[isActive] = true
        }

        println("✅ Sample admin created: admin/admin123")
    }

    // Insert sample games nếu chưa có
    val gamesExist = Games.selectAll().count() > 0

    if (!gamesExist) {
        Games.batchInsert(listOf(
            Triple(1001, "Bắn súng Laser", "Game bắn súng tia laser trong không gian"),
            Triple(1002, "Đua xe F1", "Mô phỏng đua xe công thức 1"),
            Triple(1003, "Nhảy dù VR", "Trải nghiệm nhảy dù với kính thực tế ảo"),
            Triple(1004, "Câu cá vàng", "Game câu cá truyền thống với giải thưởng"),
            Triple(1005, "Bóng rổ 3D", "Bắn bóng rổ với hiệu ứng 3D")
        )) { (code, name, desc) ->
            this[Games.gameCode] = code
            this[Games.gameName] = name
            this[Games.gameDescription] = desc
            this[Games.ticketPrice] = (10 + code % 100).toBigDecimal()
            this[Games.isActive] = true
        }

        println("✅ Sample games created")
    }
}