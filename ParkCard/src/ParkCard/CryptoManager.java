package ParkCard;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

/**
 * Qun l m hóa/gii m AES-128-CBC
 */
public class CryptoManager {
    private Cipher aesCipher;
    private byte[] aesKey;           // Transient - 16 bytes
    private boolean keyReady;
    private PBKDF2 pbkdf2;
    
    private static final short AES_KEY_SIZE = 16;
    
    public CryptoManager() {
        // Khi to AES cipher CBC mode vi NoPadding (t padding)
        aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        
        // Transient key - clear on deselect
        aesKey = JCSystem.makeTransientByteArray(AES_KEY_SIZE, JCSystem.CLEAR_ON_DESELECT);
        
        keyReady = false;
        pbkdf2 = new PBKDF2();
    }
    
    /**
     * Derive AES key t PIN s dng PBKDF2
     */
    public void deriveKeyFromPIN(byte[] pin, short pinOff, byte pinLen, 
                                  byte[] salt, short saltOff, short saltLen) {
        // To buffer 20 bytes cho output ca PBKDF2
        byte[] derivedKey = JCSystem.makeTransientByteArray((short)20, JCSystem.CLEAR_ON_DESELECT);
        
        // PBKDF2 vi 2000 iterations
        pbkdf2.deriveKey(pin, pinOff, pinLen, 
                         salt, saltOff, saltLen, 
                         (short)2000, 
                         derivedKey, (short)0);
        
        // Ly 16 bytes ðu làm AES-128 key
        Util.arrayCopyNonAtomic(derivedKey, (short)0, aesKey, (short)0, AES_KEY_SIZE);
        keyReady = true;
    }
    
    /**
     * Encrypt data vi AES-128-CBC
     * Data phi ð ðýc padding thành bi s ca 16
     */
    public void encrypt(byte[] plaintext, short ptOff, short ptLen,
                       byte[] ciphertext, short ctOff,
                       byte[] iv, short ivOff) {
        if (!keyReady) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        
        if (ptLen % 16 != 0) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        // To AES Key object
        AESKey aesKeyObj = (AESKey) KeyBuilder.buildKey(
            KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        aesKeyObj.setKey(aesKey, (short)0);
        
        // Init cipher vi IV
        aesCipher.init(aesKeyObj, Cipher.MODE_ENCRYPT, iv, ivOff, (short)16);
        
        // Encrypt
        aesCipher.doFinal(plaintext, ptOff, ptLen, ciphertext, ctOff);
    }
    
    /**
     * Decrypt data vi AES-128-CBC
     */
    public void decrypt(byte[] ciphertext, short ctOff, short ctLen,
                       byte[] plaintext, short ptOff,
                       byte[] iv, short ivOff) {
        if (!keyReady) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        
        if (ctLen % 16 != 0) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        // To AES Key object
        AESKey aesKeyObj = (AESKey) KeyBuilder.buildKey(
            KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        aesKeyObj.setKey(aesKey, (short)0);
        
        // Init cipher vi IV
        aesCipher.init(aesKeyObj, Cipher.MODE_DECRYPT, iv, ivOff, (short)16);
        
        // Decrypt
        aesCipher.doFinal(ciphertext, ctOff, ctLen, plaintext, ptOff);
    }
    
    /**
     * Clear key khi memory (gi khi deselect hoc fail verify)
     */
    public void clearKey() {
        if (keyReady) {
            Util.arrayFillNonAtomic(aesKey, (short)0, AES_KEY_SIZE, (byte)0);
            keyReady = false;
        }
    }
    
    public boolean isKeyReady() {
        return keyReady;
    }
}