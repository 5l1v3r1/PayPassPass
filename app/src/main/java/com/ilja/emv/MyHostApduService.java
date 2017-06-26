package com.ilja.emv;

/**
 * Created by iljab on 15.06.2017.
 */

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/*! Card emulation class */


public class MyHostApduService extends HostApduService {

    private boolean mode;
    private String ppse;                /*!< string with ppse response to POS */
    private String card_application;    /*!< string with card application response to POS */
    private String processing_options;  /*!< string with processing options response to POS */
    private String records;             /*!< string with records  response to POS */
    private String[] crypto_checksum;   /*!< array of string with cryptographic checksum response to POS for any possible UN */

    private String record1;
    private String record2;
    private String record3;
    private String record4;
    private String generateAC;


    private final String MagStripe = "MagStripe";
    private final String MChip = "MChip";

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {


        Log.i("EMVemulator", "Emulator started, processCommandApdu");
        Log.i("EMVemulator", "Received :" + fromByte2Hex(apdu));

        if (ppse == null)
            getCardData();
        if (!mode) {
            if (apdu[0] == (byte) 0 && apdu[1] == (byte) 0xa4 && apdu[2] == (byte) 0x04 && apdu[3] == (byte) 0x00 && apdu[4] == (byte) 0x0E) {
                Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));
                appendLog("Received: " + fromByte2Hex(apdu));
                appendLog("Sended: " + ppse + "\n");
                return fromHex2Byte(ppse);
            }
            if (apdu[0] == (byte) 0 && apdu[1] == (byte) 0xa4 && apdu[2] == (byte) 0x04 && apdu[3] == (byte) 0x00 && apdu[4] == (byte) 0x07) {
                Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));
                appendLog("Received: " + fromByte2Hex(apdu));
                appendLog("Sended: " + card_application + "\n");
                return fromHex2Byte(card_application);
            }
            if (apdu[0] == (byte) 0x80 && apdu[1] == (byte) 0xa8 && apdu[2] == (byte) 0x00 && apdu[3] == (byte) 0x00 && apdu[4] == (byte) 0x02) {
                Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));
                appendLog("Received: " + fromByte2Hex(apdu));
                appendLog("Sended: " + processing_options + "\n");
                return fromHex2Byte(processing_options);
            }
            if (apdu[0] == (byte) 0 && apdu[1] == (byte) 0xb2 && apdu[2] == (byte) 0x01 && apdu[3] == (byte) 0x0c && apdu[4] == (byte) 0x00) {
                Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));
                appendLog("Received: " + fromByte2Hex(apdu));
                appendLog("Sended: " + records + "\n");
                return fromHex2Byte(records);
            }

            if (apdu[0] == (byte) 0x80 && apdu[1] == (byte) 0x2a && apdu[2] == (byte) 0x8e && apdu[3] == (byte) 0x80 && apdu[4] == (byte) 0x04) {
                Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));
                appendLog("Received: " + fromByte2Hex(apdu));
                int i = Integer.parseInt(fromByte2Hex(apdu).replaceAll("\\s+", "").substring(15, 18));
                Log.i("EMVemulator", "Pozor: " + String.valueOf(i));
                appendLog("Pozor: " + String.valueOf(i));
                appendLog("Sended: " + crypto_checksum[i] + "\n");
                return fromHex2Byte(crypto_checksum[i]);
            } else {
                Log.i("EMVemulator", "else-Received: " + fromByte2Hex(apdu) + ";");
                appendLog("else-Received: " + fromByte2Hex(apdu) + ";");
                appendLog("Sended: " + "6A 82" + "\n");

                appendLog("===============================================\n");
                return fromHex2Byte("6A 82");
            }
        } else {
            if (apdu[0] == (byte) 0 && apdu[1] == (byte) 0xa4 && apdu[2] == (byte) 0x04 && apdu[3] == (byte) 0x00 && apdu[4] == (byte) 0x0E) {
                Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));
                appendLog("Received: " + fromByte2Hex(apdu));
                appendLog("Sended: " + ppse + "\n");
                return fromHex2Byte(ppse);
            }
            if (apdu[0] == (byte) 0 && apdu[1] == (byte) 0xa4 && apdu[2] == (byte) 0x04 && apdu[3] == (byte) 0x00 && apdu[4] == (byte) 0x07) {
                Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));
                appendLog("Received: " + fromByte2Hex(apdu));
                appendLog("Sended: " + card_application + "\n");
                return fromHex2Byte(card_application);
            }
            if (apdu[0] == (byte) 0x80 && apdu[1] == (byte) 0xa8 && apdu[2] == (byte) 0x00 && apdu[3] == (byte) 0x00 && apdu[4] == (byte) 0x02) {
                Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));
                appendLog("Received: " + fromByte2Hex(apdu));
                appendLog("Sended: " + processing_options + "\n");
                return fromHex2Byte(processing_options);
            }
            if (apdu[0]==(byte)0x00 && apdu[1]==(byte)0xb2 && apdu[2]==(byte)0x01  && apdu[3]==(byte)0x14  ) {
                Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));
                appendLog("Received: " + fromByte2Hex(apdu));
                appendLog("Sended: " + record1 + "\n");
                return fromHex2Byte(record1);
            }
            if (apdu[0]==(byte)0x00 && apdu[1]==(byte)0xb2 && apdu[2]==(byte)0x01  && apdu[3]==(byte)0x1c  ) {
                Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));
                appendLog("Received: " + fromByte2Hex(apdu));
                appendLog("Sended: " + record2 + "\n");
                return fromHex2Byte(record2);
            }
            if (apdu[0]==(byte)0x00 && apdu[1]==(byte)0xb2 && apdu[2]==(byte)0x01  && apdu[3]==(byte)0x24  ) {
                Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));
                appendLog("Received: " + fromByte2Hex(apdu));
                appendLog("Sended: " + record3 + "\n");
                return fromHex2Byte(record3);
            }
            if (apdu[0]==(byte)0x00 && apdu[1]==(byte)0xb2 && apdu[2]==(byte)0x02  && apdu[3]==(byte)0x24  ) {
                Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));
                appendLog("Received: " + fromByte2Hex(apdu));
                appendLog("Sended: " + record4 + "\n");
                return fromHex2Byte(record4);
            }
            if (apdu[0]==(byte)0x80 && apdu[1]==(byte)0xAe && apdu[2]==(byte)0x50  && apdu[3]==(byte)0x00  ) {
                Log.i("EMVemulator", "Received: " + fromByte2Hex(apdu));
                appendLog("Received: " + fromByte2Hex(apdu));
                appendLog("Sended: " + generateAC + "\n");
                return fromHex2Byte(generateAC);
            }
        }

        return fromHex2Byte(ppse);
    }


    public void appendLog(String text) {
        File logFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/EMV/EMV.log");
        Log.i("EMVemulator", Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMV/EMV.log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void getCardData() {

        File cardFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/EMV/active_card");
        crypto_checksum = new String[1000];


        BufferedReader myReader = null;
        try {
            myReader = new BufferedReader(new FileReader(cardFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {

            String line = myReader.readLine();

            if (line.equals(MagStripe)) {

                mode = false;
                Log.i("EMVEmulator", "MagStripe Mode");
                ppse = myReader.readLine();
                //  Log.i("EMVemulator", "Read: " + ppse);
                card_application = myReader.readLine();
                //   Log.i("EMVemulator", "Read: " + card_application);
                processing_options = myReader.readLine();
                //  Log.i("EMVemulator", "Read: " + processing_options);
                records = myReader.readLine();
                //  Log.i("EMVemulator", "Read: " + records);

                for (int i = 0; i < 1000; i++) {
                    crypto_checksum[i] = myReader.readLine();
                    //     Log.i("EMVemulator", "Read: " + crypto_checksum[i]);
                }


            } else if (line.equals(MChip)) {
                mode = true;
                ppse = myReader.readLine();
                //  Log.i("EMVemulator", "Read: " + ppse);
                card_application = myReader.readLine();
                //   Log.i("EMVemulator", "Read: " + card_application);
                processing_options = myReader.readLine();
                record1 = myReader.readLine();
                record2 = myReader.readLine();
                record3 = myReader.readLine();
                record4 = myReader.readLine();
                generateAC = myReader.readLine();
            }


            myReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static byte[] fromHex2Byte(String digits) {
        /*!
            This method converts strings in hex to bytes
         */
        digits = digits.replace(" ", "");
        final int bytes = digits.length() / 2;
        byte[] result = new byte[bytes];
        for (int i = 0; i < digits.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(digits.substring(i, i + 2), 16);
        }
        return result;
    }

    static protected String fromByte2Hex(byte[] input) {
         /*!
            This method converts bytes to strings of hex
         */
        StringBuilder result = new StringBuilder();
        for (Byte inputbyte : input) {
            result.append(String.format("%02X" + " ", inputbyte));
        }
        return result.toString();
    }

    @Override
    public void onDeactivated(int reason) {
        /*!
            This method will be called in two possible scenarios:
            - The NFC link has been deactivated or lost
            - A different AID has been selected and was resolved to a different service component
         */
        Log.i("EMVemulator", "Deactivated: " + reason);
    }
}