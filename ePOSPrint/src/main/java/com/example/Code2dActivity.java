package com.example;

import com.epson.eposprint.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class Code2dActivity extends Activity implements OnClickListener, OnItemSelectedListener, StatusChangeEventListener, BatteryStatusChangeEventListener {

    static final int SEND_TIMEOUT = 10 * 1000;
    
    static final int SYMBOL_PDF417_STANDARD_INDEX = 0;
    static final int SYMBOL_PDF417_TRUNCATED_INDEX = 1;
    static final int SYMBOL_QRCODE_MODEL_1_INDEX = 2;
    static final int SYMBOL_QRCODE_MODEL_2_INDEX = 3;
    static final int SYMBOL_MAXICODE_MODE_2 = 4;
    static final int SYMBOL_MAXICODE_MODE_3 = 5;
    static final int SYMBOL_MAXICODE_MODE_4 = 6;
    static final int SYMBOL_MAXICODE_MODE_5 = 7;
    static final int SYMBOL_MAXICODE_MODE_6 = 8;
    static final int SYMBOL_GS1_DATABAR_STACKED = 9;
    static final int SYMBOL_GS1_DATABAR_STACKED_OMNIDIRECTIONAL = 10;
    static final int SYMBOL_GS1_DATABAR_EXPANDED_STACKED = 11;
    static final int SYMBOL_AZTECCODE_FULLRANGE = 12;
    static final int SYMBOL_AZTECCODE_COMPACT = 13;
    static final int SYMBOL_DATAMATRIX_SQUARE = 14;
    static final int SYMBOL_DATAMATRIX_RECTANGLE_8 = 15;
    static final int SYMBOL_DATAMATRIX_RECTANGLE_12 = 16;
    static final int SYMBOL_DATAMATRIX_RECTANGLE_16 = 17;

    boolean changeBarcodeData = false;
    String defaultBarcodeData = "";
    boolean exitActivity = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.code2d);
    
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
        
        //init type list
        Spinner spinner = (Spinner)findViewById(R.id.spinner_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        int[] typelist = {
                R.string.code2type_pdf417s,
                R.string.code2type_pdf417t,
                R.string.code2type_qr1,
                R.string.code2type_qr2,
                R.string.code2type_maxcode2,
                R.string.code2type_maxcode3,
                R.string.code2type_maxcode4,
                R.string.code2type_maxcode5,
                R.string.code2type_maxcode6,
                R.string.code2type_gr1st,
                R.string.code2type_gr1so,
                R.string.code2type_gr1es,
                R.string.code2type_aztecfull,
                R.string.code2type_azteccompact,
                R.string.code2type_dmsquare,
                R.string.code2type_dmrect_8rows,
                R.string.code2type_dmrect_12rows,
                R.string.code2type_dmrect_16rows,
        };
        for(int i = 0; i < typelist.length; i++){
            adapter.add(getString(typelist[i]));
        }
        spinner.setAdapter(adapter);
        
        //init level list
        setLevelList();

        //default setting
        setDefaultSetting();
        setDefaultBarcodeData();
        
        //Registration ClickListener
        Button button = (Button)findViewById(R.id.button_print);
        button.setOnClickListener(this);
        
        //register OnCheckedChangeListener
        spinner = (Spinner)findViewById(R.id.spinner_type);
        spinner.setOnItemSelectedListener(this);

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
        printBarcode();
    }
    
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        //reset level list
        setLevelList();

        //set default settings
        setDefaultSetting();

        //check default change
        TextView text = (TextView)findViewById(R.id.editText_data);
        if(defaultBarcodeData.compareTo(text.getText().toString()) != 0){
            changeBarcodeData = true;
        }
        if(changeBarcodeData){
            //not update
            return;
        }
        
        //update barcode data
        setDefaultBarcodeData();
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        return ;
    }

    private void printBarcode(){
        TextView text = (TextView)findViewById(R.id.editText_data);
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
            int level = getBuilderLevelRange();
            if (level == Builder.PARAM_UNSPECIFIED){
            	level = getBuilderLevel();
            }
            
            method = "addSymbol";
            builder.addSymbol(
                    getBuilderData(),
                    getBuilderType(),
                    level,
                    getBuilderWidth(),
                    getBuilderHeight(),
                    getBuilderMaxSize());

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
    
    private String getBuilderData(){
        TextView text = (TextView)findViewById(R.id.editText_data);
        return text.getText().toString();
    }
    
    private int getBuilderType(){
        Spinner spinner = (Spinner)findViewById(R.id.spinner_type);
        int[] typelist = {
                Builder.SYMBOL_PDF417_STANDARD,
                Builder.SYMBOL_PDF417_TRUNCATED,
                Builder.SYMBOL_QRCODE_MODEL_1,
                Builder.SYMBOL_QRCODE_MODEL_2,
                Builder.SYMBOL_MAXICODE_MODE_2,
                Builder.SYMBOL_MAXICODE_MODE_3,
                Builder.SYMBOL_MAXICODE_MODE_4,
                Builder.SYMBOL_MAXICODE_MODE_5,
                Builder.SYMBOL_MAXICODE_MODE_6,
                Builder.SYMBOL_GS1_DATABAR_STACKED,
                Builder.SYMBOL_GS1_DATABAR_STACKED_OMNIDIRECTIONAL,
                Builder.SYMBOL_GS1_DATABAR_EXPANDED_STACKED,
                Builder.SYMBOL_AZTECCODE_FULLRANGE,
                Builder.SYMBOL_AZTECCODE_COMPACT,
                Builder.SYMBOL_DATAMATRIX_SQUARE,
                Builder.SYMBOL_DATAMATRIX_RECTANGLE_8,
                Builder.SYMBOL_DATAMATRIX_RECTANGLE_12,
                Builder.SYMBOL_DATAMATRIX_RECTANGLE_16,
        };
        if(spinner.getSelectedItemPosition() < typelist.length){
            return typelist[spinner.getSelectedItemPosition()];
        }else{
            return typelist[0];
        }
    }

    private int getBuilderLevel(){
        Spinner spinner = (Spinner)findViewById(R.id.spinner_type);
        int[] levellist = null;
        int[] pdf417levellist = {
                Builder.LEVEL_0,
                Builder.LEVEL_1,
                Builder.LEVEL_2,
                Builder.LEVEL_3,
                Builder.LEVEL_4,
                Builder.LEVEL_5,
                Builder.LEVEL_6,
                Builder.LEVEL_7,
                Builder.LEVEL_8,
        };
        int[] qrlevellist = {
                Builder.LEVEL_L,
                Builder.LEVEL_M,
                Builder.LEVEL_Q,
                Builder.LEVEL_H,
        };
        switch(spinner.getSelectedItemPosition()){
        case SYMBOL_PDF417_STANDARD_INDEX:
        case SYMBOL_PDF417_TRUNCATED_INDEX:
            levellist = pdf417levellist;
            break;
        case SYMBOL_QRCODE_MODEL_1_INDEX:
        case SYMBOL_QRCODE_MODEL_2_INDEX:
            levellist = qrlevellist;
            break;
        default:
            break;
        }
        if(levellist == null){
            return Builder.LEVEL_DEFAULT;
        }else{
           spinner = (Spinner)findViewById(R.id.spinner_level);
           if(spinner.getSelectedItemPosition() < levellist.length){
               return levellist[spinner.getSelectedItemPosition()];
           }else{
               return levellist[0];
           }
        }
    }

    private int getBuilderWidth(){
        TextView text = (TextView)findViewById(R.id.editText_modulesize_w);
        if(text.isEnabled()){
            try{
                return Integer.parseInt(text.getText().toString());
            }catch(Exception e){
                return 0;
            }
        }else{
            return Builder.PARAM_UNSPECIFIED;
        }
    }

    private int getBuilderHeight(){
        TextView text = (TextView)findViewById(R.id.editText_modulesize_h);
        if(text.isEnabled()){
            try{
                return Integer.parseInt(text.getText().toString());
            }catch(Exception e){
                return 0;
            }
        }else{
            return Builder.PARAM_UNSPECIFIED;
        }
    }
    
    private int getBuilderMaxSize(){
        TextView text = (TextView)findViewById(R.id.editText_maxsize);
        if(text.isEnabled()){
            try{
                return Integer.parseInt(text.getText().toString());
            }catch(Exception e){
                return 0;
            }
        }else{
            return Builder.PARAM_UNSPECIFIED;
        }
    }
    
    private int getBuilderLevelRange(){
        TextView text = (TextView)findViewById(R.id.editText_level);
        if(text.isEnabled()){
            try{
                return Integer.parseInt(text.getText().toString());
            }catch(Exception e){
                return 0;
            }
        }else{
            return Builder.PARAM_UNSPECIFIED;
        }
    }
    
    private void setLevelList(){
        Spinner spinner = (Spinner)findViewById(R.id.spinner_type);
        int[] levellist = null;
        int[] pdf417levellist = {
                R.string.level_pdf417_0,
                R.string.level_pdf417_1,
                R.string.level_pdf417_2,
                R.string.level_pdf417_3,
                R.string.level_pdf417_4,
                R.string.level_pdf417_5,
                R.string.level_pdf417_6,
                R.string.level_pdf417_7,
                R.string.level_pdf417_8,
        };
        int[] qrlevellist = {
                R.string.level_qr_l,
                R.string.level_qr_m,
                R.string.level_qr_q,
                R.string.level_qr_h,
        };
        switch(spinner.getSelectedItemPosition()){
        case SYMBOL_PDF417_STANDARD_INDEX:
        case SYMBOL_PDF417_TRUNCATED_INDEX:
            levellist = pdf417levellist;
            break;
        case SYMBOL_QRCODE_MODEL_1_INDEX:
        case SYMBOL_QRCODE_MODEL_2_INDEX:
            levellist = qrlevellist;
            break;
        default:
             break;
        }
        
        spinner = (Spinner)findViewById(R.id.spinner_level);
        if(levellist == null){
            //disable
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            adapter.add(getString(R.string.empty));
            spinner.setEnabled(false);
        }else{
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for(int i = 0; i < levellist.length; i++){
                adapter.add(getString(levellist[i]));
            }
            spinner.setAdapter(adapter);
            spinner.setEnabled(true);
        }
    }
    
    private void setDefaultSetting(){
        Spinner spinner = (Spinner)findViewById(R.id.spinner_type);

        TextView textModuleW = (TextView)findViewById(R.id.editText_modulesize_w);
        TextView textModuleH = (TextView)findViewById(R.id.editText_modulesize_h);
        TextView textModuleMaxsize = (TextView)findViewById(R.id.editText_maxsize);
        TextView textLevel = (TextView)findViewById(R.id.editText_level);
        switch(spinner.getSelectedItemPosition()){
        case SYMBOL_PDF417_STANDARD_INDEX:
        case SYMBOL_PDF417_TRUNCATED_INDEX:
            textModuleW.setText("3");
            textModuleH.setText("3");
            textModuleMaxsize.setText("0");
            textLevel.setText("");
            textModuleW.setEnabled(true);
            textModuleH.setEnabled(true);
            textModuleMaxsize.setEnabled(true);
            textLevel.setEnabled(false);
            break;
        case SYMBOL_QRCODE_MODEL_1_INDEX:
        case SYMBOL_QRCODE_MODEL_2_INDEX:
            textModuleW.setText("3");
            textModuleH.setText("");
            textModuleMaxsize.setText("");
            textLevel.setText("");
            textModuleW.setEnabled(true);
            textModuleH.setEnabled(false);
            textModuleMaxsize.setEnabled(false);
            textLevel.setEnabled(false);
            break;
        case SYMBOL_MAXICODE_MODE_2:
        case SYMBOL_MAXICODE_MODE_3:
        case SYMBOL_MAXICODE_MODE_4:
        case SYMBOL_MAXICODE_MODE_5:
        case SYMBOL_MAXICODE_MODE_6:
            textModuleW.setText("");
            textModuleH.setText("");
            textModuleMaxsize.setText("");
            textLevel.setText("");
            textModuleW.setEnabled(false);
            textModuleH.setEnabled(false);
            textModuleMaxsize.setEnabled(false);
            textLevel.setEnabled(false);
            break;
        case SYMBOL_GS1_DATABAR_STACKED:
        case SYMBOL_GS1_DATABAR_STACKED_OMNIDIRECTIONAL:
        case SYMBOL_GS1_DATABAR_EXPANDED_STACKED:
            textModuleW.setText("2");
            textModuleH.setText("");
            textModuleMaxsize.setText("114");
            textLevel.setText("");
            textModuleW.setEnabled(true);
            textModuleH.setEnabled(false);
            textModuleMaxsize.setEnabled(true);
            textLevel.setEnabled(false);
            break;
        case SYMBOL_AZTECCODE_FULLRANGE:
        case SYMBOL_AZTECCODE_COMPACT:
            textModuleW.setText("3");
            textModuleH.setText("");
            textModuleMaxsize.setText("");
            textLevel.setText("23");
            textModuleW.setEnabled(true);
            textModuleH.setEnabled(false);
            textModuleMaxsize.setEnabled(false);
            textLevel.setEnabled(true);
            break;
        case SYMBOL_DATAMATRIX_SQUARE:
        case SYMBOL_DATAMATRIX_RECTANGLE_8:
        case SYMBOL_DATAMATRIX_RECTANGLE_12:
        case SYMBOL_DATAMATRIX_RECTANGLE_16:
            textModuleW.setText("3");
            textModuleH.setText("");
            textModuleMaxsize.setText("");
            textLevel.setText("");
            textModuleW.setEnabled(true);
            textModuleH.setEnabled(false);
            textModuleMaxsize.setEnabled(false);
            textLevel.setEnabled(false);
            break;
        default:
            break;
        }
    }
    
    private void setDefaultBarcodeData(){
        //update default data
        defaultBarcodeData = getDefaultBarcodeData();

        //update ui
        TextView text = (TextView)findViewById(R.id.editText_data);
        text.setText(defaultBarcodeData);
    }
    
    private String getDefaultBarcodeData(){
        Spinner spinner = (Spinner)findViewById(R.id.spinner_type);
        int defaultDataNo = 0;
        int[] dataList = {
                R.string.code2data_pdf417s,
                R.string.code2data_pdf417t,
                R.string.code2data_qr1,
                R.string.code2data_qr2,
                R.string.code2data_maxcode2,
                R.string.code2data_maxcode3,
                R.string.code2data_maxcode4,
                R.string.code2data_maxcode5,
                R.string.code2data_maxcode6,
                R.string.code2data_gr1st,
                R.string.code2data_gr1so,
                R.string.code2data_gr1es,
                R.string.code2data_aztecfull,
                R.string.code2data_azteccompact,
                R.string.code2data_dmsquare,
                R.string.code2data_dmrect_8rows,
                R.string.code2data_dmrect_12rows,
                R.string.code2data_dmrect_16rows,
        };
        if(spinner.getSelectedItemPosition() < dataList.length){
            defaultDataNo = dataList[spinner.getSelectedItemPosition()];
        }else{
            defaultDataNo = dataList[0];
        }
        
        return getString(defaultDataNo);
    }
    
	@Override
	public void onStatusChangeEvent(final String deviceName, final int status) {
		runOnUiThread(new Runnable() {
			@Override
			public synchronized void run() {
				ShowMsg.showStatusChangeEvent(deviceName, status, Code2dActivity.this);
			}
		});
	}

	@Override
	public void onBatteryStatusChangeEvent(final String deviceName, final int battery) {
		runOnUiThread(new Runnable() {
			@Override
			public synchronized void run() {
				ShowMsg.showBatteryStatusChangeEvent(deviceName, battery, Code2dActivity.this);
			}
		});
	}
}
