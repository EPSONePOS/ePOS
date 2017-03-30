package com.example.eposreceiptprintsample;

import com.epson.eposprint.EposException;
import com.epson.epsonio.EpsonIoException;

public class Result {
    private int printerStatus = 0;
    private int batteryStatus = 0;
    private EposException eposException = null;
    private EpsonIoException epsonIoException = null;
    private final int BATTERY_NO_DATA = 0x0000;

    public int getPrinterStatus() {
        return printerStatus;
    }

    public void setPrinterStatus(int status) {
        this.printerStatus = status;
    }

    public int getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(int battery) {
        this.batteryStatus = battery;
    }

    public EposException getEposException() {
        return eposException;
    }

    public void setEposException(EposException epos) {
        this.eposException = epos;

        if (epos != null) {
            this.printerStatus |= epos.getPrinterStatus();
            int tmpBattery = epos.getBatteryStatus();
            if (tmpBattery != BATTERY_NO_DATA) {
                this.batteryStatus = tmpBattery;
            }
        }
    }

    public EpsonIoException getEpsonIoException() {
        return epsonIoException;
    }

    public void setEpsonIoException(EpsonIoException epsonio) {
        this.epsonIoException = epsonio;
    }

}
