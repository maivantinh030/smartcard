package ParkCard;
import javacard.framework.*;

public class CustomerCardApplet extends Applet {
    private CardModel model;
    private PinManager pinMgr;
    private GameManager gameMgr;
    private CryptoManager cryptoMgr;

    // Instruction codes
    private static final byte INS_CREATE_PIN = (byte) 0x40;
    private static final byte INS_VERIFY_PIN = (byte) 0x20;
    private static final byte INS_CHANGE_PIN = (byte) 0x24;
    private static final byte INS_GET_PIN_TRIES = (byte) 0x41;
    private static final byte INS_RESET_PIN_COUNTER = (byte) 0x42;
    private static final byte INS_WRITE_INFO = (byte) 0x01;
    private static final byte INS_START_PHOTO_WRITE = (byte) 0x04;
    private static final byte INS_WRITE_PHOTO_CHUNK = (byte) 0x02;
    private static final byte INS_FINISH_PHOTO_WRITE = (byte) 0x06;
    private static final byte INS_READ_INFO = (byte) 0x03;
    private static final byte INS_READ_PHOTO_CHUNK = (byte) 0x05;
    private static final byte INS_RECHARGE_BALANCE = (byte) 0x50;
    private static final byte INS_CHECK_BALANCE = (byte) 0x51;
    private static final byte INS_MAKE_PAYMENT = (byte) 0x52;
    private static final byte INS_ADD_OR_INCREASE_TICKETS = (byte) 0x60;
    private static final byte INS_DECREASE_GAME_TICKETS = (byte) 0x61;
    private static final byte INS_READ_GAMES = (byte) 0x62;
    private static final byte INS_UPDATE_GAME_TICKETS = (byte) 0x63;
    private static final byte INS_FIND_GAME = (byte) 0x64;
    private static final byte INS_REMOVE_GAME = (byte) 0x65;
    
    // New commands
    private static final byte INS_GET_CRYPTO_INFO = (byte) 0x70;  // Ly IV v� Salt

    private static final short SW_SECURITY_STATUS_NOT_SATISFIED = (short) 0x6982;
    private static final short SW_AUTHENTICATION_METHOD_BLOCKED = (short) 0x6983;
    private static final short SW_INSUFFICIENT_BALANCE = (short) 0x6901;

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
            case INS_CREATE_PIN: pinMgr.createPIN(apdu); break;
            case INS_VERIFY_PIN: verifyPIN(apdu); break;
            case INS_CHANGE_PIN: changePIN(apdu); break;
            case INS_GET_PIN_TRIES: pinMgr.getPinTries(apdu); break;
            case INS_RESET_PIN_COUNTER: pinMgr.resetPinCounter(apdu); break;
            case INS_GET_CRYPTO_INFO: getCryptoInfo(apdu); break;
            
            // Commands cn authentication v� encryption
            case INS_WRITE_INFO: requireAuthenticated(); model.writeCustomerInfo(apdu, cryptoMgr); break;
            case INS_START_PHOTO_WRITE: requireAuthenticated(); model.startPhotoWrite(); break;
            case INS_WRITE_PHOTO_CHUNK: requireAuthenticated(); model.writePhotoChunk(apdu); break;
            case INS_FINISH_PHOTO_WRITE: requireAuthenticated(); model.finishPhotoWrite(apdu); break;
            case INS_READ_INFO: requireAuthenticated(); model.readAllData(apdu, cryptoMgr); break;
            case INS_READ_PHOTO_CHUNK: requireAuthenticated(); model.readPhotoChunk(apdu); break;
            case INS_RECHARGE_BALANCE: requireAuthenticated(); model.rechargeBalance(apdu, cryptoMgr); break;
            case INS_CHECK_BALANCE: requireAuthenticated(); model.checkBalance(apdu, cryptoMgr); break;
            case INS_MAKE_PAYMENT: requireAuthenticated(); model.makePayment(apdu, cryptoMgr); break;
            
            // Game commands kh�ng cn encryption
            case INS_ADD_OR_INCREASE_TICKETS: requireAuthenticated(); gameMgr.addOrIncreaseTickets(apdu); break;
            case INS_DECREASE_GAME_TICKETS: requireAuthenticated(); gameMgr.decreaseGameTickets(apdu); break;
            case INS_READ_GAMES: requireAuthenticated(); gameMgr.readGames(apdu); break;
            case INS_UPDATE_GAME_TICKETS: requireAuthenticated(); gameMgr.updateGameTickets(apdu); break;
            case INS_FIND_GAME: requireAuthenticated(); gameMgr.findGame(apdu); break;
            case INS_REMOVE_GAME: requireAuthenticated(); gameMgr.removeGame(apdu); break;
            
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
        pinMgr.changePIN(apdu);
        model.setDataReady(true);
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

    public void deselect() {
        // Clear key v� reset dataReady khi deselect
        if (model.isDataReady()) {
            cryptoMgr.clearKey();
            model.setDataReady(false);
        }
    }
}