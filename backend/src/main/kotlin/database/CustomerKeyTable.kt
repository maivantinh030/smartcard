package com.example.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object CustomerKeys : Table("customer_keys") {
    val customerId = varchar("customer_id", 15)
    val publicKey = text("public_key")
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").default(Instant.now())

    override val primaryKey = PrimaryKey(customerId)
}