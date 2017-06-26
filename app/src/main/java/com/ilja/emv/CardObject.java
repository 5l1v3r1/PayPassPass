package com.ilja.emv;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by iljab on 19.06.2017.
 */

public class CardObject {


    public boolean mode;
    public String ppse;                /*!< string with ppse response to POS */
    public String card_application;    /*!< string with card application response to POS */
    public String processing_options;  /*!< string with processing options response to POS */
    public String records;             /*!< string with records  response to POS */
    public String[] crypto_checksum;
    public boolean state;
    public String filename;


    public String record1;
    public String record2;
    public String record3;
    public String record4;
    public String generateAC;

    public String getRecord1() {
        return record1;
    }

    public void setRecord1(String record1) {
        this.record1 = record1;
    }

    public String getRecord2() {
        return record2;
    }

    public void setRecord2(String record2) {
        this.record2 = record2;
    }

    public String getRecord3() {
        return record3;
    }

    public void setRecord3(String record3) {
        this.record3 = record3;
    }

    public String getRecord4() {
        return record4;
    }

    public void setRecord4(String record4) {
        this.record4 = record4;
    }

    public String getGenerateAC() {
        return generateAC;
    }

    public void setGenerateAC(String generateAC) {
        this.generateAC = generateAC;
    }


    public CardObject() {
    }

    public boolean isMode() {
        return mode;
    }

    public void setMode(boolean mode) {
        this.mode = mode;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


    public String getPpse() {
        return ppse;
    }

    public void setPpse(String ppse) {
        this.ppse = ppse;
    }

    public String getCard_application() {
        return card_application;
    }

    public void setCard_application(String card_application) {
        this.card_application = card_application;
    }

    public String getProcessing_options() {
        return processing_options;
    }

    public void setProcessing_options(String processing_options) {
        this.processing_options = processing_options;
    }

    public String getRecords() {
        return records;
    }

    public void setRecords(String records) {
        this.records = records;
    }

    public String[] getCrypto_checksum() {
        return crypto_checksum;
    }

    public void setCrypto_checksum(String[] crypto_checksum) {
        this.crypto_checksum = crypto_checksum;
    }


    public String getType() {
        Log.i("EMVEmulator PPSE:", ppse);
        if (ppse.indexOf("A0 00 00 00 04 10") != 0)
            return "MasterCard";
        if (ppse.indexOf("A0 00 00 00 03 20") != 0)
            return "Visa Electron";
        if (ppse.indexOf("A0 00 00 00 03 10") != 0)
            return "Visa";

        return "Undefined";
    }

    public String getNumber() {

        if (!mode) {
            return new String(Arrays.copyOfRange(fromHex2Byte(records), 28, 44));
        }

        return "MChip";
    }

    public String getExp() {
        if (!mode) {
            return new String(Arrays.copyOfRange(fromHex2Byte(records), 50, 52)) + "/" + new String(Arrays.copyOfRange(fromHex2Byte(records), 48, 50));
        }

        return "MChip";
    }

    private static byte[] fromHex2Byte(String digits) {
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

}
