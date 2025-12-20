package com.example.model



data class AdminLoginRequest(
    val username: String,
    val password: String
)


data class AdminLoginResponse(
    val success: Boolean,
    val token: String? = null,
    val adminInfo: AdminInfo? = null,
    val message: String? = null
)


data class AdminInfo(
    val adminId: String,
    val username: String,
    val fullName: String,
    val role: String
)