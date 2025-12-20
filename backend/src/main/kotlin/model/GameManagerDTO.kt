package com.example.model

data class GameDto(
    val gameCode: Int,
    val gameName: String,
    val gameDescription: String?,
    val gameImage: String?, // Base64 encoded image
    val ticketPrice: String,
    val isActive: Boolean
)

data class AddGameRequest(
    val gameName: String,
    val gameDescription: String,
    val ticketPrice: String,
    val gameImage: String? = null // Base64 encoded image (optional)
)

data class UpdateGameRequest(
    val gameName: String,
    val gameDescription: String,
    val ticketPrice: String,
    val gameImage: String? = null // Base64 encoded image (optional)
)