package com.example;

import java.io.InputStream;

import com.epson.eposprint.*;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class ImageActivity extends Activity implements OnClickListener, StatusChangeEventListener, BatteryStatusChangeEventListener {

    static final int SEND_TIMEOUT = 10 * 1000;
    static final int REQUEST_CODE = 12345;
    static final int IMAGE_WIDTH_MAX = 512;
    
    Bitmap selectImage = null;
    boolean exitActivity = false;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image);
    
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
        
        //init printer list
        Spinner spinner = (Spinner)findViewById(R.id.spinner_colormode);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(getString(R.string.mode_mono));
        adapter.add(getString(R.string.mode_gray16));
        spinner.setAdapter(adapter);

        //init language list
        spinner = (Spinner)findViewById(R.id.spinner_halftonemethod);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(getString(R.string.halftone_dither));
        adapter.add(getString(R.string.halftone_error_diffusion));
        adapter.add(getString(R.string.halftone_threshold));
        spinner.setAdapter(adapter);
        
        //init edit
        TextView textBrightness = (TextView)findViewById(R.id.editText_brightness);
        textBrightness.setText("1.0");
        
        //Registration ClickListener
        Button button = (Button)findViewById(R.id.button_print);
        button.setOnClickListener(this);

        button = (Button)findViewById(R.id.button_select);
        button.setOnClickListener(this);
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
        switch(v.getId()){
        case    R.id.button_print:
            printImage();
            break;
        case    R.id.button_select:
            selectImage();
            break;
        default:
            break;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            ImageView view = (ImageView)findViewById(R.id.imageView);
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());
                selectImage = BitmapFactory.decodeStream(in);

                if(view.getWidth() < selectImage.getWidth()
                            || view.getHeight() < selectImage.getHeight()){
                    view.setScaleType(ScaleType.FIT_CENTER);
                }else{
                    view.setScaleType(ScaleType.CENTER);
                }
                view.setImageBitmap(selectImage);

                in.close();
            } catch (Exception e) {
                view.setImageBitmap(null);
            }
        }
    }
    
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE);
    }
    
    private void printImage() {
        if(selectImage == null){
            ShowMsg.showError(R.string.errmsg_noimage, this);
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
            method = "addImage";
            builder.addImage(selectImage, 0, 0, Math.min(IMAGE_WIDTH_MAX, selectImage.getWidth()), selectImage.getHeight(), Builder.COLOR_1, 
                    getBuilderMode(), getBuilderHalftone(), getBuilderBrightness());

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
    
    private int getBuilderMode() {
        Spinner spinner = (Spinner)findViewById(R.id.spinner_colormode);
        switch(spinner.getSelectedItemPosition()){
        case 1:
            return Builder.MODE_GRAY16;
        case 0:
        default:
            return Builder.MODE_MONO;
        }
    }
    
    private int getBuilderHalftone() {
        Spinner spinner = (Spinner)findViewById(R.id.spinner_halftonemethod);
        switch(spinner.getSelectedItemPosition()){
        case 1:
            return Builder.HALFTONE_ERROR_DIFFUSION;
        case 2:
            return Builder.HALFTONE_THRESHOLD;
        case 0:
        default:
            return Builder.HALFTONE_DITHER;
        }
    }
    
    private double getBuilderBrightness() {
        TextView text = (TextView)findViewById(R.id.editText_brightness);
        try{
            return Double.parseDouble(text.getText().toString());
        }catch(Exception e){
            return 1.0;
        }
    }
    
	@Override
	public void onStatusChangeEvent(final String deviceName, final int status) {
		runOnUiThread(new Runnable() {
			@Override
			public synchronized void run() {
				ShowMsg.showStatusChangeEvent(deviceName, status, ImageActivity.this);
			}
		});
	}

	@Override
	public void onBatteryStatusChangeEvent(final String deviceName, final int battery) {
		runOnUiThread(new Runnable() {
			@Override
			public synchronized void run() {
				ShowMsg.showBatteryStatusChangeEvent(deviceName, battery, ImageActivity.this);
			}
		});
	}
}
