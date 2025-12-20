package com.example.model
import kotlinx.serialization.Serializable

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: String? = null
)

data class ErrorResponse(
    val success: Boolean = false,
    val error: String,
    val code: String? = null
)