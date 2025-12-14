package ParkCard;

import javacard.framework.*;

public class CustomerCardApplet extends Applet {

    private CardModel model;
    private PinManager pinMgr;
    private CryptoManager cryptoMgr;
    private GameManager gameMgr;

    // Instruction codes
    private static final byte INS_CREATE_PIN                  = (byte) 0x40;
    private static final byte INS_VERIFY_PIN                  = (byte) 0x20;
    private static final byte INS_CHANGE_PIN                  = (byte) 0x24;
    private static final byte INS_GET_PIN_TRIES               = (byte) 0x41;
    private static final byte INS_RESET_PIN_COUNTER           = (byte) 0x42;

    private static final byte INS_WRITE_INFO                  = (byte) 0x01;
    private static final byte INS_START_PHOTO_WRITE           = (byte) 0x04;
    private static final byte INS_WRITE_PHOTO_CHUNK           = (byte) 0x02;
    private static final byte INS_FINISH_PHOTO_WRITE          = (byte) 0x06;
    private static final byte INS_READ_INFO                   = (byte) 0x03;
    private static final byte INS_READ_PHOTO_CHUNK            = (byte) 0x05;

    private static final byte INS_RECHARGE_BALANCE            = (byte) 0x50;
    private static final byte INS_CHECK_BALANCE               = (byte) 0x51;
    private static final byte INS_MAKE_PAYMENT                = (byte) 0x52;

    private static final byte INS_ADD_OR_INCREASE_TICKETS     = (byte) 0x60;
    private static final byte INS_DECREASE_GAME_TICKETS       = (byte) 0x61;
    private static final byte INS_READ_GAMES                  = (byte) 0x62;
    private static final byte INS_UPDATE_GAME_TICKETS         = (byte) 0x63;
    private static final byte INS_FIND_GAME                   = (byte) 0x64;
    private static final byte INS_REMOVE_GAME                 = (byte) 0x65;

    private static final short SW_SECURITY_STATUS_NOT_SATISFIED = (short) 0x6982;
    private static final short SW_AUTHENTICATION_METHOD_BLOCKED = (short) 0x6983;
    private static final short SW_INSUFFICIENT_BALANCE         = (short) 0x6901;

    private CustomerCardApplet() {
        model = new CardModel();
        model.generateRandomSalt();
        pinMgr = new PinManager();
        cryptoMgr = new CryptoManager(model.getSalt());
        gameMgr = new GameManager(model.gameList, model.gameCountRef);
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new CustomerCardApplet().register(bArray, (short)(bOffset + 1), bArray[bOffset]);
    }

    public void process(APDU apdu) {
        if (selectingApplet()) return;

        byte[] buf = apdu.getBuffer();
        byte ins = buf[ISO7816.OFFSET_INS];

        switch (ins) {
            case INS_CREATE_PIN:                  pinMgr.createPIN(apdu); break;
            case INS_VERIFY_PIN:                  verifyPIN(apdu); break;
            case INS_CHANGE_PIN:                  changePIN(apdu); break;
            case INS_GET_PIN_TRIES:               pinMgr.getPinTries(apdu); break;
            case INS_RESET_PIN_COUNTER:           pinMgr.resetPinCounter(apdu); break;

            case INS_WRITE_INFO:                  requireAuthenticated(); model.writeCustomerInfo(apdu); break;
            case INS_START_PHOTO_WRITE:           requireAuthenticated(); model.startPhotoWrite(); break;
            case INS_WRITE_PHOTO_CHUNK:           requireAuthenticated(); model.writePhotoChunk(apdu); break;
            case INS_FINISH_PHOTO_WRITE:          requireAuthenticated(); model.finishPhotoWrite(apdu); break;
            case INS_READ_INFO:                   requireAuthenticated(); model.readAllData(apdu); break;
            case INS_READ_PHOTO_CHUNK:            requireAuthenticated(); model.readPhotoChunk(apdu); break;

            case INS_RECHARGE_BALANCE:            requireAuthenticated(); model.rechargeBalance(apdu); break;
            case INS_CHECK_BALANCE:               requireAuthenticated(); model.checkBalance(apdu); break;
            case INS_MAKE_PAYMENT:                requireAuthenticated(); model.makePayment(apdu); break;

            case INS_ADD_OR_INCREASE_TICKETS:     requireAuthenticated(); gameMgr.addOrIncreaseTickets(apdu); break;
            case INS_DECREASE_GAME_TICKETS:       requireAuthenticated(); gameMgr.decreaseGameTickets(apdu); break;
            case INS_READ_GAMES:                  requireAuthenticated(); gameMgr.readGames(apdu); break;
            case INS_UPDATE_GAME_TICKETS:         requireAuthenticated(); gameMgr.updateGameTickets(apdu); break;
            case INS_FIND_GAME:                   requireAuthenticated(); gameMgr.findGame(apdu); break;
            case INS_REMOVE_GAME:                 requireAuthenticated(); gameMgr.removeGame(apdu); break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void requireAuthenticated() {
        if (!model.isDataReady()) {
            ISOException.throwIt(SW_SECURITY_STATUS_NOT_SATISFIED);
        }
    }

    private void verifyPIN(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short lc = apdu.setIncomingAndReceive();

        if (pinMgr.verify(buf, ISO7816.OFFSET_CDATA, (byte)lc)) {
            // Derive key t PIN và decrypt toàn b d liu
            cryptoMgr.deriveKeyFromPIN(buf, ISO7816.OFFSET_CDATA, (byte)lc);
            model.decryptAll(cryptoMgr);
            model.setDataReady(true);
        } else {
            model.setDataReady(false);
            if (pinMgr.getTriesRemaining() == 0) {
                ISOException.throwIt(SW_AUTHENTICATION_METHOD_BLOCKED);
            } else {
                ISOException.throwIt((short)0x6A80);
            }
        }
    }

    private void changePIN(APDU apdu) {
        // Trýc khi ði PIN: m hóa li d liu bng khóa c
        if (model.isDataReady()) {
            model.encryptAll(cryptoMgr);
        }

        pinMgr.changePIN(apdu);

        // Sau khi ði PIN thành công, verify li PIN mi ð derive key mi và decrypt
        // (APDU cha old + new PIN, ta ly new PIN t buffer ð verify li)
        byte[] buf = apdu.getBuffer();
        byte oldLen = buf[ISO7816.OFFSET_CDATA];
        short newOffset = (short)(ISO7816.OFFSET_CDATA + 1 + oldLen + 1);
        byte newLen = buf[(short)(ISO7816.OFFSET_CDATA + 1 + oldLen)];

        cryptoMgr.deriveKeyFromPIN(buf, newOffset, newLen);
        model.decryptAll(cryptoMgr);
        model.setDataReady(true);
    }
}