package com.example.eposreceiptprintsample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;

import com.epson.epsonio.Finder;
import com.epson.epsonio.DeviceInfo;
import com.epson.epsonio.FilterOption;
import com.epson.epsonio.DevType;
import com.epson.epsonio.EpsonIoException;
import com.epson.epsonio.IoStatus;
import com.epson.eposprint.Print;

public class DiscoverPrinterActivity extends Activity implements OnItemClickListener, OnCheckedChangeListener, Runnable {

    final static int DISCOVERY_INTERVAL = 500;

    ArrayList<HashMap<String, String>> printerList = null;
    SimpleAdapter printerListAdapter = null;
    ScheduledExecutorService scheduler;
    ScheduledFuture<?> future;
    Handler handler = new Handler();

    // Called when the activity is first created. 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discovery);

        // init printer list control
        printerList = new ArrayList<HashMap<String, String>>();
        printerListAdapter = new SimpleAdapter(this, printerList, R.layout.list_at,
                                               new String[] { "PrinterName", "Address" },
                                               new int[] { R.id.PrinterName, R.id.Address });
        ListView list = (ListView)findViewById(R.id.listView_printerlist);
        list.setAdapter(printerListAdapter);
        list.setOnItemClickListener(this);

        // register OnCheckedChangeListener
        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radiogroup_devtype);
        radioGroup.setOnCheckedChangeListener(this);

        // select default radiobutton
        Intent intent = getIntent();

        switch (intent.getIntExtra("devtype", Print.DEVTYPE_TCP)) {
        case Print.DEVTYPE_TCP:
            radioGroup.check(R.id.radioButton_tcp);
            break;

        case Print.DEVTYPE_BLUETOOTH:
            radioGroup.check(R.id.radioButton_bluetooth);
            break;

        case Print.DEVTYPE_USB:
            radioGroup.check(R.id.radioButton_usb);
            break;

        default:
            radioGroup.check(R.id.radioButton_tcp);
            break;
        }

        // start find thread scheduler
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // find start
        findStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // stop find 
        if (future != null) {
            future.cancel(false);
            while (!future.isDone()) {
                try {
                    Thread.sleep(DISCOVERY_INTERVAL);
                }
                catch (Exception e) {
                    break;
                }
            }
            future = null;
        }

        if (scheduler != null) {
            scheduler.shutdown();

            scheduler = null;
        }

        // stop old finder
        while (true) {
            try {
                Finder.stop();
                break;
            }
            catch (EpsonIoException e) {
                if (e.getStatus() != IoStatus.ERR_PROCESSING) {
                    break;
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // return settings 
        Intent intent = new Intent();
        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radiogroup_devtype);

        switch (radioGroup.getCheckedRadioButtonId()) {
        case R.id.radioButton_tcp:
            intent.putExtra("devtype", Print.DEVTYPE_TCP);
            break;

        case R.id.radioButton_bluetooth:
            intent.putExtra("devtype", Print.DEVTYPE_BLUETOOTH);
            break;

        case R.id.radioButton_usb:
            intent.putExtra("devtype", Print.DEVTYPE_USB);
            break;

        default:
            intent.putExtra("devtype", Print.DEVTYPE_TCP);
            break;
        }

        HashMap<String, String> item  = printerList.get(position);
        intent.putExtra("ipaddress", item.get("Address"));

        // return main activity 
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    public void onCheckedChanged(RadioGroup arg0, int arg1) {
        // find restart 
        findStart();
    }

    @Override
    // find thread 
    public synchronized void run() {
        class UpdateListThread extends Thread {
            DeviceInfo[] list = null;

            public UpdateListThread(DeviceInfo[] listDevices) {
                list = listDevices;
            }

            @Override
            public void run() {
                if (list == null) {
                    if (printerList.size() > 0) {
                        printerList.clear();
                        printerListAdapter.notifyDataSetChanged();
                    }
                }
                else if (list.length != printerList.size()) {
                    printerList.clear();

                    for (int i = 0; i < list.length; i++) {
                        String name = list[i].getPrinterName();
                        String address = list[i].getDeviceName();

                        HashMap<String, String> item = new HashMap<String, String>();
                        item.put("PrinterName", name);
                        item.put("Address", address);

                        printerList.add(item);
                    }
                    printerListAdapter.notifyDataSetChanged();
                }
            }
        }

        try {
            DeviceInfo[] deviceList = Finder.getDeviceInfoList(FilterOption.PARAM_DEFAULT);

            handler.post(new UpdateListThread(deviceList));
        }
        catch (Exception e) {
            return;
        }
    }

    // find start / restart 
    private void findStart() {
        if (scheduler == null) {
            return;
        }

        // stop old finder 
        while (true) {
            try {
                Finder.stop();
                break;
            }
            catch (EpsonIoException e) {
                if (e.getStatus() != IoStatus.ERR_PROCESSING) {
                    break;
                }
            }
        }

        // stop find thread 
        if (future != null) {
            future.cancel(false);
            while (!future.isDone()) {
                try {
                    Thread.sleep(DISCOVERY_INTERVAL);
                }
                catch (Exception e) {
                    break;
                }
            }

            future = null;
        }

        // clear list 
        printerList.clear();
        printerListAdapter.notifyDataSetChanged();

        try {
			// get device type and find 
			RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radiogroup_devtype);

            switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radioButton_tcp:
                Finder.start(this, DevType.TCP, "255.255.255.255");
                break;

            case R.id.radioButton_bluetooth:
                Finder.start(this, DevType.BLUETOOTH, null);
                break;

            case R.id.radioButton_usb:
                Finder.start(this, DevType.USB, null);
                break;

            default:
                Finder.start(this, DevType.TCP, "255.255.255.255");
                break;
            }
        }
        catch (EpsonIoException e) {
			String msg = "";
            String errorMsg = "";
            String warningMsg = "";
			Result result = new Result();

            result.setEpsonIoException(e);

            errorMsg = MsgMaker.makeErrorMessage(this, result);
            warningMsg = MsgMaker.makeWarningMessage(this, result);

            if (!errorMsg.isEmpty()) {
                msg = getString(R.string.handlingmsg_notice_error) + errorMsg;
            }

            if (!warningMsg.isEmpty()) {
                msg = getString(R.string.handlingmsg_notice_warning) + warningMsg;
            }

            ShowMsg.show(this, msg);

            result = null;

            return ;
        }

        // start thread
        future = scheduler.scheduleWithFixedDelay(this, 0, DISCOVERY_INTERVAL, TimeUnit.MILLISECONDS);
    }

}
