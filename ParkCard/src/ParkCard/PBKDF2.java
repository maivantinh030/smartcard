package ParkCard;

import javacard.security.MessageDigest;
import javacard.framework.Util;
import javacard.framework.JCSystem;

/**
 * PBKDF2-HMAC-SHA1
 * Týõng thích JavaCard 2.2.1
 * Ti ýu cho môi trýng RAM thp
 */
public class PBKDF2 {

    private static final short BLOCK_SIZE = 64;   // SHA-1 block size
    private static final short HASH_SIZE  = 20;   // SHA-1 output size

    private final MessageDigest sha1;

    // Reusable buffers (persistent)
    private final byte[] ipad;
    private final byte[] opad;
    private final byte[] innerHash;

    public PBKDF2() {
        sha1 = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);

        ipad = new byte[BLOCK_SIZE];
        opad = new byte[BLOCK_SIZE];
        innerHash = new byte[HASH_SIZE];
    }

    /**
     * HMAC-SHA1
     */
    private void hmac(byte[] key, short keyOff, short keyLen,
                      byte[] data, short dataOff, short dataLen,
                      byte[] output, short outOff) {

        Util.arrayFillNonAtomic(ipad, (short) 0, BLOCK_SIZE, (byte) 0x00);
        Util.arrayFillNonAtomic(opad, (short) 0, BLOCK_SIZE, (byte) 0x00);

        if (keyLen <= BLOCK_SIZE) {
            Util.arrayCopyNonAtomic(key, keyOff, ipad, (short) 0, keyLen);
            Util.arrayCopyNonAtomic(key, keyOff, opad, (short) 0, keyLen);
        } else {
            // Hash key nu dài hõn block
            sha1.reset();
            sha1.doFinal(key, keyOff, keyLen, ipad, (short) 0);
            Util.arrayCopyNonAtomic(ipad, (short) 0, opad, (short) 0, HASH_SIZE);
        }

        for (short i = 0; i < BLOCK_SIZE; i++) {
            ipad[i] ^= (byte) 0x36;
            opad[i] ^= (byte) 0x5C;
        }

        // Inner hash
        sha1.reset();
        sha1.update(ipad, (short) 0, BLOCK_SIZE);
        sha1.doFinal(data, dataOff, dataLen, innerHash, (short) 0);

        // Outer hash
        sha1.reset();
        sha1.update(opad, (short) 0, BLOCK_SIZE);
        sha1.doFinal(innerHash, (short) 0, HASH_SIZE, output, outOff);
    }

    /**
     * PBKDF2-HMAC-SHA1
     *
     * @param password PIN
     * @param passwordOff offset
     * @param passwordLen length
     * @param salt salt
     * @param saltOff offset
     * @param saltLen length
     * @param iterations s vng lp (khuyên: 1000–3000)
     * @param output buffer output (20 bytes)
     * @param outOff offset
     */
    public void deriveKey(byte[] password, short passwordOff, short passwordLen,
                          byte[] salt, short saltOff, short saltLen,
                          short iterations,
                          byte[] output, short outOff) {

        byte[] u = JCSystem.makeTransientByteArray(HASH_SIZE, JCSystem.CLEAR_ON_DESELECT);
        byte[] result = JCSystem.makeTransientByteArray(HASH_SIZE, JCSystem.CLEAR_ON_DESELECT);

        // salt || 0x00000001
        short saltCounterLen = (short) (saltLen + 4);
        byte[] saltCounter = JCSystem.makeTransientByteArray(saltCounterLen, JCSystem.CLEAR_ON_DESELECT);

        Util.arrayCopyNonAtomic(salt, saltOff, saltCounter, (short) 0, saltLen);
        saltCounter[saltLen] = 0;
        saltCounter[(short) (saltLen + 1)] = 0;
        saltCounter[(short) (saltLen + 2)] = 0;
        saltCounter[(short) (saltLen + 3)] = 1;

        // U1
        hmac(password, passwordOff, passwordLen,
             saltCounter, (short) 0, saltCounterLen,
             u, (short) 0);

        Util.arrayCopyNonAtomic(u, (short) 0, result, (short) 0, HASH_SIZE);

        // U2 ... Uc
        for (short i = 1; i < iterations; i++) {
            hmac(password, passwordOff, passwordLen,
                 u, (short) 0, HASH_SIZE,
                 u, (short) 0);

            for (short j = 0; j < HASH_SIZE; j++) {
                result[j] ^= u[j];
            }
        }

        Util.arrayCopyNonAtomic(result, (short) 0, output, outOff, HASH_SIZE);
    }
}
