package ParkCard;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class CryptoManager {
    private AESKey aesKey;
    private Cipher aesCipher;
    private RandomData randomGen;
    private byte[] iv; 

    private byte[] salt;  // Salt ngu nhiên per card, persistent
    private static final short SALT_LENGTH = 16;
    private static final short IV_LENGTH = 16;

    public CryptoManager(byte[] saltBuffer) {
        aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        this.salt = saltBuffer;  // Nhn salt t CardModel
        iv = new byte[IV_LENGTH];
        randomGen.generateData(iv, (short)0, IV_LENGTH);
    }

    public void deriveKeyFromPIN(byte[] pin, short pinOffset, byte pinLen) {
        MessageDigest md = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);
        byte[] temp = JCSystem.makeTransientByteArray((short)32, JCSystem.CLEAR_ON_DESELECT);

        // Hash salt trýc
        md.doFinal(salt, (short)0, SALT_LENGTH, temp, (short)0);

        // Hash tip PIN
        md.doFinal(pin, pinOffset, pinLen, temp, (short)0);
        // Hash li ln na ð tãng ð phc tp
        md.doFinal(temp, (short)0, (short)20, temp, (short)0);

        // Ly 16 bytes ðu làm khóa AES
        aesKey.setKey(temp, (short)0);

        // Xóa temp
        Util.arrayFillNonAtomic(temp, (short)0, (short)32, (byte)0);
    }

    public void encryptInPlace(byte[] data, short offset, short len) {
        short paddedLen = (short)((len + 15) & ~15);
        aesCipher.init(aesKey, Cipher.MODE_ENCRYPT, iv, (short)0, (short)16);
        aesCipher.doFinal(data, offset, paddedLen, data, offset);
    }

    public void decryptInPlace(byte[] data, short offset, short len) {
        short paddedLen = (short)((len + 15) & ~15);
        aesCipher.init(aesKey, Cipher.MODE_DECRYPT, iv, (short)0, (short)16);
        aesCipher.doFinal(data, offset, paddedLen, data, offset);
    }
}