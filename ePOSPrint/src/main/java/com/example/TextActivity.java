package com.example;


import com.epson.eposprint.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class TextActivity extends Activity implements OnClickListener, StatusChangeEventListener, BatteryStatusChangeEventListener {

    static final int SEND_TIMEOUT = 10 * 1000;
    static final int SIZEWIDTH_MAX = 8;
    static final int SIZEHEIGHT_MAX = 8;

    boolean exitActivity = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text);
        
        //check close port
        Print printer = EPOSPrintSampleActivity.getPrinter();
        if(printer == null){
            finish();
            return ;
        }
        else {
            printer.setStatusChangeEventCallback(this);
            printer.setBatteryStatusChangeEventCallback(this);
        }
    
        //init font list
        Spinner spinner = (Spinner)findViewById(R.id.spinner_font);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(getString(R.string.font_a));
        adapter.add(getString(R.string.font_b));
        adapter.add(getString(R.string.font_c));
		adapter.add(getString(R.string.font_d));
		adapter.add(getString(R.string.font_e));
        spinner.setAdapter(adapter);

        //init align list
        spinner = (Spinner)findViewById(R.id.spinner_align);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(getString(R.string.align_left));
        adapter.add(getString(R.string.align_center));
        adapter.add(getString(R.string.align_right));
        spinner.setAdapter(adapter);

        //init language list
        spinner = (Spinner)findViewById(R.id.spinner_language);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(getString(R.string.language_ank));
        adapter.add(getString(R.string.language_japanese));
        adapter.add(getString(R.string.language_simplified_chinese));
        adapter.add(getString(R.string.language_traditional_chinese));
        adapter.add(getString(R.string.language_korean));
        adapter.add(getString(R.string.language_thai));
        adapter.add(getString(R.string.language_vietnamese));
        spinner.setAdapter(adapter);
        
        //init size list
        spinner = (Spinner)findViewById(R.id.spinner_size_width);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(int i = 1; i <= SIZEWIDTH_MAX; i++){
            adapter.add(String.format("%d", i));
        }
        spinner.setAdapter(adapter);
        
        spinner = (Spinner)findViewById(R.id.spinner_size_height);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(int i = 1; i <= SIZEHEIGHT_MAX; i++){
            adapter.add(String.format("%d", i));
        }
        spinner.setAdapter(adapter);
        
        //default setting
        TextView text = (TextView)findViewById(R.id.editText_text);
        String value = getString(R.string.text_edit_text);
        value = value.replaceAll("\\*", " ");
        text.setText(value);
        
        text = (TextView)findViewById(R.id.editText_linespace);
        text.setText("30");
        
        text = (TextView)findViewById(R.id.editText_xposition);
        text.setText("0");
        
        text = (TextView)findViewById(R.id.editText_feedunit);
        text.setText("30");
        
        //Registration ClickListener
        Button button = (Button)findViewById(R.id.button_print);
        button.setOnClickListener(this);

        //hide keyboard
        this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!exitActivity){
            EPOSPrintSampleActivity.closePrinter();
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            setResult(0, null);
            exitActivity = true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void onClick(View v) {
        printText();
    }
    
    private void printText(){
        TextView text = (TextView)findViewById(R.id.editText_text);
        if(text.getText().toString().isEmpty()){
            ShowMsg.showError(R.string.errmsg_notext, this);
            return ;
        }
        
        Builder builder = null;
        String method = "";
        try{
            //create builder
            Intent intent = getIntent();
            method = "Builder";
            builder = new Builder(
                    intent.getStringExtra("printername"), intent.getIntExtra("language", 0), getApplicationContext());

            //add command
            method = "addTextFont";
            builder.addTextFont(getBuilderFont());
            
            method = "addTextAlign";
            builder.addTextAlign(getBuilderAlign());
            
            method = "addTextLineSpace";
            builder.addTextLineSpace(getBuilderLineSpace());
            
            method = "addTextLang";
            builder.addTextLang(getBuilderLanguage());
            
            method = "addTextSize";
            builder.addTextSize(getBuilderSizeW(), getBuilderSizeH());
            
            method = "addTextStyle";
            builder.addTextStyle(Builder.FALSE, getBuilderStyleUnderline(), getBuilderStyleBold(), Builder.COLOR_1);
            
            method = "addTextPosition";
            builder.addTextPosition(getBuilderXPosition());
            
            method = "addText";
            builder.addText(getBuilderText());
            
            method = "addFeedUnit";
            builder.addFeedUnit(getBuilderFeedUnit());

            //send builder data
            int[] status = new int[1];
            int[] battery = new int[1];
            try{
                Print printer = EPOSPrintSampleActivity.getPrinter();
                printer.sendData(builder, SEND_TIMEOUT, status, battery);
                ShowMsg.showStatus(EposException.SUCCESS, status[0], battery[0], this);
            }catch(EposException e){
                ShowMsg.showStatus(e.getErrorStatus(), e.getPrinterStatus(), e.getBatteryStatus(), this);
            }
        }catch(Exception e){
            ShowMsg.showException(e, method, this);
        }
        
        //remove builder
        if(builder != null){
            try{
                builder.clearCommandBuffer();
                builder = null;
            }catch(Exception e){
                builder = null;
            }
        }
    }
    
    private int getBuilderFont() {
        Spinner spinner = (Spinner)findViewById(R.id.spinner_font);
        switch(spinner.getSelectedItemPosition()){
        case 1:
            return Builder.FONT_B;
        case 2:
            return Builder.FONT_C;
		case 3:
            return Builder.FONT_D;
		case 4:
            return Builder.FONT_E;
        case 0:
        default:
            return Builder.FONT_A;
        }
    }
    
    private int getBuilderAlign() {
        Spinner spinner = (Spinner)findViewById(R.id.spinner_align);
        switch(spinner.getSelectedItemPosition()){
        case 1:
            return Builder.ALIGN_CENTER;
        case 2:
            return Builder.ALIGN_RIGHT;
        case 0:
        default:
            return Builder.ALIGN_LEFT;
        }
    }
    
    private int getBuilderLineSpace() {
        TextView text = (TextView)findViewById(R.id.editText_linespace);
        try{
            return Integer.parseInt(text.getText().toString());
        }catch(Exception e){
            return 0;
        }
    }

    private int getBuilderLanguage() {
        Spinner spinner = (Spinner)findViewById(R.id.spinner_language);
        switch(spinner.getSelectedItemPosition()){
        case 1:
            return Builder.LANG_JA;
        case 2:
            return Builder.LANG_ZH_CN;
        case 3:
            return Builder.LANG_ZH_TW;
        case 4:
            return Builder.LANG_KO;
        case 5:
            return Builder.LANG_TH;
        case 6:
            return Builder.LANG_VI;
        case 0:
        default:
            return Builder.LANG_EN;
        }
    }
    
    private int getBuilderSizeW() {
        Spinner spinner = (Spinner)findViewById(R.id.spinner_size_width);
        return spinner.getSelectedItemPosition() + 1;
    }
        
    private int getBuilderSizeH() {
        Spinner spinner = (Spinner)findViewById(R.id.spinner_size_height);
        return spinner.getSelectedItemPosition() + 1;
    }

    private int getBuilderStyleBold() {
        ToggleButton toggle = (ToggleButton)findViewById(R.id.toggleButton_style_bold);
        if(toggle.isChecked()){
            return Builder.TRUE;
        }else{
            return Builder.FALSE;
        }
    }

    private int getBuilderStyleUnderline() {
        ToggleButton toggle = (ToggleButton)findViewById(R.id.toggleButton_style_underline);
        if(toggle.isChecked()){
            return Builder.TRUE;
        }else{
            return Builder.FALSE;
        }
    }
    
    private int getBuilderXPosition() {
        TextView text = (TextView)findViewById(R.id.editText_xposition);
        try{
            return Integer.parseInt(text.getText().toString());
        }catch(Exception e){
            return 0;
        }
    }
    
    private String getBuilderText() {
        TextView text = (TextView)findViewById(R.id.editText_text);
        return text.getText().toString();
    }

    private int getBuilderFeedUnit() {
        TextView text = (TextView)findViewById(R.id.editText_feedunit);
        try{
            return Integer.parseInt(text.getText().toString());
        }catch(Exception e){
            return 0;
        }
    }
    
	@Override
	public void onStatusChangeEvent(final String deviceName, final int status) {
		runOnUiThread(new Runnable() {
			@Override
			public synchronized void run() {
				ShowMsg.showStatusChangeEvent(deviceName, status, TextActivity.this);
			}
		});
	}

	@Override
	public void onBatteryStatusChangeEvent(final String deviceName, final int battery) {
		runOnUiThread(new Runnable() {
			@Override
			public synchronized void run() {
				ShowMsg.showBatteryStatusChangeEvent(deviceName, battery, TextActivity.this);
			}
		});
	}
}
