package ParkCard;

import javacard.framework.*;

public class CustomerCardApplet extends Applet
{

	private static final short MAX_PHOTO_SIZE = 8000;
	private byte[] customerID;
	private byte[] name;
	private byte[] dateOfBirth;
	private byte[] phoneNumber;
	private byte[] cardType;
	private byte[] customerPhoto;
	
	private short photoLength;
    private short photoWriteOffset;
	
	
	private static final byte INS_WRITE_INFO = (byte) 0x01;
	private static final byte INS_WRITE_PHOTO_CHUNK = (byte) 0x02;  
    private static final byte INS_READ_INFO        = (byte) 0x03;  
    private static final byte INS_START_PHOTO_WRITE = (byte) 0x04; 
    private static final byte INS_READ_PHOTO_CHUNK = (byte) 0X05;
    private static final byte INS_FINISH_PHOTO_WRITE = (byte) 0x06;
    
    private static final byte INS_VERIFY_PIN = (byte) 0x20;
    private static final byte INS_CHANGE_PIN = (byte) 0x24;
    private static final byte INS_CREATE_PIN = (byte) 0x40;
    private static final byte INS_GET_PIN_TRIES = (byte) 0x41;
    private static final byte INS_RESET_PIN_COUNTER = (byte) 0x42;
    
    private static final short SW_AUTHENTICATION_METHOD_BLOCKED = (short)0x6983;
    private static final short SW_WRONG_PIN = (short)0x6A80;
    private static final short SW_SECURITY_STATUS_NOT_SATISFIED = (short)0x6982;
    
	
	private static final short LEN_CUSTOMER_ID = 15;
	private static final short LEN_NAME = 50;
	private static final short LEN_DOB = 10;
	private static final short LEN_PHONE = 10;
	private static final short LEN_CARD_TYPE = 10;

	private OwnerPIN userPIN;
    private boolean pinCreated;
    private byte pinTriesLeft;
    
    private static final byte PIN_MIN_LENGTH = 4;
    private static final byte PIN_MAX_LENGTH = 8;
    private static final byte PIN_MAX_TRIES = 3;
    
	private CustomerCardApplet(){
		customerID = new byte[LEN_CUSTOMER_ID];
		name = new byte[LEN_NAME];
		dateOfBirth = new byte[LEN_DOB];
		phoneNumber = new byte[LEN_PHONE];
		cardType = new byte[LEN_CARD_TYPE];
		customerPhoto = new byte[MAX_PHOTO_SIZE];
		photoLength = 0;
        photoWriteOffset = 0;
        
        userPIN = new OwnerPIN(PIN_MAX_TRIES, PIN_MAX_LENGTH);
        pinCreated = false;
        pinTriesLeft = PIN_MAX_TRIES;
        byte[] defaultPin = {(byte)'1', (byte)'2', (byte)'3', (byte)'4'};
        userPIN.update(defaultPin, (short)0, (byte)defaultPin.length);
        pinCreated = true;
        
	}
	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new CustomerCardApplet().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public void process(APDU apdu) {
        if (selectingApplet()) return;

        byte[] buf = apdu.getBuffer();
        byte ins = buf[ISO7816.OFFSET_INS];

        switch (ins) {
        	case INS_CREATE_PIN:
                createPIN(apdu);
                break;
            case INS_VERIFY_PIN:
                verifyPIN(apdu);
                break;
            case INS_CHANGE_PIN:
                changePIN(apdu);
                break;
            case INS_GET_PIN_TRIES:
                getPinTries(apdu);
                break;
            case INS_RESET_PIN_COUNTER:
                resetPinCounter(apdu);
                break;
            case INS_WRITE_INFO:
                writeCustomerInfo(apdu);
                break;

            case INS_START_PHOTO_WRITE:
                startWritePhoto();
                break;
            case INS_FINISH_PHOTO_WRITE:
				finishPhotoWrite(apdu);
				break;

            case INS_WRITE_PHOTO_CHUNK:
                writePhotoChunk(apdu);
                break;

            case INS_READ_INFO:
                readAllData(apdu);
                break;
            case INS_READ_PHOTO_CHUNK:
				readPhotoChunk(apdu);
			break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

	private void createPIN(APDU apdu) {
        if (pinCreated) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        
        if (lc < PIN_MIN_LENGTH || lc > PIN_MAX_LENGTH) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        userPIN.update(buf, ISO7816.OFFSET_CDATA, (byte)lc);
        pinCreated = true;
        pinTriesLeft = PIN_MAX_TRIES;
    }
    private void verifyPIN(APDU apdu) {
        if (!pinCreated) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        
        if (userPIN.getTriesRemaining() == 0) {
            ISOException.throwIt(SW_AUTHENTICATION_METHOD_BLOCKED);
        }
        
        if (userPIN.check(buf, ISO7816.OFFSET_CDATA, (byte)lc)) {
            pinTriesLeft = PIN_MAX_TRIES;
            // PIN correct - return success
        } else {
            pinTriesLeft = userPIN.getTriesRemaining();
            ISOException.throwIt(SW_WRONG_PIN);
        }
    }
    private void changePIN(APDU apdu) {
        if (!pinCreated) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        
        if (!userPIN.isValidated()) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        
        // Format: [old PIN length][old PIN][new PIN length][new PIN]
        if (lc < 2) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        byte oldPinLength = buf[ISO7816.OFFSET_CDATA];
        short oldPinOffset = (short)(ISO7816.OFFSET_CDATA + 1);
        
        byte newPinLength = buf[(short)(oldPinOffset + oldPinLength)];
        short newPinOffset = (short)(oldPinOffset + oldPinLength + 1);
        
        if (newPinLength < PIN_MIN_LENGTH || newPinLength > PIN_MAX_LENGTH) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        if (!userPIN.check(buf, oldPinOffset, oldPinLength)) {
            ISOException.throwIt(SW_WRONG_PIN);
        }
        userPIN.update(buf, newPinOffset, newPinLength);
        pinTriesLeft = PIN_MAX_TRIES;
    }
    
    private void getPinTries(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        buf[0] = userPIN.getTriesRemaining();
        buf[1] = pinCreated ? (byte)1 : (byte)0;
        buf[2] = userPIN.isValidated() ? (byte)1 : (byte)0;
        
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)3);
        apdu.sendBytesLong(buf, (short)0, (short)3);
    }
    
    private void resetPinCounter(APDU apdu) {
        
        userPIN.resetAndUnblock();
        pinTriesLeft = PIN_MAX_TRIES;
    }
	private void writeCustomerInfo(APDU apdu){
		apdu.setIncomingAndReceive();
		byte[] buf = apdu.getBuffer();
		short offset = ISO7816.OFFSET_CDATA;
		Util.arrayCopyNonAtomic(buf, offset,customerID,(short)0, LEN_CUSTOMER_ID);
		offset += LEN_CUSTOMER_ID;
        Util.arrayCopyNonAtomic(buf, offset,name,(short)0, LEN_NAME);
        offset += LEN_NAME;
        Util.arrayCopyNonAtomic(buf, offset,dateOfBirth,(short)0, LEN_DOB);
        offset += LEN_DOB;
        Util.arrayCopyNonAtomic(buf, offset,phoneNumber,(short)0, LEN_PHONE);
        offset += LEN_PHONE;
        Util.arrayCopyNonAtomic(buf, offset,cardType,(short)0, LEN_CARD_TYPE);
	}
	
	private void startWritePhoto() {
        photoWriteOffset = 0;
        photoLength = 0;

        Util.arrayFillNonAtomic(customerPhoto, (short)0, MAX_PHOTO_SIZE, (byte)0x00);
    }
    private void finishPhotoWrite(APDU apdu) {
    // Finalize photo - ðm bo photoLength ðýc set ðúng
    photoLength = photoWriteOffset;
    photoWriteOffset = 0;  // Reset for next upload
}
    private void writePhotoChunk(APDU apdu) {
        short len = apdu.setIncomingAndReceive();
        byte[] buf = apdu.getBuffer();

        if ((short)(photoWriteOffset + len) > MAX_PHOTO_SIZE) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        Util.arrayCopyNonAtomic(
            buf, ISO7816.OFFSET_CDATA,
            customerPhoto, photoWriteOffset,
            len
        );
        photoWriteOffset += len;
        photoLength = photoWriteOffset;
    }
    private void readAllData(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short pos = 0;
        Util.arrayCopyNonAtomic(customerID,(short)0, buf, pos, LEN_CUSTOMER_ID);
        pos += LEN_CUSTOMER_ID;
        Util.arrayCopyNonAtomic(name,(short)0, buf, pos, LEN_NAME);
        pos += LEN_NAME;
        Util.arrayCopyNonAtomic(dateOfBirth,(short)0, buf, pos, LEN_DOB);
        pos += LEN_DOB;
        Util.arrayCopyNonAtomic(phoneNumber,(short)0, buf, pos, LEN_PHONE);
        pos += LEN_PHONE;
        Util.arrayCopyNonAtomic(cardType,(short)0, buf, pos, LEN_CARD_TYPE);
        pos += LEN_CARD_TYPE;
        buf[pos++] = (byte)(photoLength >> 8);   // High byte  
		buf[pos++] = (byte)(photoLength); 
        apdu.setOutgoing();
        apdu.setOutgoingLength(pos);
        apdu.sendBytesLong(buf, (short)0, pos);
    }
    private void readPhotoChunk(APDU apdu) {
    byte[] buf = apdu.getBuffer();
    
    // Parse P1P2 = offset
    byte p1 = buf[ISO7816.OFFSET_P1];
    byte p2 = buf[ISO7816.OFFSET_P2];
    short offset = (short)((p1 << 8) | (p2 & 0xFF));
    
    // Parse Le = requested size
    short le = apdu.setOutgoing();
    if (le == 0) le = 256;
    if (le > 200) le = 200;  // Limit chunk size
    
    // Check if no photo
    if (photoLength == 0) {
        ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
    }
    
    // Check if offset beyond photo
    if (offset >= photoLength) {
        ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
    }
    
    // Calculate actual chunk size
    short remainingBytes = (short)(photoLength - offset);
    short actualSize;
	if (le < remainingBytes) {
		actualSize = le;
	} else {
		actualSize = remainingBytes;
	}
    
    // Send chunk
    apdu.setOutgoingLength(actualSize);
    apdu.sendBytesLong(customerPhoto, offset, actualSize);
	}
    
}
