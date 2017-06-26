package com.ilja.emv;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

/*! Main Activity class with inner Card reading class*/

public class MainActivity extends Activity {

    private NfcAdapter nfcAdapter;                                                              /*!< represents the local NFC adapter */
    private Tag tag;                                                                            /*!< represents an NFC tag that has been discovered */
    private IsoDep tagcomm;                                                                     /*!< provides access to ISO-DEP (ISO 14443-4) properties and I/O operations on a Tag */
    private String[][] nfctechfilter = new String[][]{new String[]{NfcA.class.getName()}};      /*!<  NFC tech lists */
    private PendingIntent nfcintent;                                                            /*!< reference to a token maintained by the system describing the original data used to retrieve it */
    private ListView listView;
    private static String cardsStorage = "";
    private ArrayList<CardObject> allCards;
    private TextView statusLog;
    private Button readCard;
    private boolean statusRead;
    private CustomAdapter adapter;
    private final String MagStripe = "MagStripe";
    private final String MChip = "MChip";
    private boolean mode ; // false - MagStripe ; True - MChip
    private Switch  mSwitch ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        isStoragePermissionGranted();
        statusRead = false;
        setContentView(R.layout.activity_main);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcintent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        listView = (ListView) findViewById(R.id.listView);
        statusLog = (TextView) findViewById(R.id.textStatus);
        cardsStorage = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMV/";
        mSwitch = (Switch) findViewById(R.id.switch1);


        readCard = (Button) findViewById(R.id.read_card_btn);

        File emvDir = new File(cardsStorage);
        emvDir.mkdirs();

        LoadCardStorage(true);
    }


    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if ((checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)) {

                Log.v(TAG, "Permission is granted");

                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
           }
    }

    public void readCard(View v) {

        changeState();

    }


    public void changeState() {
        if (!statusRead) {
            statusRead = true;
            readCard.setText("Stop read");
            nfcAdapter.enableForegroundDispatch(this, nfcintent, null, nfctechfilter);
        } else {
            statusRead = false;
            readCard.setText("Read card");
            nfcAdapter.disableReaderMode(this);
        }
    }

    private void LoadCardStorage(boolean reset) {
        if (reset) {
            allCards = ReadCardStorage();
        }
        adapter = new CustomAdapter(allCards, getApplicationContext());

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CardObject dataModel = allCards.get(position);
                for (int i = 0; i < allCards.size(); i++) {
                    allCards.get(i).setState(false);
                    if (i == position) {
                        allCards.get(i).setState(true);
                    }
                }
                Log.i("Main", dataModel.getNumber());
                writeActiveCard(dataModel.filename);
                LoadCardStorage(false);

            }
        });
    }


    protected void onNewIntent(Intent intent) {
        if (statusRead) {
            super.onNewIntent(intent);
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.i("EMVemulator", "Tag detected");
            new CardReader().execute(tag);
        }
    }

    public void changeMode(View view) {

        if (mSwitch.isChecked()) {
            mode = true;
            mSwitch.setChecked(true);
            mSwitch.setText("MChip");
        } else {
            mode = false;
            mSwitch.setChecked(false);
            mSwitch.setText("MagStripe");

        }
    }

    private class CardReader extends AsyncTask<Tag, String, String> {
        String cardtype;            /*!< string with card type */
        String cardnumber;          /*!< string with card number */
        String cardexpiration;      /*!< string with card expiration*/
        String error;               /*!< string with error value */

        @Override
        protected String doInBackground(Tag... params) {
            /*!
                This method performs background computation (Reading card data) that can take a long time ( up to 60-90 sec)
             */

            Tag tag = params[0];
            tagcomm = IsoDep.get(tag);
            try {
                tagcomm.connect();
            } catch (IOException e) {
                Log.i("EMVemulator", "Error tagcomm: " + e.getMessage());
                error = "Reading card data ... Error tagcomm: " + e.getMessage();
                return null;
            }
            try {
                if (!mode) {
                readCardMagStripe(); }
                else {
                    readCardMChip();
                }
                tagcomm.close();
            } catch (IOException e) {
                Log.i("EMVemulator", "Error tranceive: " + e.getMessage());
                error = "Reading card data ... Error tranceive: " + e.getMessage();
                return null;
            }
            return null;
        }

        private void readCardMagStripe() {

            final ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
            final TextView txtLog = (TextView)findViewById(R.id.textStatus);
            pb.setProgress(0);

            try {
                String temp;

                long unixTime = System.currentTimeMillis() / 1000L;
                String filename = String.valueOf(unixTime) + "MagStripe.card";

                byte[] recv = transceive("00 A4 04 00 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 00");
                appendLog("MagStripe",filename);
                appendLog(Byte2Hex(recv), filename);
                temp = "00 A4 04 00 07";
                temp += Byte2Hex(recv).substring(80, 102);
                temp += "00";
                if (temp.matches("00 A4 04 00 07 A0 00 00 00 04 10 10 00"))
                    cardtype = "MasterCard";
                if (temp.matches("00 A4 04 00 07 A0 00 00 00 03 20 10 00"))
                    cardtype = "Visa Electron";
                if (temp.matches("00 A4 04 00 07 A0 00 00 00 03 10 10 00"))
                    cardtype = "Visa";
                recv = transceive(temp);

                appendLog(Byte2Hex(recv), filename);

                appendLog(toMagStripeMode(), filename);
                recv = transceive("00 B2 01 0C 00");

                appendLog(Byte2Hex(recv), filename);
                if (cardtype == "MasterCard") {
                    cardnumber = "Card number: " + new String(Arrays.copyOfRange(recv, 28, 44));
                    cardexpiration = "Card expiration: " + new String(Arrays.copyOfRange(recv, 50, 52)) + "/" + new String(Arrays.copyOfRange(recv, 48, 50));

                    for (int i = 0; i < 1000; i++) {
                        recv = transceive("80 A8 00 00 02 83 00 00");
                        temp = "802A8E800400000";
                        temp += String.format("%03d", i);
                        temp += "00";
                        temp = temp.replaceAll("..(?!$)", "$0 ");
                        recv = transceive(temp);

                        appendLog(Byte2Hex(recv), filename);
                        if (i % 1 == 0) {
                           pb.setProgress( i);
                          /*  String text = String.valueOf(i) + "\n" + txtLog.getText();

                            txtLog.setText(text);*/
                            Log.i("EMVemulator", "Count:"+String.valueOf(i));
                        }
                    }
                }
                if (cardtype == "Visa" || cardtype == "Visa Electron") {
                    cardnumber = "Card number: " + Byte2Hex(recv).substring(12, 36).replaceAll(" ", "");
                    cardexpiration = "Card expiration: " + Byte2Hex(recv).substring(40, 43).replaceAll(" ", "") + "/" + Byte2Hex(recv).substring(37, 40).replaceAll(" ", "");
                }

                Log.i("EMVemulator", "Done!");

            } catch (IOException e) {
                Log.i("EMVemulator", "Error readCard: " + e.getMessage());
                error = "Reading card data ... Error readCard: " + e.getMessage();
            }
        }

        private void readCardMChip() {

            final ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
            pb.setProgress(0);

            try {
                String temp;

                long unixTime = System.currentTimeMillis() / 1000L;
                String filename = String.valueOf(unixTime) + "-MChip.card";

                //byte[] recv = transceive("00 A4 04 00 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 00");

               byte[] recv = transceive("00 A4 04 00 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 00"); //Get PPSE
                appendLog("MChip",filename);
                appendLog(Byte2Hex(recv), filename);

                temp = "00 A4 04 00 07";
                temp += Byte2Hex(recv).substring(80, 102);
                temp += "00";

                recv = transceive(temp);  //SELECT Command
                appendLog(Byte2Hex(recv), filename);


                recv = transceive("80 A8 00 00 02 83 00 00");  //Get Processing Options
                appendLog(Byte2Hex(recv), filename);

                recv = transceive("00 B2 01 14 00 00");   //Read Record1
                appendLog(Byte2Hex(recv), filename);

                recv = transceive("00 B2 01 1C 00 00");   //Read Record2
                appendLog(Byte2Hex(recv), filename);

                recv = transceive("00 B2 01 24 00 00");   //Read Record3
                appendLog(Byte2Hex(recv), filename);

                recv = transceive("00 B2 02 24 00 00");   //Read Record4
                appendLog(Byte2Hex(recv), filename);


                 recv = transceive("80 AE 50 00 2B 00 00 00 00 00 00 00 00 00 00 00 00 00 56 00 00 00 00 00 09 78 17 06 21 00 51 33 05 49 22 00 00 00 00 00 00 00 00 00 00 1F 03 02 00");   //Generate AC
                appendLog(Byte2Hex(recv), filename);


                Log.i("EMVemulator", "Done!");

            } catch (IOException e) {
                Log.i("EMVemulator", "Error readCard: " + e.getMessage());
                error = "Reading card data ... Error readCard: " + e.getMessage();
            }
        }

        public void appendLog(String text, String filename) {
            File logFile = new File(cardsStorage, filename);
            Log.i("EMVemulator", cardsStorage + filename);

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

        protected byte[] transceive(String hexstr) throws IOException {

            String[] hexbytes = hexstr.split("\\s");
            byte[] bytes = new byte[hexbytes.length];
            for (int i = 0; i < hexbytes.length; i++) {
                bytes[i] = (byte) Integer.parseInt(hexbytes[i], 16);
            }
            Log.i("EMVemulator", "Send: " + Byte2Hex(bytes));
            byte[] recv = tagcomm.transceive(bytes);
            Log.i("EMVemulator", "Received: " + Byte2Hex(recv));
            return recv;
        }


        protected String Byte2Hex(byte[] input) {

            StringBuilder result = new StringBuilder();
            for (Byte inputbyte : input) {
                result.append(String.format("%02X" + " ", inputbyte));
            }
            return result.toString();
        }

        protected String toMagStripeMode() {

            return "77 0A 82 02 00 00 94 04 08 01 01 00 90 00";
        }


        protected void onPreExecute() {
            appendToViewLog("Card detected!");
        }

        protected void onPostExecute(String result) {

            changeState();
            appendToViewLog("Reading card data ... completed");
            appendToViewLog(cardtype);
            appendToViewLog(cardnumber);
            appendToViewLog(cardexpiration);
            LoadCardStorage(true);
        }

    }


    public ArrayList<CardObject> ReadCardStorage() {
        ArrayList<CardObject> cards = new ArrayList<>();

        File dir = new File(cardsStorage);
        File[] filelist = dir.listFiles();

        for (int i = 0; i < filelist.length; i++) {

            if (filelist[i].getName().endsWith(".card")) {
                cards.add(getCardData(cardsStorage, filelist[i].getName()));
            }
        }

        return cards;
    }


    public CardObject getCardData(String path, String filename) {

        CardObject card = new CardObject();


        File cardFile = new File(path, filename);

        BufferedReader myReader = null;
        try {
            myReader = new BufferedReader(new FileReader(cardFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {


            String line = myReader.readLine();

            if (line.equals(MagStripe)) {
                String ppse = "";                /*!< string with ppse response to POS */
                String card_application = "";    /*!< string with card application response to POS */
                String processing_options = "";  /*!< string with processing options response to POS */
                String records = "";             /*!< string with records  response to POS */
                String[] crypto_checksum;
                crypto_checksum = new String[1000];
                ppse = myReader.readLine();
                card_application = myReader.readLine();
                processing_options = myReader.readLine();
                records = myReader.readLine();
                for (int i = 0; i < 1000; i++) {
                    crypto_checksum[i] = myReader.readLine();

                }

                myReader.close();
                card.setMode(false);
                card.setPpse(ppse);
                card.setCard_application(card_application);
                card.setProcessing_options(processing_options);
                card.setRecords(records);
                card.setCrypto_checksum(crypto_checksum);
                card.setFilename(filename);
                card.setState(false);
                card.setMode(false);
            } else if (line.equals(MChip)) {
                card.setState(false);
                card.setMode(true );
                String ppse = "";
                String card_application = "";
                String processing_options = "";
                String record1 = "";
                String record2 = "";
                String record3 = "";
                String record4 = "";
                String generateAC= "";


                ppse = myReader.readLine();
                card_application = myReader.readLine();
                processing_options = myReader.readLine();
                record1 = myReader.readLine();
                record2 = myReader.readLine();
                record3 = myReader.readLine();
                record4 = myReader.readLine();
                generateAC = myReader.readLine();

                card.setPpse(ppse);
                card.setCard_application(card_application);
                card.setProcessing_options(processing_options);
                card.setRecord1(record1);
                card.setRecord2(record2);
                card.setRecord3(record3);
                card.setRecord4(record4);
                card.setGenerateAC(generateAC);
                card.setFilename(filename);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



        return card;

    }

    private void appendToViewLog(String message) {
        String text = message + "\n" + statusLog.getText();

        statusLog.setText(text);
    }


    private void writeActiveCard(String filename) {

        File src = new File(cardsStorage, filename);
        File dst = new File(cardsStorage, "active_card");

        InputStream in = null;
        try {
            in = new FileInputStream(src);

            try {
                OutputStream out = new FileOutputStream(dst);
                try {
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }

                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
