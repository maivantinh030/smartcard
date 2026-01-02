package com.example.service

import com.example.database.TransactionHistory
import com.example.database.TransactionType
import com.example.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.stringLiteral
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.TextColumnType
import java.math.BigDecimal

class TransactionService {

    fun record(request: CreateTransactionRequest): ApiResponse {
        val type = try {
            TransactionType.valueOf(request.type.uppercase())
        } catch (_: Exception) {
            return ApiResponse(success = false, message = "Loại giao dịch không hợp lệ")
        }

        val amount = try {
            BigDecimal(request.amount)
        } catch (_: Exception) {
            return ApiResponse(success = false, message = "Số tiền không hợp lệ")
        }

        return transaction {
            try {
                val insertedId = TransactionHistory.insertAndGetId {
                    it[customerId] = request.customerId
                    it[this.type] = type
                    it[gameCode] = request.gameCode
                    it[tickets] = request.tickets
                    it[this.amount] = amount
                    it[balanceAfter] = request.balanceAfter
                    it[status] = request.status ?: "SUCCESS"
                }.value

                ApiResponse(
                    success = true,
                    message = "Ghi nhận giao dịch thành công",
                    data = insertedId.toString()
                )
            } catch (e: Exception) {
                ApiResponse(success = false, message = "Lỗi ghi giao dịch: ${e.message}")
            }
        }
    }

    fun historyByCustomer(customerId: String): List<TransactionDto> {
        return try {
            transaction {
                TransactionHistory.select { TransactionHistory.customerId eq customerId }
                    .orderBy(TransactionHistory.createdAt, SortOrder.DESC)
                    .map {
                        TransactionDto(
                            id = it[TransactionHistory.id].value,
                            customerId = it[TransactionHistory.customerId],
                            type = it[TransactionHistory.type].name,
                            gameCode = it[TransactionHistory.gameCode],
                            tickets = it[TransactionHistory.tickets],
                            amount = it[TransactionHistory.amount].toPlainString(),
                            balanceAfter = it[TransactionHistory.balanceAfter],
                            createdAt = it[TransactionHistory.createdAt].toString()  // Convert Instant to ISO-8601 String
                        )
                    }
            }
        } catch (e: Exception) {
            println("❌ Lỗi query history cho $customerId: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    fun revenueByDay(): List<RevenuePoint> {
        val dayExpr = TransactionHistory.createdAt.date().alias("day")
        val sumExpr = TransactionHistory.amount.sum().alias("total")

        return transaction {
            TransactionHistory.slice(dayExpr, sumExpr)
                .selectAll()
                .groupBy(dayExpr)
                .orderBy(dayExpr, SortOrder.DESC)
                .map {
                    RevenuePoint(
                        label = it[dayExpr].toString(),
                        totalAmount = it[sumExpr]?.toPlainString() ?: "0"
                    )
                }
        }
    }

    fun revenueByMonth(): List<RevenuePoint> {
        val monthExpr = CustomFunction<String>(
            "DATE_FORMAT",
            TextColumnType(),
            TransactionHistory.createdAt,
            stringLiteral("%Y-%m")
        ).alias("month")
        val sumExpr = TransactionHistory.amount.sum().alias("total")

        return transaction {
            TransactionHistory.slice(monthExpr, sumExpr)
                .selectAll()
                .groupBy(monthExpr)
                .orderBy(monthExpr, SortOrder.DESC)
                .map {
                    RevenuePoint(
                        label = it[monthExpr],
                        totalAmount = it[sumExpr]?.toPlainString() ?: "0"
                    )
                }
        }
    }

    fun revenueByGame(): List<GameRevenue> {
        val sumAmount = TransactionHistory.amount.sum().alias("total")
        val sumTickets = TransactionHistory.tickets.sum().alias("tickets")

        return transaction {
            TransactionHistory.slice(TransactionHistory.gameCode, sumAmount, sumTickets)
                .select { TransactionHistory.gameCode.isNotNull() }
                .groupBy(TransactionHistory.gameCode)
                .orderBy(TransactionHistory.gameCode, SortOrder.ASC)
                .map {
                    GameRevenue(
                        gameCode = it[TransactionHistory.gameCode] ?: 0,
                        totalAmount = it[sumAmount]?.toPlainString() ?: "0",
                        totalTickets = it[sumTickets] ?: 0
                    )
                }
        }
    }
}