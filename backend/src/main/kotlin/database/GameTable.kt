package com.example.database
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Games : Table("games") {
    val gameCode = integer("game_code")
    val gameName = varchar("game_name", 100)
    val gameDescription = text("game_description").nullable()
    val gameImage = blob("game_image").nullable()
    val ticketPrice = decimal("ticket_price", 6, 2).default(10.toBigDecimal())
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(gameCode)
}