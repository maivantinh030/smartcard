package com.example.service

import java.security.*
import java.security.spec.X509EncodedKeySpec
import java.util.*
import kotlin.random.Random

/**
 * Admin mutual authentication manager
 * - Generates/stores admin RSA keypair
 * - Provisions admin public key to cards
 * - Issues challenges and signs them for card verification
 */
class AdminAuthManager {
    
    private lateinit var adminPrivateKey: PrivateKey
    private lateinit var adminPublicKey: PublicKey
    
    // In-memory cache of card challenges (production: use Redis)
    private val cardChallenges = mutableMapOf<String, ChallengeData>()
    
    data class ChallengeData(
        val challenge: ByteArray,
        val issuedAt: Long,
        val expiresAt: Long
    )
    
    init {
        initializeAdminKeyPair()
    }
    
    /**
     * Initialize or load admin RSA-1024 key pair
     * Production: load from secure storage (HSM, keystore file, etc.)
     */
    private fun initializeAdminKeyPair() {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(1024)
        val keyPair = keyGen.genKeyPair()
        
        adminPrivateKey = keyPair.private
        adminPublicKey = keyPair.public
        
        println("✓ Admin RSA-1024 key pair initialized")
    }
    
    /**
     * Get admin public key in X509 encoded format (for card provisioning)
     * Returns: [exponent (128 bytes)][modulus (128 bytes)]
     */
    fun getAdminPublicKeyComponents(): Pair<ByteArray, ByteArray> {
        val keyFactory = KeyFactory.getInstance("RSA")
        val rsaPublicKey = keyFactory.getKeySpec(adminPublicKey, 
            java.security.spec.RSAPublicKeySpec::class.java)
        
        val exponent = rsaPublicKey.publicExponent.toByteArray()
        val modulus = rsaPublicKey.modulus.toByteArray()
        
        // Pad to 128 bytes (RSA-1024)
        val expPadded = ByteArray(128)
        val modPadded = ByteArray(128)
        
        System.arraycopy(exponent, 0, expPadded, 128 - exponent.size, exponent.size)
        System.arraycopy(modulus, 0, modPadded, 128 - modulus.size, modulus.size)
        
        return Pair(expPadded, modPadded)
    }
    
    /**
     * Get admin public key as PEM format string
     */
    fun getAdminPublicKeyPEM(): String {
        val encoded = adminPublicKey.encoded
        val base64 = Base64.getEncoder().encodeToString(encoded)
        return """
            |-----BEGIN PUBLIC KEY-----
            |${base64.chunked(64).joinToString("\n")}
            |-----END PUBLIC KEY-----
        """.trimMargin()
    }
    
    /**
     * Issue challenge to card
     * Card will sign this challenge with its private key
     */
    fun issueCardChallenge(customerId: String): ByteArray {
        val challenge = Random.nextBytes(32)
        val now = System.currentTimeMillis()
        val expiry = now + 10 * 60 * 1000  // 10 minutes
        
        cardChallenges[customerId] = ChallengeData(challenge, now, expiry)
        
        return challenge
    }
    
    /**
     * Verify card signature over challenge
     * Card must have signed the challenge bytes with its private key
     */
    fun verifyCardSignature(
        customerId: String,
        signature: ByteArray,
        publicKeyService: RSAService
    ): Boolean {
        val challengeData = cardChallenges[customerId] ?: return false
        
        // Check expiry
        if (System.currentTimeMillis() > challengeData.expiresAt) {
            cardChallenges.remove(customerId)
            return false
        }
        
        return try {
            // Get card's public key from database via RSAService
            // (card public key stored when registered)
            val cardPublicKey = publicKeyService.getCustomerPublicKey(customerId) ?: return false
            
            // Verify signature using SHA1withRSA (JavaCard compatible)
            val sig = Signature.getInstance("SHA1withRSA")
            sig.initVerify(cardPublicKey)
            sig.update(challengeData.challenge)
            sig.verify(signature)
        } catch (e: Exception) {
            println("❌ Card signature verification failed: ${e.message}")
            false
        }
    }
    
    /**
     * Sign a challenge for card to verify
     * Card will verify this signature using admin public key we provisioned
     */
    fun signAdminChallenge(challenge: ByteArray): ByteArray {
        return try {
            val sig = Signature.getInstance("SHA1withRSA")
            sig.initSign(adminPrivateKey)
            sig.update(challenge)
            sig.sign()
        } catch (e: Exception) {
            println("❌ Failed to sign admin challenge: ${e.message}")
            ByteArray(0)
        }
    }
    
    /**
     * Clean up expired card challenges
     */
    fun cleanupExpiredChallenges() {
        val now = System.currentTimeMillis()
        cardChallenges.entries.removeAll { (_, data) -> now > data.expiresAt }
    }
    
    /**
     * Get card challenge for testing/debugging
     */
    fun getCardChallenge(customerId: String): ByteArray? {
        return cardChallenges[customerId]?.challenge
    }
}
