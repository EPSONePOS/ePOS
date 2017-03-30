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

public class BarcodeActivity extends Activity implements OnClickListener, OnItemSelectedListener, StatusChangeEventListener, BatteryStatusChangeEventListener {
    
    static final int SEND_TIMEOUT = 10 * 1000;
    static final int HRI_NONE_INDEX =  0;
    static final int HRI_ABOVE_INDEX = 1;
    static final int HRI_BELOW_INDEX = 2;
    static final int HRI_BOTH_INDEX = 3;
    static final int FONT_A_INDEX = 0;
    static final int FONT_B_INDEX = 1;
    static final int FONT_C_INDEX = 2;
	static final int FONT_D_INDEX = 3;
	static final int FONT_E_INDEX = 4;
    
    boolean changeBarcodeData = false;
    String defaultBarcodeData = "";
    boolean exitActivity = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode);
    
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
                R.string.bartype_upca,
                R.string.bartype_upce,
                R.string.bartype_ean13,
                R.string.bartype_jan13,
                R.string.bartype_ean8,
                R.string.bartype_jan8,
                R.string.bartype_code39,
                R.string.bartype_itf,
                R.string.bartype_codabar,
                R.string.bartype_code93,
                R.string.bartype_code128,
                R.string.bartype_gs1128,
                R.string.bartype_gs1om,
                R.string.bartype_gs1tr,
                R.string.bartype_gs1li,
                R.string.bartype_gs1ex,
        };
        for(int i = 0; i < typelist.length; i++){
            adapter.add(getString(typelist[i]));
        }
        spinner.setAdapter(adapter);
        
        //init hri list
        spinner = (Spinner)findViewById(R.id.spinner_hri);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(getString(R.string.hri_none));
        adapter.add(getString(R.string.hri_above));
        adapter.add(getString(R.string.hri_below));
        adapter.add(getString(R.string.hri_both));
        spinner.setAdapter(adapter);

        //init font list
        spinner = (Spinner)findViewById(R.id.spinner_font);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(getString(R.string.font_a));
        adapter.add(getString(R.string.font_b));
        adapter.add(getString(R.string.font_c));
		adapter.add(getString(R.string.font_d));
		adapter.add(getString(R.string.font_e));
        spinner.setAdapter(adapter);

        //default setting
        TextView text = (TextView)findViewById(R.id.editText_modulesize_w);
        text.setText("3");

        text = (TextView)findViewById(R.id.editText_modulesize_h);
        text.setText("162");

        spinner = (Spinner)findViewById(R.id.spinner_hri);
        spinner.setSelection(2);
        
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
            method = "addBarcode";
            builder.addBarcode(
                    getBuilderData(),
                    getBuilderType(),
                    getBuilderHri(),
                    getBuilderFont(),
                    getBuilderWidth(),
                    getBuilderHeight());

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
                Builder.BARCODE_UPC_A,
                Builder.BARCODE_UPC_E,
                Builder.BARCODE_EAN13,
                Builder.BARCODE_JAN13,
                Builder.BARCODE_EAN8,
                Builder.BARCODE_JAN8,
                Builder.BARCODE_CODE39,
                Builder.BARCODE_ITF,
                Builder.BARCODE_CODABAR,
                Builder.BARCODE_CODE93,
                Builder.BARCODE_CODE128,
                Builder.BARCODE_GS1_128,
                Builder.BARCODE_GS1_DATABAR_OMNIDIRECTIONAL,
                Builder.BARCODE_GS1_DATABAR_TRUNCATED,
                Builder.BARCODE_GS1_DATABAR_LIMITED,
                Builder.BARCODE_GS1_DATABAR_EXPANDED,
        };
        if(spinner.getSelectedItemPosition() < typelist.length){
            return typelist[spinner.getSelectedItemPosition()];
        }else{
            return typelist[0];
        }
    }

    private int getBuilderHri(){
        Spinner spinner = (Spinner)findViewById(R.id.spinner_hri);
        switch(spinner.getSelectedItemPosition()){
        case HRI_ABOVE_INDEX:
            return Builder.HRI_ABOVE;
        case HRI_BELOW_INDEX:
            return Builder.HRI_BELOW;
        case HRI_BOTH_INDEX:
            return Builder.HRI_BOTH;
        case HRI_NONE_INDEX:
        default:
            return Builder.HRI_NONE;
        }
    }
    
    private int getBuilderFont(){
        Spinner spinner = (Spinner)findViewById(R.id.spinner_font);
        switch(spinner.getSelectedItemPosition()){
        case FONT_B_INDEX:
            return Builder.FONT_B;
        case FONT_C_INDEX:
            return Builder.FONT_C;
		case FONT_D_INDEX:
            return Builder.FONT_D;
		case FONT_E_INDEX:
            return Builder.FONT_E;
        case FONT_A_INDEX:
        default:
            return Builder.FONT_A;
        }
    }
    
    private int getBuilderWidth(){
        TextView text = (TextView)findViewById(R.id.editText_modulesize_w);
        try{
            return Integer.parseInt(text.getText().toString());
        }catch(Exception e){
            return 0;
        }
    }

    private int getBuilderHeight(){
        TextView text = (TextView)findViewById(R.id.editText_modulesize_h);
        try{
            return Integer.parseInt(text.getText().toString());
        }catch(Exception e){
            return 0;
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
                R.string.bardata_upca,
                R.string.bardata_upce,
                R.string.bardata_ean13,
                R.string.bardata_jan13,
                R.string.bardata_ean8,
                R.string.bardata_jan8,
                R.string.bardata_code39,
                R.string.bardata_itf,
                R.string.bardata_codabar,
                R.string.bardata_code93,
                R.string.bardata_code128,
                R.string.bardata_gs1128,
                R.string.bardata_gs1om,
                R.string.bardata_gs1tr,
                R.string.bardata_gs1li,
                R.string.bardata_gs1ex,
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
				ShowMsg.showStatusChangeEvent(deviceName, status, BarcodeActivity.this);
			}
		});
	}

	@Override
	public void onBatteryStatusChangeEvent(final String deviceName, final int battery) {
		runOnUiThread(new Runnable() {
			@Override
			public synchronized void run() {
				ShowMsg.showBatteryStatusChangeEvent(deviceName, battery, BarcodeActivity.this);
			}
		});
	}
}
