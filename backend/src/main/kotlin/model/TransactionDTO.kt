package com.example.model

data class TransactionDto(
    val id: Long,
    val customerId: String,
    val type: String,
    val gameCode: Int?,
    val tickets: Int?,
    val amount: String,
    val balanceAfter: Int?,
    val createdAt: String  // ISO-8601 format string (GSON friendly)
)

data class CreateTransactionRequest(
    val customerId: String,
    val type: String,
    val amount: String,
    val tickets: Int? = null,
    val gameCode: Int? = null,
    val balanceAfter: Int? = null,
    val status: String? = null
)

data class TransactionsResponse(
    val success: Boolean = true,
    val data: List<TransactionDto>,
    val message: String? = null
)

data class RevenuePoint(
    val label: String,
    val totalAmount: String
)

data class GameRevenue(
    val gameCode: Int,
    val totalAmount: String,
    val totalTickets: Int
)

data class RevenueResponse(
    val success: Boolean = true,
    val data: List<RevenuePoint>,
    val message: String? = null
)

data class GameRevenueResponse(
    val success: Boolean = true,
    val data: List<GameRevenue>,
    val message: String? = null
)