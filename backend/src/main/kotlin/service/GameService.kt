package com.example.service

import com.example.database.Games
import com.example.model.AddGameRequest
import com.example.model.ApiResponse
import com.example.model.GameDto
import com.example.model.UpdateGameRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant
import java.util.Base64

class GameService {

    /**
     * Lấy tất cả games
     */
    fun getAllGames(): List<GameDto> {
        return transaction {
            Games.selectAll().map { game ->
                val imageBlob = game[Games.gameImage]
                val imageBase64 = imageBlob?.let {
                    Base64.getEncoder().encodeToString(it.bytes)
                }
                
                GameDto(
                    gameCode = game[Games.gameCode],
                    gameName = game[Games.gameName],
                    gameDescription = game[Games.gameDescription],
                    gameImage = imageBase64,
                    ticketPrice = game[Games.ticketPrice].toString(),
                    isActive = game[Games.isActive]
                )
            }
        }
    }

    /**
     * Lấy game theo code
     */
    fun getGame(gameCode: Int): GameDto? {
        return transaction {
            Games.select { Games.gameCode eq gameCode }
                .singleOrNull()?.let { game ->
                    val imageBlob = game[Games.gameImage]
                    val imageBase64 = imageBlob?.let {
                        Base64.getEncoder().encodeToString(it.bytes)
                    }
                    
                    GameDto(
                        gameCode = game[Games.gameCode],
                        gameName = game[Games.gameName],
                        gameDescription = game[Games.gameDescription],
                        gameImage = imageBase64,
                        ticketPrice = game[Games.ticketPrice].toString(),
                        isActive = game[Games.isActive]
                    )
                }
        }
    }

    /**
     * Thêm game mới
     */
    fun addGame(request: AddGameRequest): ApiResponse {
        return transaction {
            try {
                // Tìm game code lớn nhất
                val maxGameCode = Games.slice(Games.gameCode.max()).selectAll()
                    .singleOrNull()?.get(Games.gameCode.max()) ?: 1000

                val newGameCode = (maxGameCode ?: 1000) + 1

                // Decode Base64 image if provided
                val imageBlob = request.gameImage?.let { base64 ->
                    try {
                        ExposedBlob(Base64.getDecoder().decode(base64))
                    } catch (e: Exception) {
                        null
                    }
                }

                // Thêm game mới
                Games.insert {
                    it[gameCode] = newGameCode
                    it[gameName] = request.gameName
                    it[gameDescription] = request.gameDescription
                    it[ticketPrice] = BigDecimal(request.ticketPrice)
                    it[gameImage] = imageBlob
                    it[isActive] = true
                    it[createdAt] = Instant.now()
                    it[updatedAt] = Instant.now()
                }

                ApiResponse(
                    success = true,
                    message = "Thêm game thành công",
                    data = newGameCode.toString()
                )
            } catch (e: Exception) {
                ApiResponse(
                    success = false,
                    message = "Lỗi thêm game: ${e.message}"
                )
            }
        }
    }

    /**
     * Cập nhật game
     */
    fun updateGame(gameCode: Int, request: UpdateGameRequest): ApiResponse {
        return transaction {
            try {
                // Decode Base64 image if provided
                val imageBlob = request.gameImage?.let { base64 ->
                    try {
                        ExposedBlob(Base64.getDecoder().decode(base64))
                    } catch (e: Exception) {
                        null
                    }
                }

                val updated = Games.update({ Games.gameCode eq gameCode }) {
                    it[gameName] = request.gameName
                    it[gameDescription] = request.gameDescription
                    it[ticketPrice] = BigDecimal(request.ticketPrice)
                    if (imageBlob != null) {
                        it[gameImage] = imageBlob
                    }
                    it[updatedAt] = Instant.now()
                }

                if (updated > 0) {
                    ApiResponse(
                        success = true,
                        message = "Cập nhật game thành công"
                    )
                } else {
                    ApiResponse(
                        success = false,
                        message = "Không tìm thấy game"
                    )
                }
            } catch (e: Exception) {
                ApiResponse(
                    success = false,
                    message = "Lỗi cập nhật game: ${e.message}"
                )
            }
        }
    }

    /**
     * Xóa game (set isActive = false)
     */
    fun deleteGame(gameCode: Int): ApiResponse {
        return transaction {
            try {
                val deleted = Games.update({ Games.gameCode eq gameCode }) {
                    it[isActive] = false
                    it[updatedAt] = Instant.now()
                }

                if (deleted > 0) {
                    ApiResponse(
                        success = true,
                        message = "Xóa game thành công"
                    )
                } else {
                    ApiResponse(
                        success = false,
                        message = "Không tìm thấy game"
                    )
                }
            } catch (e: Exception) {
                ApiResponse(
                    success = false,
                    message = "Lỗi xóa game: ${e.message}"
                )
            }
        }
    }

    /**
     * Toggle game active status
     */
    fun toggleGameStatus(gameCode: Int): ApiResponse {
        return transaction {
            try {
                val game = Games.select { Games.gameCode eq gameCode }.singleOrNull()

                if (game == null) {
                    return@transaction ApiResponse(
                        success = false,
                        message = "Không tìm thấy game"
                    )
                }

                val newStatus = !game[Games.isActive]

                Games.update({ Games.gameCode eq gameCode }) {
                    it[isActive] = newStatus
                    it[updatedAt] = Instant.now()
                }

                ApiResponse(
                    success = true,
                    message = if (newStatus) "Kích hoạt game thành công" else "Vô hiệu hóa game thành công"
                )
            } catch (e: Exception) {
                ApiResponse(
                    success = false,
                    message = "Lỗi thay đổi trạng thái game: ${e.message}"
                )
            }
        }
    }
}
