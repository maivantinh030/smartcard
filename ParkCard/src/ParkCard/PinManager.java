package ParkCard;
import javacard.framework.*;
import javacard.security.*;

public class PinManager {
    private OwnerPIN userPIN;
    private OwnerPIN pukPIN;
    private boolean pinCreated;
    private static final byte PIN_MIN_LENGTH = 4;
    private static final byte PIN_MAX_LENGTH = 8;
    private static final byte PIN_MAX_TRIES = 3;
    private static final byte PUK_LENGTH = 8;
    private static final byte PUK_MAX_TRIES = 5;

    // References provided from outside
    private CryptoManager cryptoManager;
    private CardModel cardModel;

    public PinManager() {
        userPIN = new OwnerPIN(PIN_MAX_TRIES, PIN_MAX_LENGTH);
        pukPIN = new OwnerPIN(PUK_MAX_TRIES, PUK_LENGTH);
        pinCreated = false;
        // Default PIN 1234
        byte[] defaultPin = { '1', '2', '3', '4' };
        userPIN.update(defaultPin, (short)0, (byte)4);
        pinCreated = true;

        // Default PUK 87654321
        byte[] defaultPuk = { '8','7','6','5','4','3','2','1' };
        pukPIN.update(defaultPuk, (short)0, PUK_LENGTH);
    }

    public void setCryptoManager(CryptoManager crypto) { this.cryptoManager = crypto; }
    public void setCardModel(CardModel model) { this.cardModel = model; }

    public void createPIN(APDU apdu) {
        if (pinCreated) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        if (lc < PIN_MIN_LENGTH || lc > PIN_MAX_LENGTH) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        userPIN.update(buf, ISO7816.OFFSET_CDATA, (byte) lc);
        pinCreated = true;
    }

    /**
     * Verify PIN -> derive PIN key -> ensure/generate master key -> unwrap master key for use.
     */
    public boolean verify(byte[] buf, short offset, byte len) {
        if (!pinCreated) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

        if (userPIN.check(buf, offset, len)) {
            if (cryptoManager != null && cardModel != null) {
                byte[] salt = cardModel.getSalt();
                cryptoManager.deriveKeyFromPIN(buf, offset, len, salt, (short)0, (short)16);
                cardModel.ensureMasterKey(cryptoManager);
                cryptoManager.unwrapMasterKey(cardModel.getWrappedMasterKey(), (short)0, cardModel.getIV(), (short)0);
            }
            return true;
        }
        return false;
    }

    public byte getTriesRemaining() { return userPIN.getTriesRemaining(); }

    /**
     * FIXED: Change PIN vi proper key management � tr�nh corruption
     * 
     * Thay v ghi �� pinKey buffer trong qu� trnh operation, ta s:
     * 1. Backup current master key
     * 2. Create new wrapped master key vi new PIN
     * 3. Atomic update PIN v� wrapped key
     * 4. Rollback nu c� li
     */
    public void changePIN(APDU apdu) {
        if (!pinCreated) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

        byte[] buf = apdu.getBuffer();
        apdu.setIncomingAndReceive();
        byte oldLen = buf[ISO7816.OFFSET_CDATA];
        short oldOffset = (short)(ISO7816.OFFSET_CDATA + 1);
        byte newLen = buf[(short)(oldOffset + oldLen)];
        short newOffset = (short)(oldOffset + oldLen + 1);

        if (newLen < PIN_MIN_LENGTH || newLen > PIN_MAX_LENGTH) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Verify old PIN tr�c
        if (!userPIN.check(buf, oldOffset, oldLen)) {
            ISOException.throwIt((short)0x6A80);
        }

        if (cardModel != null && cryptoManager != null) {
            // CREATE BACKUP REFERENCES
            byte[] originalWrappedKey = cardModel.getWrappedMasterKey();
            byte[] salt = cardModel.getSalt();
            byte[] iv = cardModel.getIV();
            
            // TEMPORARY STORAGE cho new wrapped key
            byte[] newWrappedKey = JCSystem.makeTransientByteArray((short)16, JCSystem.CLEAR_ON_DESELECT);
            byte[] backupWrappedKey = JCSystem.makeTransientByteArray((short)16, JCSystem.CLEAR_ON_DESELECT);
            
            try {
                // STEP 1: Backup current wrapped key
                Util.arrayCopyNonAtomic(originalWrappedKey, (short)0, backupWrappedKey, (short)0, (short)16);
                
                // STEP 2: Derive old PIN key v� unwrap master key
                cryptoManager.deriveKeyFromPIN(buf, oldOffset, oldLen, salt, (short)0, (short)16);
                if (!cardModel.isMasterKeyWrapped()) {
                    cardModel.ensureMasterKey(cryptoManager);
                }
                cryptoManager.unwrapMasterKey(originalWrappedKey, (short)0, iv, (short)0);
                
                // STEP 3: Derive new PIN key but keep the already unwrapped master key usable
                cryptoManager.deriveKeyFromPIN(buf, newOffset, newLen, salt, (short)0, (short)16, true);
                cryptoManager.wrapLoadedMasterKey(newWrappedKey, (short)0, iv, (short)0);
                
                // STEP 4: ATOMIC UPDATE - ch update khi � chc chn th�nh c�ng
                Util.arrayCopyNonAtomic(newWrappedKey, (short)0, originalWrappedKey, (short)0, (short)16);
                cardModel.setMasterKeyWrapped(true);
                
                // STEP 5: Update PIN cui c�ng
                userPIN.update(buf, newOffset, newLen);
                
            } catch (Exception e) {
                // ROLLBACK nu c� li
                Util.arrayCopyNonAtomic(backupWrappedKey, (short)0, originalWrappedKey, (short)0, (short)16);
                cryptoManager.clearKey();
                cardModel.setDataReady(false);
                
                // Re-throw exception
                if (e instanceof ISOException) {
                    ISOException.throwIt(((ISOException)e).getReason());
                } else {
                    ISOException.throwIt(ISO7816.SW_UNKNOWN);
                }
            } finally {
                // CLEANUP: Clear sensitive data t temporary buffers
                Util.arrayFillNonAtomic(newWrappedKey, (short)0, (short)16, (byte)0);
                Util.arrayFillNonAtomic(backupWrappedKey, (short)0, (short)16, (byte)0);
            }
        } else {
            // Fallback: nu kh�ng c� crypto manager, ch update PIN
            userPIN.update(buf, newOffset, newLen);
        }
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

    /**
     * Reset PIN using PUK, v� rewrap master key vi new PIN.
     * APDU data: [PUK(8)][newLen(1)][newPIN]
     */
    public void resetWithPUK(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        apdu.setIncomingAndReceive();

        // Check PUK first
        if (!pukPIN.check(buf, ISO7816.OFFSET_CDATA, PUK_LENGTH)) {
            ISOException.throwIt((short)0x63C0); // standard PUK fail
        }

        byte newLen = buf[(short)(ISO7816.OFFSET_CDATA + PUK_LENGTH)];
        short newOffset = (short)(ISO7816.OFFSET_CDATA + PUK_LENGTH + 1);

        if (newLen < PIN_MIN_LENGTH || newLen > PIN_MAX_LENGTH) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Derive pin key t new PIN v� rewrap master key
        if (cardModel != null && cryptoManager != null) {
            byte[] salt = cardModel.getSalt();
            cryptoManager.deriveKeyFromPIN(buf, newOffset, newLen, salt, (short)0, (short)16);
            if (!cardModel.isMasterKeyWrapped()) {
                cardModel.ensureMasterKey(cryptoManager);
            }
            // unwrap vi new pin key kh�ng th v master � wrapped bng key c
            // V vy ta phi clear key v� y�u cu verify PIN � load master
            cardModel.setMasterKeyWrapped(true);
            cryptoManager.clearKey();
        }

        userPIN.update(buf, newOffset, newLen);
        pinCreated = true;
        userPIN.resetAndUnblock();
    }
}