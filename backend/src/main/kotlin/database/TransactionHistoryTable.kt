package com.example.database

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

enum class TransactionType {
    TOPUP,
    PURCHASE
}

object TransactionHistory : LongIdTable("transaction_history") {
    val customerId = varchar("customer_id", 20)
    val type = enumeration("type", TransactionType::class)
    val gameCode = integer("game_code").nullable()
    val tickets = integer("tickets").nullable()
    val amount = decimal("amount", 12, 2)
    val balanceAfter = integer("balance_after").nullable()
    val status = varchar("status", 20).default("SUCCESS")
    val createdAt = timestamp("created_at").default(Instant.now())
}