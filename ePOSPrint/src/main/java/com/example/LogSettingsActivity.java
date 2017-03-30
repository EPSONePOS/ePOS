package com.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Log;
import com.epson.eposprint.StatusChangeEventListener;

public class LogSettingsActivity extends Activity implements OnClickListener, StatusChangeEventListener, BatteryStatusChangeEventListener {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logsettings);
		
        //init enabled list
        Spinner spinner = (Spinner)findViewById(R.id.spinner_enabled);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(getString(R.string.log_disable));
        adapter.add(getString(R.string.log_storage));
        adapter.add(getString(R.string.log_tcp));
        spinner.setAdapter(adapter);

        //init loglevel list
        spinner = (Spinner)findViewById(R.id.spinner_loglevel);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(getString(R.string.log_low));
        spinner.setAdapter(adapter);

        //default setting
        TextView text = (TextView)findViewById(R.id.editText_logsettings_ip);
        text.setText("192.168.192.168");
        
        text = (TextView)findViewById(R.id.editText_logsettings_port);
        text.setText("49152");
        
        text = (TextView)findViewById(R.id.editText_logsettings_logsize);
        text.setText("1");

        //Registration ClickListener
        Button button = (Button)findViewById(R.id.button_setting);
        button.setOnClickListener(this);

        //hide keyboard
        this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
	}

	@Override
    public void onClick(View v) {
    	try {
			Log.setLogSettings(getApplicationContext(), getPeriod(), getEnabled(), getIpAddress(), getPort(), getLogSize(), getLogLevel());
		} catch (EposException e) {
			ShowMsg.showException(e,"setLogSettings",this);
		}
    }
	
    private int getEnabled() {
        Spinner spinner = (Spinner)findViewById(R.id.spinner_enabled);
        switch(spinner.getSelectedItemPosition()){
        case 1:
            return Log.LOG_STORAGE;
        case 2:
            return Log.LOG_TCP;
        case 0:
        default:
            return Log.LOG_DISABLE;
        }
    }

    private String getIpAddress() {
        TextView text = (TextView)findViewById(R.id.editText_logsettings_ip);
        return text.getText().toString();
    }

    private int getPort() {
        TextView text = (TextView)findViewById(R.id.editText_logsettings_port);
        try{
            return Integer.parseInt(text.getText().toString());
        }catch(Exception e){
            return 0;
        }
    }
    
    private int getLogSize() {
        TextView text = (TextView)findViewById(R.id.editText_logsettings_logsize);
        try{
            return Integer.parseInt(text.getText().toString());
        }catch(Exception e){
            return 0;
        }
    }
    
    private int getLogLevel() {
        Spinner spinner = (Spinner)findViewById(R.id.spinner_loglevel);
        switch(spinner.getSelectedItemPosition()){
        case 0:
        default:
            return Log.LOG_LOW;
        }
    }
    
    private int getPeriod() {
        ToggleButton toggle = (ToggleButton)findViewById(R.id.toggleButton_logsettings);
        if(toggle.isChecked()){
            return Log.LOG_PERMANENT;
        }else{
            return Log.LOG_TEMPORARY;
        }
    }

	@Override
	public void onStatusChangeEvent(final String deviceName, final int status) {
		runOnUiThread(new Runnable() {
			@Override
			public synchronized void run() {
				ShowMsg.showStatusChangeEvent(deviceName, status, LogSettingsActivity.this);
			}
		});
	}

	@Override
	public void onBatteryStatusChangeEvent(final String deviceName, final int battery) {
		runOnUiThread(new Runnable() {
			@Override
			public synchronized void run() {
				ShowMsg.showBatteryStatusChangeEvent(deviceName, battery, LogSettingsActivity.this);
			}
		});
	}
}
