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
    
    public boolean isPINValidated() {
        return userPIN.isValidated();
    }

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
		 * Change PIN -> unwrap master with old PIN -> rewrap with new PIN (no bulk re-encrypt).
		 * Assumes old PIN has already been verified via verify() call.
		 */
		public void changePIN(APDU apdu) {
			byte[] buf = apdu.getBuffer();
			apdu.setIncomingAndReceive();
			
			byte oldLen = buf[ISO7816.OFFSET_CDATA];
			short oldOffset = (short)(ISO7816.OFFSET_CDATA + 1);
			byte newLen = buf[(short)(oldOffset + oldLen)];
			short newOffset = (short)(oldOffset + oldLen + 1);
			
			// Validate length
			if (newLen < PIN_MIN_LENGTH || newLen > PIN_MAX_LENGTH) {
				ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
			}
			
			// Verify old PIN
			if (!userPIN.check(buf, oldOffset, oldLen)) {
				ISOException.throwIt((short)(0x63C0 | userPIN.getTriesRemaining()));
			}
			
			// Re-wrap master key vi new PIN (nu có crypto setup)
			if (cardModel != null && cryptoManager != null && cryptoManager.isKeyReady()) {
				byte[] salt = cardModel.getSalt();
				byte[] iv = cardModel.getIV();
				
				// Derive new PIN key (preserve loaded master key)
				cryptoManager.deriveKeyFromPIN(buf, newOffset, newLen, salt, (short)0, (short)16, true);
				
				// Re-wrap master key vi new PIN key
				cryptoManager.wrapLoadedMasterKey(
					cardModel.getWrappedMasterKey(), (short)0, 
					iv, (short)0
				);
			}
			
			// Update PIN
			userPIN.update(buf, newOffset, newLen);
			
			// Restore authenticated state
			userPIN.check(buf, newOffset, newLen);
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
     * Reset PIN using PUK, and rewrap master key with new PIN.
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

        // Derive pin key from new PIN and rewrap master key
        if (cardModel != null && cryptoManager != null) {
            byte[] salt = cardModel.getSalt();
            cryptoManager.deriveKeyFromPIN(buf, newOffset, newLen, salt, (short)0, (short)16);
            if (!cardModel.isMasterKeyWrapped()) {
                cardModel.ensureMasterKey(cryptoManager);
            }
            // unwrap with new pin key is not possible since master is already wrapped by unknown key
            // Instead, require card to still hold master plaintext? Not available. So re-wrap assumes current wrapped master was produced with last PIN key.
            // To keep logic consistent, unwrap using current pinKey is impossible; so we reset tries and just re-derive new key and keep master wrapped as-is.
            // For safety, we clear key and mark data not ready; caller must verify PIN to load master.
            cardModel.setMasterKeyWrapped(true);
            cryptoManager.clearKey();
        }

        userPIN.update(buf, newOffset, newLen);
        pinCreated = true;
        userPIN.resetAndUnblock();
    }
}