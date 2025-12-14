package ParkCard;

import javacard.framework.*;
import javacard.security.RandomData;

public class CardModel {

    // Kích thýc gc
    private static final short LEN_CUSTOMER_ID = 15;
    private static final short LEN_NAME = 50;
    private static final short LEN_DOB = 10;
    private static final short LEN_PHONE = 10;
    private static final short LEN_CARD_TYPE = 10;
    private static final short MAX_PHOTO_SIZE = 8000;

    // Kích thýc padded cho AES CBC (bi 16)
    private static final short PAD16(short len) { return (short)((len + 15) & ~15); }

    private static final short LEN_CUSTOMER_ID_PAD = 16;
    private static final short LEN_NAME_PAD        = 64;
    private static final short LEN_DOB_PAD         = 16;
    private static final short LEN_PHONE_PAD       = 16;
    private static final short LEN_CARD_TYPE_PAD   = 16;
    private static final short MAX_PHOTO_SIZE_PAD = 8000;

    private static final short MAX_GAMES = 50;
    private static final short GAME_ENTRY_SIZE = 3;
    private static final short GAME_LIST_MAX_BYTES = (short)(MAX_GAMES * GAME_ENTRY_SIZE);
    private static final short GAME_LIST_PAD = 160;

    private static final short MAX_BALANCE = 30000;

    byte[] customerID;
    byte[] name;
    byte[] dateOfBirth;
    byte[] phoneNumber;
    byte[] cardType;

    byte[] customerPhoto;
    short photoLength;
    short photoWriteOffset;

    short balance;

    byte[] gameList;
    short[] gameCountRef; // dùng mng 1 phn t ð truyn reference cho GameManager

    boolean dataReady;
    private byte[] salt;
    private static final short SALT_LENGTH = 16;
    private boolean saltGenerated;  // Ðánh du ð sinh salt chýa

    public CardModel() {
        customerID = new byte[LEN_CUSTOMER_ID_PAD];
        name = new byte[LEN_NAME_PAD];
        dateOfBirth = new byte[LEN_DOB_PAD];
        phoneNumber = new byte[LEN_PHONE_PAD];
        cardType = new byte[LEN_CARD_TYPE_PAD];

        customerPhoto = new byte[MAX_PHOTO_SIZE_PAD];
        photoLength = 0;
        photoWriteOffset = 0;

        balance = 0;

        gameList = new byte[GAME_LIST_PAD];
        gameCountRef = new short[1];
        gameCountRef[0] = 0;

        dataReady = false;
        salt = new byte[SALT_LENGTH];
        saltGenerated = false;  // Ban ðu chýa có
    }

    public boolean isDataReady() { return dataReady; }
    public void setDataReady(boolean r) { dataReady = r; }

	// Thêm hàm ð CryptoManager ly salt
    public byte[] getSalt() { return salt; }

    // Thêm hàm sinh salt ngu nhiên (gi t Applet constructor)
    public void generateRandomSalt() {
        if (saltGenerated) return;  // Ch sinh mt ln

        RandomData rnd = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        rnd.generateData(salt, (short)0, SALT_LENGTH);

        saltGenerated = true;
    }
    public void writeCustomerInfo(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short offset = ISO7816.OFFSET_CDATA;

        Util.arrayCopyNonAtomic(buf, offset, customerID, (short)0, LEN_CUSTOMER_ID);
        offset += LEN_CUSTOMER_ID;
        Util.arrayCopyNonAtomic(buf, offset, name, (short)0, LEN_NAME);
        offset += LEN_NAME;
        Util.arrayCopyNonAtomic(buf, offset, dateOfBirth, (short)0, LEN_DOB);
        offset += LEN_DOB;
        Util.arrayCopyNonAtomic(buf, offset, phoneNumber, (short)0, LEN_PHONE);
        offset += LEN_PHONE;
        Util.arrayCopyNonAtomic(buf, offset, cardType, (short)0, LEN_CARD_TYPE);
    }

    public void startPhotoWrite() {
        photoWriteOffset = 0;
        photoLength = 0;
        Util.arrayFillNonAtomic(customerPhoto, (short)0, MAX_PHOTO_SIZE_PAD, (byte)0);
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
    }

    public void readAllData(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short pos = 0;

        Util.arrayCopyNonAtomic(customerID, (short)0, buf, pos, LEN_CUSTOMER_ID); pos += LEN_CUSTOMER_ID;
        Util.arrayCopyNonAtomic(name, (short)0, buf, pos, LEN_NAME); pos += LEN_NAME;
        Util.arrayCopyNonAtomic(dateOfBirth, (short)0, buf, pos, LEN_DOB); pos += LEN_DOB;
        Util.arrayCopyNonAtomic(phoneNumber, (short)0, buf, pos, LEN_PHONE); pos += LEN_PHONE;
        Util.arrayCopyNonAtomic(cardType, (short)0, buf, pos, LEN_CARD_TYPE); pos += LEN_CARD_TYPE;

        buf[pos++] = (byte)(photoLength >> 8);
        buf[pos++] = (byte)photoLength;

        apdu.setOutgoing();
        apdu.setOutgoingLength(pos);
        apdu.sendBytesLong(buf, (short)0, pos);
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

    public void rechargeBalance(APDU apdu) {
        short lc = apdu.setIncomingAndReceive();
        if (lc != 2) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        byte[] buf = apdu.getBuffer();
        short amount = (short)((buf[ISO7816.OFFSET_CDATA] << 8) | (buf[ISO7816.OFFSET_CDATA + 1] & 0xFF));

        if (amount <= 0 || (short)(balance + amount) > MAX_BALANCE) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }
        balance += amount;
    }

    public void checkBalance(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        buf[0] = (byte)(balance >> 8);
        buf[1] = (byte)balance;
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)2);
        apdu.sendBytes((short)0, (short)2);
    }

    public void makePayment(APDU apdu) {
        short lc = apdu.setIncomingAndReceive();
        if (lc != 2) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        byte[] buf = apdu.getBuffer();
        short amount = (short)((buf[ISO7816.OFFSET_CDATA] << 8) | (buf[ISO7816.OFFSET_CDATA + 1] & 0xFF));

        if (amount <= 0 || balance < amount) {
            ISOException.throwIt(balance < amount ? (short)0x6901 : ISO7816.SW_DATA_INVALID);
        }
        balance -= amount;
    }

    public void encryptAll(CryptoManager crypto) {
        if (!dataReady) return;

        crypto.encryptInPlace(customerID, (short)0, LEN_CUSTOMER_ID_PAD);
        crypto.encryptInPlace(name, (short)0, LEN_NAME_PAD);
        crypto.encryptInPlace(dateOfBirth, (short)0, LEN_DOB_PAD);
        crypto.encryptInPlace(phoneNumber, (short)0, LEN_PHONE_PAD);
        crypto.encryptInPlace(cardType, (short)0, LEN_CARD_TYPE_PAD);

        if (photoLength > 0) {
            crypto.encryptInPlace(customerPhoto, (short)0, PAD16(photoLength));
        }

        // Balance: chuyn thành block 16 bytes
        byte[] balBlock = JCSystem.makeTransientByteArray((short)16, JCSystem.CLEAR_ON_DESELECT);
        balBlock[0] = (byte)(balance >> 8);
        balBlock[1] = (byte)balance;
        crypto.encryptInPlace(balBlock, (short)0, (short)16);
        balance = (short)((balBlock[0] << 8) | (balBlock[1] & 0xFF));

        // Game list
        short gameBytes = (short)(gameCountRef[0] * GAME_ENTRY_SIZE);
        if (gameBytes > 0) {
            crypto.encryptInPlace(gameList, (short)0, PAD16(gameBytes));
        }
    }

    public void decryptAll(CryptoManager crypto) {
        crypto.decryptInPlace(customerID, (short)0, LEN_CUSTOMER_ID_PAD);
        crypto.decryptInPlace(name, (short)0, LEN_NAME_PAD);
        crypto.decryptInPlace(dateOfBirth, (short)0, LEN_DOB_PAD);
        crypto.decryptInPlace(phoneNumber, (short)0, LEN_PHONE_PAD);
        crypto.decryptInPlace(cardType, (short)0, LEN_CARD_TYPE_PAD);

        if (photoLength > 0) {
            crypto.decryptInPlace(customerPhoto, (short)0, PAD16(photoLength));
        }

        // Balance
        byte[] balBlock = JCSystem.makeTransientByteArray((short)16, JCSystem.CLEAR_ON_DESELECT);
        balBlock[0] = (byte)(balance >> 8);
        balBlock[1] = (byte)balance;
        crypto.decryptInPlace(balBlock, (short)0, (short)16);
        balance = (short)((balBlock[0] << 8) | (balBlock[1] & 0xFF));

        // Game list
        short gameBytes = (short)(gameCountRef[0] * GAME_ENTRY_SIZE);
        if (gameBytes > 0) {
            crypto.decryptInPlace(gameList, (short)0, PAD16(gameBytes));
        }
    }
}