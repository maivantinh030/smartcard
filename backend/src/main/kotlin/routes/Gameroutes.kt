package com.example.routes



import com.example.model.*
import com.example.service.GameService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.gameRoutes() {
    val gameService = GameService()

    route("/games") {

        /**
         * GET /api/v1/games
         * Lấy tất cả games (public endpoint)
         */
        get {
            try {
                val games = gameService.getAllGames()
                call.respond(HttpStatusCode.OK, games)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi lấy danh sách games: ${e.message}"
                ))
            }
        }

        /**
         * GET /api/v1/games/{gameCode}
         * Lấy thông tin 1 game cụ thể
         */
        get("/{gameCode}") {
            try {
                val gameCode = call.parameters["gameCode"]?.toIntOrNull()

                if (gameCode == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Game code không hợp lệ"
                    ))
                    return@get
                }

                val game = gameService.getGame(gameCode)

                if (game != null) {
                    call.respond(HttpStatusCode.OK, game)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(
                        error = "Không tìm thấy game"
                    ))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi lấy thông tin game: ${e.message}"
                ))
            }
        }

        /**
         * POST /api/v1/games
         * Thêm game mới (cần admin token)
         */
        post {
            try {
                val admin = call.getAuthenticatedAdmin()
                if (admin == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                        error = "Cần đăng nhập admin"
                    ))
                    return@post
                }

                val request = call.receive<AddGameRequest>()

                // Validate input
                if (request.gameName.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Tên game không được để trống"
                    ))
                    return@post
                }

                val result = gameService.addGame(request)

                val status = if (result.success) HttpStatusCode.Created else HttpStatusCode.BadRequest
                call.respond(status, result)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi thêm game: ${e.message}"
                ))
            }
        }

        /**
         * PUT /api/v1/games/{gameCode}
         * Cập nhật game (cần admin token)
         */
        put("/{gameCode}") {
            try {
                val admin = call.getAuthenticatedAdmin()
                if (admin == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                        error = "Cần đăng nhập admin"
                    ))
                    return@put
                }

                val gameCode = call.parameters["gameCode"]?.toIntOrNull()

                if (gameCode == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Game code không hợp lệ"
                    ))
                    return@put
                }

                val request = call.receive<UpdateGameRequest>()

                // Validate input
                if (request.gameName.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Tên game không được để trống"
                    ))
                    return@put
                }

                val result = gameService.updateGame(gameCode, request)

                val status = if (result.success) HttpStatusCode.OK else HttpStatusCode.BadRequest
                call.respond(status, result)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi cập nhật game: ${e.message}"
                ))
            }
        }

        /**
         * DELETE /api/v1/games/{gameCode}
         * Xóa game (cần admin token)
         */
        delete("/{gameCode}") {
            try {
                val admin = call.getAuthenticatedAdmin()
                if (admin == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                        error = "Cần đăng nhập admin"
                    ))
                    return@delete
                }

                val gameCode = call.parameters["gameCode"]?.toIntOrNull()

                if (gameCode == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Game code không hợp lệ"
                    ))
                    return@delete
                }

                val result = gameService.deleteGame(gameCode)

                val status = if (result.success) HttpStatusCode.OK else HttpStatusCode.BadRequest
                call.respond(status, result)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi xóa game: ${e.message}"
                ))
            }
        }

        /**
         * PATCH /api/v1/games/{gameCode}/toggle
         * Bật/tắt game (cần admin token)
         */
        patch("/{gameCode}/toggle") {
            try {
                val admin = call.getAuthenticatedAdmin()
                if (admin == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                        error = "Cần đăng nhập admin"
                    ))
                    return@patch
                }

                val gameCode = call.parameters["gameCode"]?.toIntOrNull()

                if (gameCode == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        error = "Game code không hợp lệ"
                    ))
                    return@patch
                }

                val result = gameService.toggleGameStatus(gameCode)

                val status = if (result.success) HttpStatusCode.OK else HttpStatusCode.BadRequest
                call.respond(status, result)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    error = "Lỗi thay đổi trạng thái game: ${e.message}"
                ))
            }
        }
    }
}