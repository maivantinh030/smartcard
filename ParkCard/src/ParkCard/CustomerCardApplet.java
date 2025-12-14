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
    
    private short balance;
    private static final short MAX_GAMES = 50; // Ti �a 50 tr ch�i
    private static final short GAME_ENTRY_SIZE = 3; // 1 byte v� + 2 bytes m game
    private byte[] gameList;
    private short gameCount;

	
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
    
    private static final byte INS_RECHARGE_BALANCE = (byte) 0x50;
	private static final byte INS_CHECK_BALANCE = (byte) 0x51;
	private static final byte INS_MAKE_PAYMENT = (byte) 0x52;
		// Game management instruction codes
	private static final byte INS_ADD_OR_INCREASE_TICKETS = (byte) 0x80;
	private static final byte INS_DECREASE_GAME_TICKETS = (byte) 0x81;
	private static final byte INS_READ_GAMES = (byte) 0x82;
	private static final byte INS_UPDATE_GAME_TICKETS = (byte) 0x83;
	private static final byte INS_FIND_GAME = (byte) 0x84;
	private static final byte INS_REMOVE_GAME = (byte) 0x85;
	
    
    private static final short SW_AUTHENTICATION_METHOD_BLOCKED = (short)0x6983;
    private static final short SW_WRONG_PIN = (short)0x6A80;
    private static final short SW_SECURITY_STATUS_NOT_SATISFIED = (short)0x6982;
    private static final short SW_INSUFFICIENT_BALANCE = (short) 0x6901; 
	private static final short MAX_BALANCE = 30000; 
    
	
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
        
        gameList = new byte[(short)(MAX_GAMES * GAME_ENTRY_SIZE)];
        gameCount = 0;
        
        userPIN = new OwnerPIN(PIN_MAX_TRIES, PIN_MAX_LENGTH);
        pinCreated = false;
        pinTriesLeft = PIN_MAX_TRIES;
        byte[] defaultPin = {(byte)'1', (byte)'2', (byte)'3', (byte)'4'};
        userPIN.update(defaultPin, (short)0, (byte)defaultPin.length);
        pinCreated = true;
        balance = 0;
        
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
			case INS_RECHARGE_BALANCE:
				rechargeBalance(apdu);
				break;
			// Kim tra s d�
			case INS_CHECK_BALANCE:
				checkBalance(apdu);
				break;

			// Thanh to�n
			case INS_MAKE_PAYMENT:
				makePayment(apdu);
				break;
				case INS_ADD_OR_INCREASE_TICKETS:
				addOrIncreaseTickets(apdu);
				break;
			case INS_DECREASE_GAME_TICKETS:
				decreaseGameTickets(apdu);
				break;
			case INS_READ_GAMES:
				readGames(apdu);
				break;
			case INS_UPDATE_GAME_TICKETS:
				updateGameTickets(apdu);
				break;
			case INS_FIND_GAME:
				findGame(apdu);
				break;
			case INS_REMOVE_GAME:
				removeGame(apdu);
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
            
        } else {
            pinTriesLeft = userPIN.getTriesRemaining();
            ISOException.throwIt(SW_WRONG_PIN);
        }
    }
    private void changePIN(APDU apdu) {
        if (!pinCreated) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        
        // if (!userPIN.isValidated()) {
            // ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        // }
        
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
    // Finalize photo - �m bo photoLength ��c set ��ng
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
    private void rechargeBalance(APDU apdu) {
    	short lc = apdu.setIncomingAndReceive();
		if (lc != 2) { // Check length
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}
    	
		byte[] buf = apdu.getBuffer();
		short amount = (short)((buf[ISO7816.OFFSET_CDATA] << 8) | (buf[ISO7816.OFFSET_CDATA + 1] & 0xFF)); // �c khon tin

		if (amount <= 0 || amount > MAX_BALANCE) {
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}

		if ((short)(balance + amount) > MAX_BALANCE) {
			ISOException.throwIt(ISO7816.SW_WARNING_STATE_UNCHANGED); // Kh�ng np ��c do v�t s d� ti �a
		}

		balance += amount;
	}

	/**
	 * Ph��ng thc kim tra s d�
	 */
	private void checkBalance(APDU apdu) {
		byte[] buf = apdu.getBuffer();

		buf[0] = (byte)(balance >> 8); // Byte cao
		buf[1] = (byte)(balance);      // Byte thp

		apdu.setOutgoing();
		apdu.setOutgoingLength((short)2);
		apdu.sendBytes((short)0, (short)2);
	}

	/**
	 * Ph��ng thc thanh to�n
	 */
	private void makePayment(APDU apdu) {
		short lc = apdu.setIncomingAndReceive(); // Th�m dng n�y  
		if (lc != 2) {
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		}
		byte[] buf = apdu.getBuffer();
		short amount = (short)((buf[ISO7816.OFFSET_CDATA] << 8) | (buf[ISO7816.OFFSET_CDATA + 1] & 0xFF)); // �c khon tin cn thanh to�n

		if (amount <= 0) {
			ISOException.throwIt(ISO7816.SW_WRONG_LENGTH); // S tin kh�ng hp l
		}

		if (balance < amount) {
			ISOException.throwIt(SW_INSUFFICIENT_BALANCE); // Kh�ng � s d� � thanh to�n
		}

		balance -= amount;
	}
	private void addOrIncreaseTickets(APDU apdu) {
	    short lc = apdu.setIncomingAndReceive();
	    if (lc != 3) {
	        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
	    }
	    
	    byte[] buf = apdu.getBuffer();
	    byte ticketsToAdd = buf[ISO7816.OFFSET_CDATA];
	    short gameCode = (short)((buf[ISO7816.OFFSET_CDATA + 1] << 8) | 
	                            (buf[ISO7816.OFFSET_CDATA + 2] & 0xFF));
	    
	    if (ticketsToAdd <= 0) {
	        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
	    }
	    
	    // Tm xem game � tn ti ch�a
	    short index = findGameIndex(gameCode);
	    
	    if (index == -1) {
	        // CASE 1: Game ch�a c�  th�m mi
	        if (gameCount >= MAX_GAMES) {
	            ISOException.throwIt(ISO7816.SW_FILE_FULL);
	        }
	        
	        short offset = (short)(gameCount * GAME_ENTRY_SIZE);
	        gameList[offset] = ticketsToAdd;
	        gameList[(short)(offset + 1)] = buf[ISO7816.OFFSET_CDATA + 1];
	        gameList[(short)(offset + 2)] = buf[ISO7816.OFFSET_CDATA + 2];
	        
	        gameCount++;
	        
	    } else {
	        // CASE 2: Game � c�  t�ng s v�
	        short offset = (short)(index * GAME_ENTRY_SIZE);
	        byte currentTickets = gameList[offset];
	        
	        // Kim tra overflow
	        if ((short)(currentTickets + ticketsToAdd) > 255) {
	            ISOException.throwIt(ISO7816.SW_WARNING_STATE_UNCHANGED);
	        }
	        
	        gameList[offset] = (byte)(currentTickets + ticketsToAdd);
	    }
	}
	
	/**
	 * Tr s v� ca tr ch�i (khi s dng)
	 * Data format: [gameCode high][gameCode low][tickets_to_subtract]
	 */
	private void decreaseGameTickets(APDU apdu) {
	    short lc = apdu.setIncomingAndReceive();
	    if (lc != 3) {
	        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
	    }
	    
	    byte[] buf = apdu.getBuffer();
	    short gameCode = (short)((buf[ISO7816.OFFSET_CDATA] << 8) | 
	                            (buf[ISO7816.OFFSET_CDATA + 1] & 0xFF));
	    byte ticketsToSubtract = buf[ISO7816.OFFSET_CDATA + 2];
	    
	    if (ticketsToSubtract <= 0) {
	        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
	    }
	    
	    // Tm game
	    short index = findGameIndex(gameCode);
	    if (index == -1) {
	        ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
	    }
	    
	    // Ly s v� hin ti
	    short offset = (short)(index * GAME_ENTRY_SIZE);
	    byte currentTickets = gameList[offset];
	    
	    // Kim tra � v� kh�ng
	    if (currentTickets < ticketsToSubtract) {
	        ISOException.throwIt(SW_INSUFFICIENT_BALANCE);
	    }
	    
	    // Tr s v�
	    gameList[offset] = (byte)(currentTickets - ticketsToSubtract);
	}
	
	/**
	 * �c danh s�ch tt c tr ch�i
	 * Return format: [count high][count low][game1][game2]...
	 */
	private void readGames(APDU apdu) {
	    byte[] buf = apdu.getBuffer();
	    
	    buf[0] = (byte)(gameCount >> 8);
	    buf[1] = (byte)(gameCount);
	    
	    short totalSize = (short)(gameCount * GAME_ENTRY_SIZE);
	    if (totalSize > 0) {
	        Util.arrayCopyNonAtomic(gameList, (short)0, buf, (short)2, totalSize);
	    }
	    
	    apdu.setOutgoing();
	    apdu.setOutgoingLength((short)(2 + totalSize));
	    apdu.sendBytesLong(buf, (short)0, (short)(2 + totalSize));
	}
	
	/**
	 * Cp nht s v� ca tr ch�i (set absolute value)
	 * Data format: [gameCode high][gameCode low][new tickets]
	 */
	private void updateGameTickets(APDU apdu) {
	    short lc = apdu.setIncomingAndReceive();
	    if (lc != 3) {
	        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
	    }
	    
	    byte[] buf = apdu.getBuffer();
	    short gameCode = (short)((buf[ISO7816.OFFSET_CDATA] << 8) | 
	                            (buf[ISO7816.OFFSET_CDATA + 1] & 0xFF));
	    byte newTickets = buf[ISO7816.OFFSET_CDATA + 2];
	    
	    short index = findGameIndex(gameCode);
	    if (index == -1) {
	        ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
	    }
	    
	    short offset = (short)(index * GAME_ENTRY_SIZE);
	    gameList[offset] = newTickets;
	}
	
	/**
	 * Tm tr ch�i theo m
	 * Data format: [gameCode high][gameCode low]
	 * Return: [found][tickets][gameCode high][gameCode low]
	 */
	private void findGame(APDU apdu) {
	    short lc = apdu.setIncomingAndReceive();
	    if (lc != 2) {
	        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
	    }
	    
	    byte[] buf = apdu.getBuffer();
	    short gameCode = (short)((buf[ISO7816.OFFSET_CDATA] << 8) | 
	                            (buf[ISO7816.OFFSET_CDATA + 1] & 0xFF));
	    
	    short index = findGameIndex(gameCode);
	    
	    if (index == -1) {
	        buf[0] = 0; // Not found
	        apdu.setOutgoing();
	        apdu.setOutgoingLength((short)1);
	        apdu.sendBytesLong(buf, (short)0, (short)1);
	    } else {
	        short offset = (short)(index * GAME_ENTRY_SIZE);
	        buf[0] = 1; // Found
	        buf[1] = gameList[offset];
	        buf[2] = gameList[(short)(offset + 1)];
	        buf[3] = gameList[(short)(offset + 2)];
	        
	        apdu.setOutgoing();
	        apdu.setOutgoingLength((short)4);
	        apdu.sendBytesLong(buf, (short)0, (short)4);
	    }
	}
	
	/**
	 * X�a tr ch�i theo m
	 * Data format: [gameCode high][gameCode low]
	 */
	private void removeGame(APDU apdu) {
	    short lc = apdu.setIncomingAndReceive();
	    if (lc != 2) {
	        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
	    }
	    
	    byte[] buf = apdu.getBuffer();
	    short gameCode = (short)((buf[ISO7816.OFFSET_CDATA] << 8) | 
	                            (buf[ISO7816.OFFSET_CDATA + 1] & 0xFF));
	    
	    short index = findGameIndex(gameCode);
	    if (index == -1) {
	        ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
	    }
	    
	    // Dch chuyn c�c elements v ph�a tr�c
	    short fromOffset = (short)((index + 1) * GAME_ENTRY_SIZE);
	    short toOffset = (short)(index * GAME_ENTRY_SIZE);
	    short remainingBytes = (short)((gameCount - index - 1) * GAME_ENTRY_SIZE);
	    
	    if (remainingBytes > 0) {
	        Util.arrayCopyNonAtomic(gameList, fromOffset, gameList, toOffset, remainingBytes);
	    }
	    
	    gameCount--;
	}
	
	/**
	 * Tm index ca game theo m
	 * Return -1 nu kh�ng tm thy
	 */
	private short findGameIndex(short gameCode) {
	    for (short i = 0; i < gameCount; i++) {
	        short offset = (short)(i * GAME_ENTRY_SIZE);
	        short storedGameCode = (short)((gameList[(short)(offset + 1)] << 8) | 
	                                      (gameList[(short)(offset + 2)] & 0xFF));
	        if (storedGameCode == gameCode) {
	            return i;
	        }
	    }
	    return -1;
	}
	
}