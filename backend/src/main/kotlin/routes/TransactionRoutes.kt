package com.example.routes

import com.example.model.ApiResponse
import com.example.model.CreateTransactionRequest
import com.example.model.ErrorResponse
import com.example.model.TransactionsResponse
import com.example.service.TransactionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.transactionRoutes() {
    val service = TransactionService()

    route("/transactions") {
        post("/record") {
            try {
                val request = call.receive<CreateTransactionRequest>()

                if (request.customerId.isBlank() || request.type.isBlank() || request.amount.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "customerId, type, amount không được trống"))
                    return@post
                }

                val result = service.record(request)
                val status = if (result.success) HttpStatusCode.Created else HttpStatusCode.BadRequest
                call.respond(status, result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(error = "Lỗi ghi lịch sử: ${e.message}"))
            }
        }

        get("/history/{customerId}") {
            try {
                val customerId = call.parameters["customerId"]
                if (customerId.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "customerId không hợp lệ"))
                    return@get
                }

                println("🔍 Lấy lịch sử cho customer: $customerId")
                val history = service.historyByCustomer(customerId)
                println("✅ Tìm được ${history.size} giao dịch")
                call.respond(HttpStatusCode.OK, TransactionsResponse(data = history))
            } catch (e: Exception) {
                e.printStackTrace()
                println("❌ Lỗi lấy lịch sử: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(error = "Lỗi lấy lịch sử: ${e.message}"))
            }
        }
    }

    route("/analytics") {
        get("/revenue/day") {
            val admin = call.getAuthenticatedAdmin()
            if (admin == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(error = "Cần đăng nhập admin"))
                return@get
            }

            val data = service.revenueByDay()
            call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to data))
        }

        get("/revenue/month") {
            val admin = call.getAuthenticatedAdmin()
            if (admin == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(error = "Cần đăng nhập admin"))
                return@get
            }

            val data = service.revenueByMonth()
            call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to data))
        }

        get("/revenue/game") {
            val admin = call.getAuthenticatedAdmin()
            if (admin == null) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(error = "Cần đăng nhập admin"))
                return@get
            }

            val data = service.revenueByGame()
            call.respond(HttpStatusCode.OK, mapOf("success" to true, "data" to data))
        }
    }
}