package ParkCard;

import javacard.framework.*;
import javacard.security.*;

public class PinManager {
    private OwnerPIN userPIN;
    private boolean pinCreated;

    private static final byte PIN_MIN_LENGTH = 4;
    private static final byte PIN_MAX_LENGTH = 8;
    private static final byte PIN_MAX_TRIES = 3;

    public PinManager() {
        userPIN = new OwnerPIN(PIN_MAX_TRIES, PIN_MAX_LENGTH);
        pinCreated = false;

        // PIN mc ðnh
        byte[] defaultPin = {'1','2','3','4'};
        userPIN.update(defaultPin, (short)0, (byte)4);
        pinCreated = true;
    }

    public void createPIN(APDU apdu) {
        if (pinCreated) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        if (lc < PIN_MIN_LENGTH || lc > PIN_MAX_LENGTH) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        userPIN.update(buf, ISO7816.OFFSET_CDATA, (byte)lc);
        pinCreated = true;
    }

    public boolean verify(byte[] buf, short offset, byte len) {
        if (!pinCreated) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        return userPIN.check(buf, offset, len);
    }

    public byte getTriesRemaining() { return userPIN.getTriesRemaining(); }

    public void changePIN(APDU apdu) {
        if (!pinCreated) ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);

        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();

        byte oldLen = buf[ISO7816.OFFSET_CDATA];
        short oldOffset = (short)(ISO7816.OFFSET_CDATA + 1);
        byte newLen = buf[(short)(oldOffset + oldLen)];
        short newOffset = (short)(oldOffset + oldLen + 1);

        if (newLen < PIN_MIN_LENGTH || newLen > PIN_MAX_LENGTH) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        if (!userPIN.check(buf, oldOffset, oldLen)) ISOException.throwIt((short)0x6A80);

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