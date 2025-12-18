package com.example.model

// Admin card provisioning request
data class AdminCardProvisionRequest(
    val customerId: String
)

// Admin card provisioning response
data class AdminCardProvisionResponse(
    val success: Boolean,
    val publicKeyExp: String? = null,  // Base64 encoded 128-byte exponent
    val publicKeyMod: String? = null,  // Base64 encoded 128-byte modulus
    val message: String
)

// Admin challenge request
data class AdminChallengeRequest(
    val customerId: String
)

// Admin challenge response
data class AdminChallengeResponse(
    val challenge: String,  // Base64 encoded 32 bytes
    val expiresAt: Long
)

// Admin signature verification request (from card)
data class AdminVerifyCardSignatureRequest(
    val customerId: String,
    val signature: String  // Base64 encoded signature
)

// Admin signature verification response
data class AdminVerifyCardSignatureResponse(
    val success: Boolean,
    val message: String
)

// Admin sign challenge request (card → server)
data class AdminSignChallengeRequest(
    val customerId: String,
    val challenge: String  // Base64 encoded 32 bytes
)

// Admin sign challenge response
data class AdminSignChallengeResponse(
    val signature: String,  // Base64 encoded signature (128 bytes RSA-1024)
    val expiresAt: Long
)
