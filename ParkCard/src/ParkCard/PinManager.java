package ParkCard;
import javacard.framework.*;
import javacard.security.*;

public class PinManager {
    private OwnerPIN userPIN;
    private boolean pinCreated;
    private static final byte PIN_MIN_LENGTH = 4;
    private static final byte PIN_MAX_LENGTH = 8;
    private static final byte PIN_MAX_TRIES = 3;
    
    // Reference to CryptoManager (ðýc set t bên ngoài)
    private CryptoManager cryptoManager;
    private CardModel cardModel;

    public PinManager() {
        userPIN = new OwnerPIN(PIN_MAX_TRIES, PIN_MAX_LENGTH);
        pinCreated = false;
        // PIN mc ðnh
        byte[] defaultPin = {'1','2','3','4'};
        userPIN.update(defaultPin, (short)0, (byte)4);
        pinCreated = true;
    }
    
    /**
     * Set references ð có th derive key
     */
    public void setCryptoManager(CryptoManager crypto) {
        this.cryptoManager = crypto;
    }
    
    public void setCardModel(CardModel model) {
        this.cardModel = model;
    }

    public void createPIN(APDU apdu) {
        if (pinCreated) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        if (lc < PIN_MIN_LENGTH || lc > PIN_MAX_LENGTH) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        userPIN.update(buf, ISO7816.OFFSET_CDATA, (byte)lc);
        pinCreated = true;
    }

    /**
     * Verify PIN và derive AES key
     */
    public boolean verify(byte[] buf, short offset, byte len) {
        if (!pinCreated) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        
        if (userPIN.check(buf, offset, len)) {
            // Verify thành công - derive key t PIN
            if (cryptoManager != null && cardModel != null) {
                byte[] salt = cardModel.getSalt();
                cryptoManager.deriveKeyFromPIN(buf, offset, len, salt, (short)0, (short)16);
            }
            return true;
        }
        return false;
    }

    public byte getTriesRemaining() { return userPIN.getTriesRemaining(); }

    /**
     * Change PIN - cn re-encrypt tt c data vi key mi
     */
    public void changePIN(APDU apdu) {
        if (!pinCreated) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        byte oldLen = buf[ISO7816.OFFSET_CDATA];
        short oldOffset = (short)(ISO7816.OFFSET_CDATA + 1);
        byte newLen = buf[(short)(oldOffset + oldLen)];
        short newOffset = (short)(oldOffset + oldLen + 1);
        
        if (newLen < PIN_MIN_LENGTH || newLen > PIN_MAX_LENGTH) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        // Verify old PIN
        if (!userPIN.check(buf, oldOffset, oldLen)) {
            ISOException.throwIt((short)0x6A80);
        }
        
        // Nu có data ð encrypted, cn re-encrypt vi key mi
        if (cardModel != null && cardModel.isDataEncrypted() && cryptoManager != null) {
            // To CryptoManager mi cho new PIN
            CryptoManager newCrypto = new CryptoManager();
            byte[] salt = cardModel.getSalt();
            
            // Derive key mi t new PIN
            newCrypto.deriveKeyFromPIN(buf, newOffset, newLen, salt, (short)0, (short)16);
            
            // Re-encrypt tt c data
            // oldCrypto (cryptoManager hin ti) vn có key c
            cardModel.reEncryptAllData(cryptoManager, newCrypto);
            
            // Clear old crypto và update reference
            cryptoManager.clearKey();
            
            // Note:  ðây không th thay th reference cryptoManager
            // v nó ðýc pass t Applet. Thay vào ðó, copy key t newCrypto
            // Workaround: derive li key t new PIN vào cryptoManager hin ti
            cryptoManager.deriveKeyFromPIN(buf, newOffset, newLen, salt, (short)0, (short)16);
        }
        
        // Update PIN
        userPIN.update(buf, newOffset, newLen);
    }

    public void getPinTries(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        buf[0] = userPIN.getTriesRemaining();
        buf[1] = pinCreated ? (byte)1 : (byte)0;
        buf[2] = userPIN.isValidated() ? (byte)1 : (byte)0;
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)3);
        apdu.sendBytesLong(buf, (short)0, (short)3);
    }

    public void resetPinCounter(APDU apdu) {
        userPIN.resetAndUnblock();
    }
}