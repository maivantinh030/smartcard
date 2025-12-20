package com.example.service
import com.example.database.CustomerKeys
import com.example.model.ApiResponse
import com.example.model.ChallengeResponse
import com.example.model.RSAVerifyRequest
import com.example.model.RSAVerifyResponse
import com.example.model.RegisterKeyRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*
import kotlin.random.Random


class RSAService {

    // Store challenges in memory (production nên dùng Redis)
    private val challenges = mutableMapOf<String, Long>()

    /**
     * Tạo challenge ngẫu nhiên
     */
    fun generateChallenge(): ChallengeResponse {
        val challengeBytes = Random.nextBytes(32)
        val challenge = Base64.getEncoder().encodeToString(challengeBytes)
        val expiresAt = System.currentTimeMillis() + 5 * 60 * 1000 // 5 phút

        // Lưu challenge với thời gian expire
        challenges[challenge] = expiresAt

        return ChallengeResponse(
            challenge = challenge,
            expiresAt = expiresAt
        )
    }

    /**
     * Verify RSA signature từ thẻ
     * CHANGED: SHA256withRSA → SHA1withRSA (compatible với JavaCard 2.2.1)
     */
    fun verifySignature(request: RSAVerifyRequest): RSAVerifyResponse {
        return transaction {
            try {
                // Check challenge có hợp lệ và chưa expire không
                val challengeExpiry = challenges[request.challenge]
                if (challengeExpiry == null) {
                    return@transaction RSAVerifyResponse(
                        success = false,
                        message = "Challenge không tồn tại"
                    )
                }

                if (System.currentTimeMillis() > challengeExpiry) {
                    challenges.remove(request.challenge)
                    return@transaction RSAVerifyResponse(
                        success = false,
                        message = "Challenge đã hết hạn"
                    )
                }

                // Lấy public key của customer từ database
                val customerKey = CustomerKeys.select {
                    CustomerKeys.customerId eq request.customerId and (CustomerKeys.isActive eq true)
                }.singleOrNull()

                if (customerKey == null) {
                    return@transaction RSAVerifyResponse(
                        success = false,
                        message = "Không tìm thấy khách hàng hoặc key đã bị vô hiệu hóa"
                    )
                }

                // Parse public key
                val publicKeyPem = customerKey[CustomerKeys.publicKey]
                val publicKey = parsePublicKey(publicKeyPem)

                // Verify signature với SHA1withRSA
                val signature = Signature.getInstance("SHA1withRSA")  // CHANGED: SHA256 → SHA1
                signature.initVerify(publicKey)
                
                // Decode challenge Base64 để lấy bytes gốc (card ký bytes gốc, không ký Base64 string)
                val challengeBytes = Base64.getDecoder().decode(request.challenge)
                signature.update(challengeBytes)

                val signatureBytes = Base64.getDecoder().decode(request.signature)
                val isValid = signature.verify(signatureBytes)

                if (isValid) {
                    // Xóa challenge đã dùng (một lần duy nhất)
                    challenges.remove(request.challenge)
                }

                RSAVerifyResponse(
                    success = isValid,
                    message = if (isValid) "Xác thực thành công" else "Chữ ký không hợp lệ"
                )

            } catch (e: Exception) {
                RSAVerifyResponse(
                    success = false,
                    message = "Lỗi xác thực: ${e.message}"
                )
            }
        }
    }

    /**
     * Đăng ký public key cho customer
     */
    fun registerCustomerKey(request: RegisterKeyRequest): ApiResponse {
        return transaction {
            try {
                // Kiểm tra format public key
                parsePublicKey(request.publicKey)

                // Check customer đã tồn tại chưa
                val existing = CustomerKeys.select {
                    CustomerKeys.customerId eq request.customerId
                }.singleOrNull()

                if (existing != null) {
                    // Update key nếu đã tồn tại
                    CustomerKeys.update({ CustomerKeys.customerId eq request.customerId }) {
                        it[publicKey] = request.publicKey
                        it[isActive] = true
                    }

                    ApiResponse(
                        success = true,
                        message = "Cập nhật public key thành công"
                    )
                } else {
                    // Thêm mới
                    CustomerKeys.insert {
                        it[customerId] = request.customerId
                        it[publicKey] = request.publicKey
                        it[isActive] = true
                    }

                    ApiResponse(
                        success = true,
                        message = "Đăng ký public key thành công"
                    )
                }

            } catch (e: Exception) {
                ApiResponse(
                    success = false,
                    message = "Lỗi đăng ký key: ${e.message}"
                )
            }
        }
    }

    /**
     * Vô hiệu hóa key của customer
     */
    fun deactivateCustomerKey(customerId: String): ApiResponse {
        return transaction {
            try {
                val updated = CustomerKeys.update({ CustomerKeys.customerId eq customerId }) {
                    it[isActive] = false
                }

                if (updated > 0) {
                    ApiResponse(
                        success = true,
                        message = "Vô hiệu hóa key thành công"
                    )
                } else {
                    ApiResponse(
                        success = false,
                        message = "Không tìm thấy customer"
                    )
                }
            } catch (e: Exception) {
                ApiResponse(
                    success = false,
                    message = "Lỗi vô hiệu hóa key: ${e.message}"
                )
            }
        }
    }

    /**
     * Parse PEM format public key thành PublicKey object
     */
    private fun parsePublicKey(publicKeyPem: String): PublicKey {
        val publicKeyContent = publicKeyPem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
            .replace("\n", "")

        val decoded = Base64.getDecoder().decode(publicKeyContent)
        val keySpec = X509EncodedKeySpec(decoded)
        return KeyFactory.getInstance("RSA").generatePublic(keySpec)
    }

    /**
     * Cleanup expired challenges (nên chạy định kỳ)
     */
    fun cleanupExpiredChallenges() {
        val now = System.currentTimeMillis()
        challenges.entries.removeAll { (_, expiry) -> now > expiry }
    }

    /**
     * Get customer public key for admin auth signature verification
     */
    fun getCustomerPublicKey(customerId: String): PublicKey? {
        return transaction {
            try {
                val customerKey = CustomerKeys.select {
                    CustomerKeys.customerId eq customerId and (CustomerKeys.isActive eq true)
                }.singleOrNull()

                if (customerKey != null) {
                    val publicKeyPem = customerKey[CustomerKeys.publicKey]
                    parsePublicKey(publicKeyPem)
                } else {
                    null
                }
            } catch (e: Exception) {
                println("Error getting customer public key: ${e.message}")
                null
            }
        }
    }
}