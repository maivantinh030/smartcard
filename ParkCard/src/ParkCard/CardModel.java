package ParkCard;
import javacard.framework.*;
import javacard.security.*;

public class CardModel {
    // K�ch th�c mi (sau khi padding cho AES blocks)
    private static final short LEN_CUSTOMER_ID = 16;      // 15  16 (1 block)
    private static final short LEN_NAME = 64;             // 50  64 (4 blocks)
    private static final short LEN_DOB = 16;              // 10  16 (1 block)
    private static final short LEN_PHONE = 16;            // 10  16 (1 block)
    private static final short LEN_BALANCE = 16;          // 2  16 (1 block)
    private static final short MAX_PHOTO_SIZE = 8000;
    
    private static final short IV_SIZE = 16;
    private static final short SALT_SIZE = 16;

    private static final short MAX_GAMES = 50;
    private static final short GAME_ENTRY_SIZE = 3;
    private static final short GAME_LIST_SIZE = 160;

    private static final short MAX_BALANCE = 30000;

    // Encrypted fields
    byte[] customerID;
    byte[] name;
    byte[] dateOfBirth;
    byte[] phoneNumber;
    byte[] balance;  // �i t short th�nh byte array
    
    // Photo kh�ng encrypt
    byte[] customerPhoto;
    short photoLength;
    short photoWriteOffset;
    
    // Game list kh�ng encrypt
    byte[] gameList;
    short[] gameCountRef;
    
    // Crypto materials
    byte[] iv;       // IV chung cho tt c fields
    byte[] salt;     // Salt cho PBKDF2
    
    boolean dataReady;
    boolean dataEncrypted;  // Flag � bit data � ��c encrypt ch�a
    
    // Transient buffers cho decrypt
    private byte[] tempDecryptBuffer;

    public CardModel() {
        customerID = new byte[LEN_CUSTOMER_ID];
        name = new byte[LEN_NAME];
        dateOfBirth = new byte[LEN_DOB];
        phoneNumber = new byte[LEN_PHONE];
        balance = new byte[LEN_BALANCE];
        
        customerPhoto = new byte[MAX_PHOTO_SIZE];
        photoLength = 0;
        photoWriteOffset = 0;
        
        gameList = new byte[GAME_LIST_SIZE];
        gameCountRef = new short[1];
        gameCountRef[0] = 0;
        
        // Generate IV v� Salt mt ln duy nht
        iv = new byte[IV_SIZE];
        salt = new byte[SALT_SIZE];
        generateIVAndSalt();
        
        dataReady = false;
        dataEncrypted = false;
        
        // Transient buffer cho decrypt operations
        tempDecryptBuffer = JCSystem.makeTransientByteArray((short)64, JCSystem.CLEAR_ON_DESELECT);
    }
    
    /**
     * Generate IV v� Salt ngu nhi�n (ch 1 ln l�c khi to)
     */
    private void generateIVAndSalt() {
        RandomData rng = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        rng.generateData(iv, (short)0, IV_SIZE);
        rng.generateData(salt, (short)0, SALT_SIZE);
    }

    public boolean isDataReady() { return dataReady; }
    public void setDataReady(boolean r) { dataReady = r; }
    public byte[] getIV() { return iv; }
    public byte[] getSalt() { return salt; }
    public boolean isDataEncrypted() { return dataEncrypted; }

    /**
     * Write customer info - data s ��c encrypt bi CryptoManager
     * Input data � ��c pad ��ng k�ch th�c
     */
    public void writeCustomerInfo(APDU apdu, CryptoManager crypto) {
        byte[] buf = apdu.getBuffer();
        apdu.setIncomingAndReceive();
        short offset = ISO7816.OFFSET_CDATA;
        
        // Encrypt tng field
        crypto.encrypt(buf, offset, LEN_CUSTOMER_ID, customerID, (short)0, iv, (short)0);
        offset += LEN_CUSTOMER_ID;
        
        crypto.encrypt(buf, offset, LEN_NAME, name, (short)0, iv, (short)0);
        offset += LEN_NAME;
        
        crypto.encrypt(buf, offset, LEN_DOB, dateOfBirth, (short)0, iv, (short)0);
        offset += LEN_DOB;
        
        crypto.encrypt(buf, offset, LEN_PHONE, phoneNumber, (short)0, iv, (short)0);
        
        dataEncrypted = true;
    }

    /**
     * Read all data - decrypt tng field khi �c
     */
    public void readAllData(APDU apdu, CryptoManager crypto) {
        byte[] buf = apdu.getBuffer();
        short pos = 0;
        
        // Decrypt customerID
        crypto.decrypt(customerID, (short)0, LEN_CUSTOMER_ID, tempDecryptBuffer, (short)0, iv, (short)0);
        Util.arrayCopyNonAtomic(tempDecryptBuffer, (short)0, buf, pos, LEN_CUSTOMER_ID);
        pos += LEN_CUSTOMER_ID;
        
        // Decrypt name
        crypto.decrypt(name, (short)0, LEN_NAME, tempDecryptBuffer, (short)0, iv, (short)0);
        Util.arrayCopyNonAtomic(tempDecryptBuffer, (short)0, buf, pos, LEN_NAME);
        pos += LEN_NAME;
        
        // Decrypt DOB
        crypto.decrypt(dateOfBirth, (short)0, LEN_DOB, tempDecryptBuffer, (short)0, iv, (short)0);
        Util.arrayCopyNonAtomic(tempDecryptBuffer, (short)0, buf, pos, LEN_DOB);
        pos += LEN_DOB;
        
        // Decrypt phone
        crypto.decrypt(phoneNumber, (short)0, LEN_PHONE, tempDecryptBuffer, (short)0, iv, (short)0);
        Util.arrayCopyNonAtomic(tempDecryptBuffer, (short)0, buf, pos, LEN_PHONE);
        pos += LEN_PHONE;
       
        
        // Photo length (kh�ng encrypt)
        buf[pos++] = (byte)(photoLength >> 8);
        buf[pos++] = (byte)photoLength;
        
        apdu.setOutgoing();
        apdu.setOutgoingLength(pos);
        apdu.sendBytesLong(buf, (short)0, pos);
    }

    public void startPhotoWrite() {
        photoWriteOffset = 0;
        photoLength = 0;
        Util.arrayFillNonAtomic(customerPhoto, (short)0, MAX_PHOTO_SIZE, (byte)0);
    }

    public void writePhotoChunk(APDU apdu) {
        short len = apdu.setIncomingAndReceive();
        byte[] buf = apdu.getBuffer();
        if ((short)(photoWriteOffset + len) > MAX_PHOTO_SIZE) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        Util.arrayCopyNonAtomic(buf, ISO7816.OFFSET_CDATA, customerPhoto, photoWriteOffset, len);
        photoWriteOffset += len;
    }

    public void finishPhotoWrite(APDU apdu) {
        photoLength = photoWriteOffset;
        photoWriteOffset = 0;
        // Photo kh�ng encrypt theo y�u cu
    }

    public void readPhotoChunk(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short offset = (short)((buf[ISO7816.OFFSET_P1] << 8) | (buf[ISO7816.OFFSET_P2] & 0xFF));
        short le = apdu.setOutgoing();
        if (le == 0 || le > 200) le = 200;
        if (photoLength == 0 || offset >= photoLength) {
            ISOException.throwIt(offset >= photoLength ? ISO7816.SW_INCORRECT_P1P2 : ISO7816.SW_FILE_NOT_FOUND);
        }
        short remaining = (short)(photoLength - offset);
        short sendLen = (le < remaining) ? le : remaining;
        apdu.setOutgoingLength(sendLen);
        apdu.sendBytesLong(customerPhoto, offset, sendLen);
    }
    public void initializeBalance(CryptoManager crypto) {
        // Balance initial = 0
        Util.arrayFillNonAtomic(tempDecryptBuffer, (short)0, LEN_BALANCE, (byte)0);
        tempDecryptBuffer[0] = (byte)0;  // 0 high byte
        tempDecryptBuffer[1] = (byte)0;  // 0 low byte
        
        // Encrypt ngay
        crypto.encrypt(tempDecryptBuffer, (short)0, LEN_BALANCE, balance, (short)0, iv, (short)0);
        dataEncrypted = true;
    }

    /**
     * Recharge balance - encrypt sau khi update
     */
    public void rechargeBalance(APDU apdu, CryptoManager crypto) {
        short lc = apdu.setIncomingAndReceive();
        if (lc != 2) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        
        byte[] buf = apdu.getBuffer();
        short amount = (short)((buf[ISO7816.OFFSET_CDATA] << 8) | (buf[ISO7816.OFFSET_CDATA + 1] & 0xFF));
        
        // Decrypt balance hin ti
        crypto.decrypt(balance, (short)0, LEN_BALANCE, tempDecryptBuffer, (short)0, iv, (short)0);
        short currentBalance = (short)((tempDecryptBuffer[0] << 8) | (tempDecryptBuffer[1] & 0xFF));
        
        if (amount <= 0 || (short)(currentBalance + amount) > MAX_BALANCE) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }
        
        short newBalance = (short)(currentBalance + amount);
        
        // Prepare plaintext: [2 bytes balance][14 bytes zero padding]
        Util.arrayFillNonAtomic(tempDecryptBuffer, (short)0, LEN_BALANCE, (byte)0);
        tempDecryptBuffer[0] = (byte)(newBalance >> 8);
        tempDecryptBuffer[1] = (byte)newBalance;
        
        // Encrypt li
        crypto.encrypt(tempDecryptBuffer, (short)0, LEN_BALANCE, balance, (short)0, iv, (short)0);
    }

    /**
     * Check balance - decrypt � �c
     */
    public void checkBalance(APDU apdu, CryptoManager crypto) {
        // Decrypt balance
        crypto.decrypt(balance, (short)0, LEN_BALANCE, tempDecryptBuffer, (short)0, iv, (short)0);
        short currentBalance = (short)((tempDecryptBuffer[0] << 8) | (tempDecryptBuffer[1] & 0xFF));
        
        byte[] buf = apdu.getBuffer();
        buf[0] = (byte)(currentBalance >> 8);
        buf[1] = (byte)currentBalance;
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)2);
        apdu.sendBytes((short)0, (short)2);
    }

    /**
     * Make payment - decrypt, update, encrypt
     */
    public void makePayment(APDU apdu, CryptoManager crypto) {
        short lc = apdu.setIncomingAndReceive();
        if (lc != 2) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        
        byte[] buf = apdu.getBuffer();
        short amount = (short)((buf[ISO7816.OFFSET_CDATA] << 8) | (buf[ISO7816.OFFSET_CDATA + 1] & 0xFF));
        
        // Decrypt balance
        crypto.decrypt(balance, (short)0, LEN_BALANCE, tempDecryptBuffer, (short)0, iv, (short)0);
        short currentBalance = (short)((tempDecryptBuffer[0] << 8) | (tempDecryptBuffer[1] & 0xFF));
        
        if (amount <= 0 || currentBalance < amount) {
            ISOException.throwIt(currentBalance < amount ? (short)0x6901 : ISO7816.SW_DATA_INVALID);
        }
        
        short newBalance = (short)(currentBalance - amount);
        
        // Prepare plaintext
        Util.arrayFillNonAtomic(tempDecryptBuffer, (short)0, LEN_BALANCE, (byte)0);
        tempDecryptBuffer[0] = (byte)(newBalance >> 8);
        tempDecryptBuffer[1] = (byte)newBalance;
        
        // Encrypt li
        crypto.encrypt(tempDecryptBuffer, (short)0, LEN_BALANCE, balance, (short)0, iv, (short)0);
    }
    
    /**
     * Re-encrypt tt c data vi key mi (d�ng khi changePIN)
     */
    public void reEncryptAllData(CryptoManager oldCrypto, CryptoManager newCrypto) {
        // Decrypt tt c vi key c
        oldCrypto.decrypt(customerID, (short)0, LEN_CUSTOMER_ID, tempDecryptBuffer, (short)0, iv, (short)0);
        newCrypto.encrypt(tempDecryptBuffer, (short)0, LEN_CUSTOMER_ID, customerID, (short)0, iv, (short)0);
        
        oldCrypto.decrypt(name, (short)0, LEN_NAME, tempDecryptBuffer, (short)0, iv, (short)0);
        newCrypto.encrypt(tempDecryptBuffer, (short)0, LEN_NAME, name, (short)0, iv, (short)0);
        
        oldCrypto.decrypt(dateOfBirth, (short)0, LEN_DOB, tempDecryptBuffer, (short)0, iv, (short)0);
        newCrypto.encrypt(tempDecryptBuffer, (short)0, LEN_DOB, dateOfBirth, (short)0, iv, (short)0);
        
        oldCrypto.decrypt(phoneNumber, (short)0, LEN_PHONE, tempDecryptBuffer, (short)0, iv, (short)0);
        newCrypto.encrypt(tempDecryptBuffer, (short)0, LEN_PHONE, phoneNumber, (short)0, iv, (short)0);
       
        
        oldCrypto.decrypt(balance, (short)0, LEN_BALANCE, tempDecryptBuffer, (short)0, iv, (short)0);
        newCrypto.encrypt(tempDecryptBuffer, (short)0, LEN_BALANCE, balance, (short)0, iv, (short)0);
    }
}