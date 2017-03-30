package com.example;

import com.epson.eposprint.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class PageModeActivity extends Activity implements OnClickListener, StatusChangeEventListener, BatteryStatusChangeEventListener {

    static final int SEND_TIMEOUT = 10 * 1000;
    
    boolean exitActivity = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pagemode);
        
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
    
        //default setting
        TextView text = (TextView)findViewById(R.id.editText_text);
        String value = getString(R.string.pagemode_edit_text);
        text.setText(value);
        
        text = (TextView)findViewById(R.id.editText_area_x);
        text.setText("100");
        
        text = (TextView)findViewById(R.id.editText_area_y);
        text.setText("100");
        
        text = (TextView)findViewById(R.id.editText_area_width);
        text.setText("300");

        text = (TextView)findViewById(R.id.editText_area_height);
        text.setText("300");
        
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
            method = "addPageBegin";
            builder.addPageBegin();
            
            method = "addPageArea";
            builder.addPageArea(
                    getBuilderAreaX(),
                    getBuilderAreaY(),
                    getBuilderAreaWidth(),
                    getBuilderAreaHeight());
            
            method = "addText";
            builder.addText(getBuilderText());

            method = "addPageEnd";
            builder.addPageEnd();
            
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
    
    private int getBuilderAreaX() {
        TextView text = (TextView)findViewById(R.id.editText_area_x);
        try{
            return Integer.parseInt(text.getText().toString());
        }catch(Exception e){
            return 0;
        }
    }
    
    private int getBuilderAreaY() {
        TextView text = (TextView)findViewById(R.id.editText_area_y);
        try{
            return Integer.parseInt(text.getText().toString());
        }catch(Exception e){
            return 0;
        }
    }

    private int getBuilderAreaWidth() {
        TextView text = (TextView)findViewById(R.id.editText_area_width);
        try{
            return Integer.parseInt(text.getText().toString());
        }catch(Exception e){
            return 0;
        }
    }
    
    private int getBuilderAreaHeight() {
        TextView text = (TextView)findViewById(R.id.editText_area_height);
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

	@Override
	public void onStatusChangeEvent(final String deviceName, final int status) {
		runOnUiThread(new Runnable() {
			@Override
			public synchronized void run() {
				ShowMsg.showStatusChangeEvent(deviceName, status, PageModeActivity.this);
			}
		});
	}

	@Override
	public void onBatteryStatusChangeEvent(final String deviceName, final int battery) {
		runOnUiThread(new Runnable() {
			@Override
			public synchronized void run() {
				ShowMsg.showBatteryStatusChangeEvent(deviceName, battery, PageModeActivity.this);
			}
		});
	}
}
