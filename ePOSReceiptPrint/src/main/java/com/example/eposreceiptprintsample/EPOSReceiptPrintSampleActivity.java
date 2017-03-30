package com.example.eposreceiptprintsample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.epson.eposprint.Print;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;

public class EPOSReceiptPrintSampleActivity extends Activity implements OnClickListener {

    private String openDeviceName = "192.168.192.168";
    private int connectionType = Print.DEVTYPE_TCP;
    private int printerModel = Builder.LANG_EN;
    private String printerName = null;

    private EditText editWarnings = null;
    private Spinner spnNames = null;
    private Spinner spnModels = null;

    // Called when the activity is first created. 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // init printer name
        printerName = getString(R.string.printername_m10);

        // Register ClickListener 
        int[] target = {
            R.id.button_discovery,
            R.id.button_print,
        };

        for (int i = 0; i < target.length; i++) {
            Button button = (Button)findViewById(target[i]);
            button.setOnClickListener(this);
        }

        // init printer name list 
        spnNames = (Spinner)findViewById(R.id.spinner_printer);
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nameAdapter.add(getString(R.string.printername_m10));
        nameAdapter.add(getString(R.string.printername_p20));
        nameAdapter.add(getString(R.string.printername_p60));
        nameAdapter.add(getString(R.string.printername_p60ii));
        nameAdapter.add(getString(R.string.printername_p80));
        nameAdapter.add(getString(R.string.printername_t20));
        nameAdapter.add(getString(R.string.printername_t20ii));
        nameAdapter.add(getString(R.string.printername_t70));
        nameAdapter.add(getString(R.string.printername_t70ii));
        nameAdapter.add(getString(R.string.printername_t81ii));
        nameAdapter.add(getString(R.string.printername_t82));
        nameAdapter.add(getString(R.string.printername_t82ii));
        nameAdapter.add(getString(R.string.printername_t83ii));
        nameAdapter.add(getString(R.string.printername_t88v));
        nameAdapter.add(getString(R.string.printername_t90ii));
        nameAdapter.add(getString(R.string.printername_u220));
        nameAdapter.add(getString(R.string.printername_u330));
        spnNames.setAdapter(nameAdapter);

        // init printer model list 
        spnModels = (Spinner)findViewById(R.id.spinner_model);
        ArrayAdapter<SpnModelsItem> modelAdapter = new ArrayAdapter<SpnModelsItem>(this, android.R.layout.simple_spinner_item);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelAdapter.add(new SpnModelsItem(getString(R.string.model_ank), Builder.MODEL_ANK));
        modelAdapter.add(new SpnModelsItem(getString(R.string.model_japanese), Builder.MODEL_JAPANESE));
        modelAdapter.add(new SpnModelsItem(getString(R.string.model_chinese), Builder.MODEL_CHINESE));
        modelAdapter.add(new SpnModelsItem(getString(R.string.model_taiwan), Builder.MODEL_TAIWAN));
        modelAdapter.add(new SpnModelsItem(getString(R.string.model_korean), Builder.MODEL_KOREAN));
        modelAdapter.add(new SpnModelsItem(getString(R.string.model_thai), Builder.MODEL_THAI));
        modelAdapter.add(new SpnModelsItem(getString(R.string.model_southasia), Builder.MODEL_SOUTHASIA));
        spnModels.setAdapter(modelAdapter);

        // init warning window 
        editWarnings = (EditText)findViewById(R.id.edit_warnings);
        editWarnings.setFocusable(false);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
        case R.id.button_discovery:
            intent = new Intent(this, DiscoverPrinterActivity.class);

            intent.putExtra("devtype", connectionType);
            intent.putExtra("ipaddress", openDeviceName);

            startActivityForResult(intent, 0);
            break;

        case R.id.button_print:
            runPrintSequence();
            break;

        default:
            // Do nothing
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (resultCode == RESULT_OK) {
                connectionType = data.getIntExtra("devtype", 0);
                openDeviceName = data.getStringExtra("ipaddress");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("openDeviceName", openDeviceName);
        outState.putInt("connectionType", connectionType);
        outState.putInt("language", printerModel);
        outState.putString("printerName", printerName);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        openDeviceName = savedInstanceState.getString("openDeviceName");
        connectionType = savedInstanceState.getInt("connectionType");
        printerModel = savedInstanceState.getInt("language");
        printerName = savedInstanceState.getString("printerName");
    }

    private void runPrintSequence() {
        Result result = new Result();
        Builder builder = null;

        // Run print sequence of sample receipt 
        editWarnings.setText("");
        builder = createReceiptData(result);

        if (result.getEposException() == null) {
            print(builder, result);
        }

        displayMsg(result);

        // Clear objects 
        if (builder != null) {
            builder.clearCommandBuffer();
        }

        builder = null;
        result = null;

        return;
    }

    private Builder createReceiptData(Result result) {
        Builder builder = null;

        // Top logo data 
        Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.drawable.store);

        // Text buffer 
        StringBuilder textData = new StringBuilder();

        // addBarcode API settings 
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        // Null check
        if (result == null) {
            return null;
        }

        // init result
        result.setPrinterStatus(0);
        result.setBatteryStatus(0);
        result.setEposException(null);
        result.setEpsonIoException(null);

        // Get printer name and model
        this.printerName = (String)this.spnNames.getSelectedItem();
        this.printerModel = ((SpnModelsItem)this.spnModels.getSelectedItem()).getModelConstant();

        try {
            // Create builder 
            builder = new Builder(printerName, printerModel);

            // Set alignment to center 
            builder.addTextAlign(Builder.ALIGN_CENTER);

            // Add top logo to command buffer 
            builder.addImage(logoData, 0, 0,
                             logoData.getWidth(),
                             logoData.getHeight(),
                             Builder.COLOR_1,
                             Builder.MODE_MONO,
                             Builder.HALFTONE_DITHER,
                             Builder.PARAM_DEFAULT,
                             getCompress(connectionType));

            // Add receipt text to command buffer 

            // Section 1 : Store information 
            builder.addFeedLine(1);

            textData.append("THE STORE 1234 (555) 555-5555\n");
            textData.append("STORE DIRECTOR - John Smith\n");
            textData.append("\n");
            textData.append("7/01/07 16:58 6153 05 0191 134\n");
            textData.append("ST# 21 OP# 001 TE# 01 TR# 747\n");
            textData.append("------------------------------\n");

            builder.addText(textData.toString());
            textData.delete(0, textData.length());

            // Section 2 : Purchased items
            textData.append("004 OHEIDA 3PK SPRINGF  9.99 R\n");
            textData.append("104 3 CUP BLK TEAPOT    9.99 R\n");
            textData.append("451 EMERIL GRIDDLE/PAN 17.99 R\n");
            textData.append("389 CANDYMAKER ASSORT   4.99 R\n");
            textData.append("740 TRIPOD              8.99 R\n");
            textData.append("334 BLK LOGO PRNTED ZO  7.99 R\n");
            textData.append("581 AQUA MICROTERRY SC  6.99 R\n");
            textData.append("934 30L BLK FF DRESS   16.99 R\n");
            textData.append("075 LEVITATING DESKTOP  7.99 R\n");
            textData.append("412 **Blue Overprint P  2.99 R\n");
            textData.append("762 REPOSE 4PCPM CHOC   5.49 R\n");
            textData.append("613 WESTGATE BLACK 25  59.99 R\n");
            textData.append("------------------------------\n");

            builder.addText(textData.toString());
            textData.delete(0, textData.length());

            // Section 3 : Payment information
            textData.append("SUBTOTAL                160.38\n");
            textData.append("TAX                      14.43\n");

            builder.addText(textData.toString());
            textData.delete(0, textData.length());

            builder.addTextDouble(Builder.TRUE, Builder.TRUE);
            builder.addText("TOTAL    174.81\n");
            builder.addTextDouble(Builder.FALSE, Builder.FALSE);
            builder.addFeedLine(1);

            textData.append("CASH                    200.00\n");
            textData.append("CHANGE                   25.19\n");
            textData.append("------------------------------\n");

            builder.addText(textData.toString());
            textData.delete(0, textData.length());

            // Section 4 : Advertisement
            textData.append("TotalNumber of Items Purchased\n");
            textData.append("Sign Up and Save !\n");
            textData.append("With Preferred Saving Card\n");

            builder.addText(textData.toString());
            textData.delete(0, textData.length());

            builder.addFeedLine(2);

            // Add barcode data to command buffer 
            builder.addBarcode("01209457",
                               Builder.BARCODE_CODE39,
                               Builder.HRI_BELOW,
                               Builder.FONT_A,
                               barcodeWidth,
                               barcodeHeight);

            // Add command to cut receipt to command buffer 
            builder.addCut(Builder.CUT_FEED);

        }
        catch (EposException e) {
            result.setEposException(e);
        }

        // Discard text buffer 
        textData = null;

        return builder;
    }

    private void print(Builder builder, Result result) {
        int printerStatus[] = new int[1];
        int batteryStatus[] = new int[1];
        boolean isBeginTransaction = false;

        // sendData API timeout setting (10000 msec) 
        final int sendTimeout = 10000;
        Print printer = null;

        // Null check 
        if ((builder == null) || (result == null)) {
            return;
        }

        // init result 
        result.setPrinterStatus(0);
        result.setBatteryStatus(0);
        result.setEposException(null);
        result.setEpsonIoException(null);

        printer = new Print(getApplicationContext());

        try {
            // Open 
            printer.openPrinter(
                connectionType,
                openDeviceName,
                Print.FALSE,
                Print.PARAM_DEFAULT,
                Print.PARAM_DEFAULT);
        }
        catch (EposException e) {
            result.setEposException(e);
            return;
        }

        try {
            // Print data if printer is printable 
            printer.getStatus(printerStatus, batteryStatus);
            result.setPrinterStatus(printerStatus[0]);
            result.setBatteryStatus(batteryStatus[0]);

            if (isPrintable(result)) {
                printerStatus[0] = 0;
                batteryStatus[0] = 0;

                printer.beginTransaction();
                isBeginTransaction = true;

                printer.sendData(builder, sendTimeout, printerStatus, batteryStatus);
                result.setPrinterStatus(printerStatus[0]);
                result.setBatteryStatus(batteryStatus[0]);
            }
        }
        catch (EposException e) {
            result.setEposException(e);
        }
        finally {
            if (isBeginTransaction) {
                try {
                    printer.endTransaction();
                }
                catch (EposException e) {
                    // Do nothing
                }
            }
        }

        try {
            printer.closePrinter();
        }
        catch (EposException e) {
            // Do nothing 
        }

        return;
    }

    // Display error messages and warning messages 
    private void displayMsg(Result result) {
        if (result == null) {
            return;
        }

        String errorMsg = MsgMaker.makeErrorMessage(this, result);
        String warningMsg = MsgMaker.makeWarningMessage(this, result);

        if (!errorMsg.isEmpty()) {
            ShowMsg.show(this, errorMsg);
        }

        if (!warningMsg.isEmpty()) {
            editWarnings.setText(warningMsg);
        }

        return;
    }

    // Determine whether printer is printable 
    private boolean isPrintable(Result result) {
        if (result == null) {
            return false;
        }

        int status = result.getPrinterStatus();
        if ((status & Print.ST_OFF_LINE) == Print.ST_OFF_LINE) {
            return false;
        }

        if ((status & Print.ST_NO_RESPONSE) == Print.ST_NO_RESPONSE) {
            return false;
        }

        return true;
    }

    // Get Compress parameter of addImage API 
    private int getCompress(int connection) {
        if (connection == Print.DEVTYPE_BLUETOOTH) {
            return Builder.COMPRESS_DEFLATE;
        }
        else {
            return Builder.COMPRESS_NONE;
        }
    }

}
