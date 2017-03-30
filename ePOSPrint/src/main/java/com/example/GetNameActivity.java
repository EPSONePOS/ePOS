package com.example;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.epson.epsonio.*;
import com.epson.eposprint.*;
import com.example.R.string;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

public class GetNameActivity extends Activity implements OnClickListener {
    static final byte[] CMD_ESC_ENABLE_PRINTER = {
        0x1b, 0x3d, 0x01,    // ESC = 1(Enables printer)
    };

    static final byte[] CMD_GS_I_PRINTER_NAME = {
        0x1d, 0x49, 0x43,    // GS I 67(Printer name)
    };

    static final byte[] CMD_GS_I_ADDITIONAL_FONTS = {
        0x1d, 0x49, 0x45,    // GS I 69(Type of mounted additional fonts)
    };

    static final int RESPONSE_HEADER = 0x5f;
    static final int RESPONSE_TERMINAL = 0x00;
    static final int SEND_RESPONSE_TIMEOUT = 1000;
    static final int RESPONSE_MAXBYTE = 128;
    
    String fontName = null;
    String printerName = null;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.getname);
        
        //select default radiobutton
        RadioGroup radio = (RadioGroup)findViewById(R.id.radiogroup_devtype);
        Intent intent = getIntent();
        switch(intent.getIntExtra("devtype", 0)){
        case Print.DEVTYPE_TCP:
            radio.check(R.id.radioButton_tcp);
            break;
        case Print.DEVTYPE_BLUETOOTH:
            radio.check(R.id.radioButton_bluetooth);
            break;
        case Print.DEVTYPE_USB:
            radio.check(R.id.radioButton_usb);
            break;
        default:
            radio.check(R.id.radioButton_tcp);
            break;
        }
        
        //init edit
        TextView textIp = (TextView)findViewById(R.id.editText_ip);
        textIp.setText(intent.getStringExtra("ipaddress"));

        //Registration ClickListener
        Button button = (Button)findViewById(R.id.button_get);
        button.setOnClickListener(this);

        //hide keyboard
        this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    
    @Override
    public void onClick(View v) {
        getPrinterName();
    }
    
    private void getPrinterName() {
        //get open parameter
        TextView textIp = (TextView)findViewById(R.id.editText_ip);
        if(textIp.getText().toString().isEmpty()){
            ShowMsg.showError(R.string.errmsg_noaddress, this);
            return ;
        }
        
        int deviceType = DevType.TCP;
        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radiogroup_devtype);
        switch(radioGroup.getCheckedRadioButtonId()){
        case R.id.radioButton_tcp:
            deviceType = DevType.TCP;
            break;
        case R.id.radioButton_bluetooth:
            deviceType = DevType.BLUETOOTH;
            break;
        case R.id.radioButton_usb:
            deviceType = DevType.USB;
            break;
        default:
            deviceType = DevType.TCP;
            break;
        }

        fontName = null;
        printerName = null;
        EpsonIo port = null;
        String method = "";
        try{
        	byte[]		receiveBuffer	= new byte[RESPONSE_MAXBYTE];
        	int[]		receiveSize		= new int[1];
        	String[]	value			= new String[1];
        	Boolean		ret				= false;

            //open
            port = new EpsonIo();
            method = "open";
            port.open(deviceType, textIp.getText().toString(), null, this);

            clearReceiveBuffer( port );

            //send command(esc/pos)
            // enable printer
            method = "write";
            port.write( CMD_ESC_ENABLE_PRINTER, 0, CMD_ESC_ENABLE_PRINTER.length, SEND_RESPONSE_TIMEOUT );

            {	// printe name
            	method = "write";
            	port.write( CMD_GS_I_PRINTER_NAME, 0, CMD_GS_I_PRINTER_NAME.length, SEND_RESPONSE_TIMEOUT );

            	method = "read";
            	Arrays.fill( receiveBuffer, (byte)0 );

            	ret = receiveResponse( port, receiveBuffer, receiveSize );
            	if ((false != ret) && (0 < receiveSize[0])) {
                	byte[] response = Arrays.copyOf( receiveBuffer, receiveSize[0] );

                	analyzeResponse( response, value );
    
                	printerName = value[0];
            	}
            }
           
            {	// additional fonts
            	method = "write";
            	port.write( CMD_GS_I_ADDITIONAL_FONTS, 0, CMD_GS_I_ADDITIONAL_FONTS.length, SEND_RESPONSE_TIMEOUT );

            	method = "read";
            	Arrays.fill( receiveBuffer, (byte)0 );

            	ret = receiveResponse( port, receiveBuffer, receiveSize );
            	if ((false != ret) && (0 < receiveSize[0])) {
                	byte[] response = Arrays.copyOf( receiveBuffer, receiveSize[0] );

                	analyzeResponse( response, value );
               
                	fontName = value[0];
            	}
            }

            //close
            method = "close";
            port.close();
        }catch(Exception e){
            ShowMsg.showException(e, method, this);
            try{
                if(port != null){
                    port.close();
                    port = null;
                }
            }catch(Exception e1){
                port = null;
            }
            return ;
        }
        
        if(fontName == null || printerName == null){
            ShowMsg.showError(string.errmsg_getname, this);
        }else{
            ShowMsg.showPrinterName(printerName, getLanguageSpec(fontName), this);
        }
    }

    //receive response
    private void clearReceiveBuffer( EpsonIo port ) throws EpsonIoException{
    	while (true) {
            try {
            	byte[] receiveBuffer = new byte[RESPONSE_MAXBYTE];
            	int readSize = 0;
                readSize = port.read( receiveBuffer, 0, receiveBuffer.length, 100 );
                if (0 == readSize) {
                	break;
                }
            } catch (EpsonIoException e) {
                if(e.getStatus() == IoStatus.ERR_TIMEOUT){
                	return ;
                }else{
                    throw e;
                }
            }
    	}
    }
    
    //receive response
    private Boolean receiveResponse(EpsonIo port, byte[] receiveBuffer, int[] readSize ) throws EpsonIoException{
        if ((null == receiveBuffer) || (0 >= receiveBuffer.length)) {
        	return false;
        }

        if ((null == readSize) || (0 >= readSize.length)) {
        	return false;
        }

        readSize[0] = 0;
        
        //receive
        try {
            readSize[0] = port.read( receiveBuffer, 0, receiveBuffer.length, SEND_RESPONSE_TIMEOUT );
        } catch (EpsonIoException e) {
            if(e.getStatus() == IoStatus.ERR_TIMEOUT){
            	return false;
            }else{
                throw e;
            }
        }

        return true;
    }
    
    private boolean analyzeResponse(byte[] response, String[] value ){
        int currentPos = 0;

        if ((null == value) || (0 >= value.length)) {
        	return false;
        }
        value[0] = "";

        //search 5f header
        for (currentPos = 0; currentPos < response.length; currentPos++) {
            if (response[currentPos] == RESPONSE_HEADER) {
            	currentPos++;
                break;
            }
        }

        if(currentPos >= response.length){
            return false;
        }

        // terminater check
        int endPos = 0;
        for (endPos = currentPos; endPos < response.length; endPos++) {
            if (response[endPos] == RESPONSE_TERMINAL) {
            	break;
            }
        }

        if (endPos == currentPos) {
        	return true;
        }

        //get response string
        String responseString = null;
        try {
            responseString = new String( response, currentPos, endPos - currentPos, "US-ASCII" );
        } catch (UnsupportedEncodingException e) {
            return false;
        }
        
        value[0] = responseString.trim();

        return true;
    }
    
    private String getLanguageSpec(String font) {
        if(font.compareTo("") == 0){
            return getString(R.string.language_ank);
        }else if(font.compareTo("KANJI JAPANESE") == 0){
            return getString(R.string.language_japanese);
        }else{
            return getString(R.string.language_other);
        }
    }
}
