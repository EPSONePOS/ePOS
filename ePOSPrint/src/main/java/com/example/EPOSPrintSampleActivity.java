package com.example;

import com.epson.eposprint.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class EPOSPrintSampleActivity extends Activity implements OnClickListener, StatusChangeEventListener, BatteryStatusChangeEventListener {

    static Print printer = null;
    String openDeviceName = "192.168.192.168";
    int connectionType = Print.DEVTYPE_TCP;
    int language = com.epson.eposprint.Builder.LANG_EN;
    String printerName = "TM-T88V";

    static void setPrinter(Print obj){
        printer = obj;
    }

    static Print getPrinter(){
        return printer;
    }

    static void closePrinter(){
        try{
            printer.closePrinter();
            printer = null;
        }catch(Exception e){
            printer = null;
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Registration ClickListener
        int[] target = {
                R.id.button_discovery,
                R.id.button_open,
                R.id.button_close,
                R.id.button_text,
                R.id.button_image,
                R.id.button_barcode,
                R.id.button_2dcode,
                R.id.button_pagemode,
                R.id.button_cut,
                R.id.button_getstatus,
                R.id.button_getname,
                R.id.button_logsettings,
        };
        for(int i = 0; i < target.length; i++){
            Button button = (Button)findViewById(target[i]);
            button.setOnClickListener(this);
        }

        //update button state
        updateButtonState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closePrinter();
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch(v.getId()){
        case    R.id.button_discovery:
            intent = new Intent(this, DiscoverPrinterActivity.class);
            break;
        case    R.id.button_open:
            intent = new Intent(this, OpenActivity.class);
            break;
        case    R.id.button_close:
            if(printer != null){
                try{
                    printer.closePrinter();
                    printer = null;
                }catch(Exception e){
                    printer = null;
                }
            }
            updateButtonState();
            break;
        case    R.id.button_text:
            intent = new Intent(this, TextActivity.class);
            break;
        case    R.id.button_image:
            intent = new Intent(this, ImageActivity.class);
            break;
        case    R.id.button_barcode:
            intent = new Intent(this, BarcodeActivity.class);
            break;
        case    R.id.button_2dcode:
            intent = new Intent(this, Code2dActivity.class);
            break;
        case    R.id.button_pagemode:
            intent = new Intent(this, PageModeActivity.class);
            break;
        case    R.id.button_cut:
            intent = new Intent(this, CutActivity.class);
            break;
        case    R.id.button_getstatus:
            showPrinterStatus();
            break;
        case    R.id.button_getname:
            intent = new Intent(this, GetNameActivity.class);
            break;
        case    R.id.button_logsettings:
        	intent = new Intent(this, LogSettingsActivity.class);
        	break;
        default:
            break;
        }
        if(intent != null){
            //show activity
            intent.putExtra("devtype", connectionType);
            intent.putExtra("ipaddress", openDeviceName);
            intent.putExtra("printername", printerName);
            intent.putExtra("language", language);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null){
            if(resultCode == 1 || resultCode == 2){
                connectionType = data.getIntExtra("devtype", 0);
                openDeviceName = data.getStringExtra("ipaddress");
            }
            if(resultCode == 2){
                printerName = data.getStringExtra("printername");
                language = data.getIntExtra("language", 0);
            }
        }

        if (printer != null) {
            printer.setStatusChangeEventCallback(this);
            printer.setBatteryStatusChangeEventCallback(this);
        }
        updateButtonState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("openDeviceName", openDeviceName);
        outState.putInt("connectionType", connectionType);
        outState.putInt("language", language);
        outState.putString("printerName", printerName);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        openDeviceName = savedInstanceState.getString("openDeviceName");
        connectionType = savedInstanceState.getInt("connectionType");
        language = savedInstanceState.getInt("language");
        printerName = savedInstanceState.getString("printerName");
    }

    //change button satate(Eable/Disable)
    private void updateButtonState() {
        int[] target1 = {
                R.id.button_open,
                R.id.button_getname,
        };
        int[] target2 = {
                R.id.button_close,
                R.id.button_text,
                R.id.button_image,
                R.id.button_barcode,
                R.id.button_2dcode,
                R.id.button_pagemode,
                R.id.button_cut,
                R.id.button_getstatus,
        };
        int[] enableTarget = null;
        int[] disableTarget = null;
        if(printer == null){
            enableTarget = target1;
            disableTarget = target2;
        }else{
            enableTarget = target2;
            disableTarget = target1;
        }
        for(int i = 0; i < enableTarget.length; i++){
            Button button = (Button)findViewById(enableTarget[i]);
            button.setEnabled(true);
        }
        for(int i = 0; i < disableTarget.length; i++){
            Button button = (Button)findViewById(disableTarget[i]);
            button.setEnabled(false);
        }
    }

    private void showPrinterStatus(){
        Builder builder = null;
        String method = "";
        try{
            //create builder
            method = "Builder";
            builder = new Builder(printerName, language, getApplicationContext());

            //send builder data(empty builder data)
            int[] status = new int[1];
            int[] battery = new int[1];
            try{
                EPOSPrintSampleActivity.printer.sendData(builder, 0, status, battery);
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

	@Override
	public void onStatusChangeEvent(final String deviceName, final int status) {
		runOnUiThread(new Runnable() {
			@Override
			public synchronized void run() {
				ShowMsg.showStatusChangeEvent(deviceName, status, EPOSPrintSampleActivity.this);
			}
		});
	}

	@Override
	public void onBatteryStatusChangeEvent(final String deviceName, final int battery) {
		runOnUiThread(new Runnable() {
			@Override
			public synchronized void run() {
				ShowMsg.showBatteryStatusChangeEvent(deviceName, battery, EPOSPrintSampleActivity.this);
			}
		});
	}
}