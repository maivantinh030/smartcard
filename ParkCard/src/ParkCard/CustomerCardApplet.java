package ParkCard;
import javacard.framework.*;

public class CustomerCardApplet extends Applet {
    private CardModel model;
    private PinManager pinMgr;
    private GameManager gameMgr;
    private CryptoManager cryptoMgr;
    
    // Instruction codes - Sequential from 0x01
    private static final byte INS_CREATE_PIN = (byte) 0x01;
    private static final byte INS_VERIFY_PIN = (byte) 0x02;
    private static final byte INS_CHANGE_PIN = (byte) 0x03;
    private static final byte INS_GET_PIN_TRIES = (byte) 0x04;
    private static final byte INS_RESET_PIN_COUNTER = (byte) 0x05;
    private static final byte INS_RESET_WITH_PUK = (byte) 0x06;
    private static final byte INS_WRITE_INFO = (byte) 0x07;
    private static final byte INS_START_PHOTO_WRITE = (byte) 0x08;
    private static final byte INS_WRITE_PHOTO_CHUNK = (byte) 0x09;
    private static final byte INS_FINISH_PHOTO_WRITE = (byte) 0x0A;
    private static final byte INS_READ_INFO = (byte) 0x0B;
    private static final byte INS_READ_PHOTO_CHUNK = (byte) 0x0C;
    private static final byte INS_RECHARGE_BALANCE = (byte) 0x0D;
    private static final byte INS_CHECK_BALANCE = (byte) 0x0E;
    private static final byte INS_MAKE_PAYMENT = (byte) 0x0F;
    private static final byte INS_GET_CRYPTO_INFO = (byte) 0x10;
    
    // Game management instruction codes
    private static final byte INS_ADD_OR_INCREASE_TICKETS = (byte) 0x11;
    private static final byte INS_DECREASE_GAME_TICKETS = (byte) 0x12;
    private static final byte INS_READ_GAMES = (byte) 0x13;
    private static final byte INS_UPDATE_GAME_TICKETS = (byte) 0x14;
    private static final byte INS_FIND_GAME = (byte) 0x15;
    private static final byte INS_REMOVE_GAME = (byte) 0x16;
    
    // RSA Challenge-Response instructions
    private static final byte INS_SET_CUSTOMER_ID = (byte) 0x17;
    private static final byte INS_GET_CUSTOMER_ID = (byte) 0x18;
    private static final byte INS_SET_RSA_EXPONENT = (byte) 0x19;
    private static final byte INS_SET_RSA_MODULUS = (byte) 0x1A;
    private static final byte INS_SIGN_CHALLENGE = (byte) 0x1B;
    private static final byte INS_GET_RSA_STATUS = (byte) 0x1C;

    private static final short SW_SECURITY_STATUS_NOT_SATISFIED = (short) 0x6982;
    private static final short SW_AUTHENTICATION_METHOD_BLOCKED = (short) 0x6983;
    private static final short SW_INSUFFICIENT_BALANCE = (short) 0x6901;
    private static final short SW_RSA_NOT_READY = (short) 0x6A88;

    private CustomerCardApplet() {
        model = new CardModel();
        pinMgr = new PinManager();
        gameMgr = new GameManager(model.gameList, model.gameCountRef);
        cryptoMgr = new CryptoManager();
        
        // Set references
        pinMgr.setCryptoManager(cryptoMgr);
        pinMgr.setCardModel(model);
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new CustomerCardApplet().register(bArray, (short)(bOffset + 1), bArray[bOffset]);
    }

    public void process(APDU apdu) {
        if (selectingApplet()) return;

        byte[] buf = apdu.getBuffer();
        byte ins = buf[ISO7816.OFFSET_INS];

        switch (ins) {
            // PIN management
            case INS_CREATE_PIN: pinMgr.createPIN(apdu); break;
            case INS_VERIFY_PIN: verifyPIN(apdu); break;
            case INS_CHANGE_PIN: changePIN(apdu); break;
            case INS_GET_PIN_TRIES: pinMgr.getPinTries(apdu); break;
            case INS_RESET_PIN_COUNTER: pinMgr.resetPinCounter(apdu); break;
            case INS_RESET_WITH_PUK: resetWithPUK(apdu); break;
            case INS_GET_CRYPTO_INFO: getCryptoInfo(apdu); break;
            
            // Commands cn authentication v� encryption
            case INS_WRITE_INFO:
            	requireAuthenticated();
            	model.writeCustomerInfo(apdu, cryptoMgr); 
            	break;
            case INS_START_PHOTO_WRITE: 
            	requireAuthenticated(); 
            	model.startPhotoWrite(); 
            	break;
            case INS_WRITE_PHOTO_CHUNK: 
            	requireAuthenticated(); 
            	model.writePhotoChunk(apdu); 
            	break;
            case INS_FINISH_PHOTO_WRITE: 
            	requireAuthenticated(); 
            	model.finishPhotoWrite(apdu); 
            	break;
            case INS_READ_INFO: 
            	requireAuthenticated(); 
            	model.readAllData(apdu, cryptoMgr); 
            	break;
            case INS_READ_PHOTO_CHUNK: 
            	requireAuthenticated(); 
            	model.readPhotoChunk(apdu); 
            	break;
            case INS_RECHARGE_BALANCE: 
            	requireAuthenticated(); 
            	model.rechargeBalance(apdu, cryptoMgr); 
            	break;
            case INS_CHECK_BALANCE: 
            	requireAuthenticated(); 
            	model.checkBalance(apdu, cryptoMgr); 
            	break;
            case INS_MAKE_PAYMENT: 
            	requireAuthenticated(); 
            	model.makePayment(apdu, cryptoMgr); 
            	break;
            
            // Game commands kh�ng cn encryption
            case INS_ADD_OR_INCREASE_TICKETS: 
            	// requireAuthenticated(); 
            	gameMgr.addOrIncreaseTickets(apdu); 
            	break;
            case INS_DECREASE_GAME_TICKETS:  
            	gameMgr.decreaseGameTickets(apdu); 
            	break;
            case INS_READ_GAMES:  
            	gameMgr.readGames(apdu); 
            	break;
            case INS_UPDATE_GAME_TICKETS:  
            	gameMgr.updateGameTickets(apdu); 
            	break;
            case INS_FIND_GAME:  
            	gameMgr.findGame(apdu); 
            	break;
            case INS_REMOVE_GAME:  
            	gameMgr.removeGame(apdu); 
            	break;
           
            case INS_SET_CUSTOMER_ID: 
            	setCustomerID(apdu);
            	break;
            case INS_GET_CUSTOMER_ID: 
            	getCustomerID(apdu); 
            	break;
            case INS_SET_RSA_EXPONENT: 
            	setRSAExponent(apdu); 
            	break;
            case INS_SET_RSA_MODULUS: 
            	setRSAModulus(apdu); 
            	break;
            case INS_SIGN_CHALLENGE: 
            	signChallenge(apdu); 
            	break;
            case INS_GET_RSA_STATUS: 
            	getRSAStatus(apdu); 
            	break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void requireAuthenticated() {
        if (!model.isDataReady() || !cryptoMgr.isKeyReady()) {
            ISOException.throwIt(SW_SECURITY_STATUS_NOT_SATISFIED);
        }
    }

    private void verifyPIN(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();

        if (pinMgr.verify(buf, ISO7816.OFFSET_CDATA, (byte)lc)) {
            // Verify th�nh c�ng - key � ��c derive trong pinMgr.verify()
            model.setDataReady(true);
            if (!model.isDataEncrypted()) {
                model.initializeBalance(cryptoMgr);
            }
        } else {
            model.setDataReady(false);
            cryptoMgr.clearKey();
            if (pinMgr.getTriesRemaining() == 0) {
                ISOException.throwIt(SW_AUTHENTICATION_METHOD_BLOCKED);
            } else {
                ISOException.throwIt((short)0x6A80);
            }
        }
    }

    private void changePIN(APDU apdu) {
        // Must be authenticated first
        if (!pinMgr.isPINValidated()) {
            ISOException.throwIt((short)0x6985);  // Conditions of use not satisfied - must verify PIN first
        }
        pinMgr.changePIN(apdu);
        // Reset authentication state after PIN change
        model.setDataReady(false);
        cryptoMgr.clearKey();
    }

    private void resetWithPUK(APDU apdu) {
        pinMgr.resetWithPUK(apdu);
        model.setDataReady(false); // require verify with new PIN before crypto ops
        cryptoMgr.clearKey();
    }
    
    /**
     * Command mi: Ly IV v� Salt � client bit (cn cho testing/debugging)
     * Trong production c� th b command n�y
     */
    private void getCryptoInfo(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        byte[] iv = model.getIV();
        byte[] salt = model.getSalt();
        
        // Return: [16 bytes IV][16 bytes Salt]
        Util.arrayCopyNonAtomic(iv, (short)0, buf, (short)0, (short)16);
        Util.arrayCopyNonAtomic(salt, (short)0, buf, (short)16, (short)16);
        
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)32);
        apdu.sendBytesLong(buf, (short)0, (short)32);
    }
    
    // ========== NEW RSA COMMANDS ==========
    
    /**
     * INS_SET_CUSTOMER_ID (0x90)
     * L�u customer ID (plain text, ti �a 15 bytes)
     * Input: [customer_id bytes]
     * Output: 9000 success
     */
    private void setCustomerID(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        
        if (lc > 15) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        model.setCustomerID(buf, ISO7816.OFFSET_CDATA, (byte)lc);
        
        // Send response 9000
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)0);
    }
    
    /**
     * INS_GET_CUSTOMER_ID (0x91)
     * �c customer ID (plain text, KH�NG cn PIN)
     * Input: none
     * Output: [15 bytes customer_id]
     */
    private void getCustomerID(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        
        model.getCustomerID(buf, (short)0);
        
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)15);
        apdu.sendBytesLong(buf, (short)0, (short)15);
    }
    
    /**
     * INS_SET_RSA_EXPONENT (0x92)
     * L�u RSA private key exponent (ch gi 1 ln khi to th)
     * Input: [exponent bytes] (128 bytes cho RSA-1024)
     * Output: 9000 success
     */
    private void setRSAExponent(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        
        if (lc != 128) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        model.setRSAExponent(buf, ISO7816.OFFSET_CDATA, lc);        
        // Send response 9000
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)0);    }
    
    /**
     * INS_SET_RSA_MODULUS (0x93)
     * L�u RSA private key modulus (ch gi 1 ln khi to th)
     * Input: [modulus bytes] (128 bytes cho RSA-1024)
     * Output: 9000 success
     */
    private void setRSAModulus(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        
        if (lc != 128) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        model.setRSAModulus(buf, ISO7816.OFFSET_CDATA, lc);        
        // Send response 9000
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)0);    }
    
    /**
     * INS_SIGN_CHALLENGE (0x94)
     * Sign challenge vi RSA private key (SHA1withRSA)
     * Input: [challenge bytes] (32 bytes)
     * Output: [signature bytes] (128 bytes cho RSA-1024)
     * 
     * Flow:
     * 1. Terminal nhn challenge t server (32 bytes)
     * 2. Terminal gi challenge xung card
     * 3. Card sign vi private key
     * 4. Return signature (128 bytes)
     * 5. Terminal gi signature l�n server verify
     */
    private void signChallenge(APDU apdu) {
        if (!model.isRSAKeyReady()) {
            ISOException.throwIt(SW_RSA_NOT_READY);
        }
        
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();
        
        if (lc != 32) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        
        // Sign challenge (input ti OFFSET_CDATA, output ti offset 0)
        short sigLen = model.signChallenge(
            buf, ISO7816.OFFSET_CDATA, (short)32,  // challenge input
            buf, (short)0                           // signature output
        );
        
        // Return signature
        apdu.setOutgoing();
        apdu.setOutgoingLength(sigLen);
        apdu.sendBytesLong(buf, (short)0, sigLen);
    }
    
    /**
     * INS_GET_RSA_STATUS (0x95)
     * Kim tra trng th�i RSA key
     * Output: [1 byte status]
     *   0x00 = RSA not ready
     *   0x01 = RSA ready
     */
    private void getRSAStatus(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        buf[0] = model.isRSAKeyReady() ? (byte)0x01 : (byte)0x00;
        
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)1);
        apdu.sendBytes((short)0, (short)1);
    }
    
    public void deselect() {
        // Clear key v� reset dataReady khi deselect
        if (model.isDataReady()) {
            cryptoMgr.clearKey();
            model.setDataReady(false);
        }
    }
}