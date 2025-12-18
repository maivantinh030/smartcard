package com.example.model


data class ChallengeResponse(
    val challenge: String,
    val expiresAt: Long
)


data class RSAVerifyRequest(
    val customerId: String,
    val challenge: String,
    val signature: String
)


data class RSAVerifyResponse(
    val success: Boolean,
    val message: String
)


data class RegisterKeyRequest(
    val customerId: String,
    val publicKey: String
)